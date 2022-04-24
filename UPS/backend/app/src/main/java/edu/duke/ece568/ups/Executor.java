package edu.duke.ece568.ups;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.io.OutputStream;

import edu.duke.ece568.ups.AmazonUps.AUPack;
import edu.duke.ece568.ups.AmazonUps.AUReadyForDelivery;
import edu.duke.ece568.ups.AmazonUps.AURequestPickup;
import edu.duke.ece568.ups.AmazonUps.Err;
import edu.duke.ece568.ups.WorldAmazon.APack;
import edu.duke.ece568.ups.WorldAmazon.AProduct;
import edu.duke.ece568.ups.AmazonUps.*;
import edu.duke.ece568.ups.WorldUps.*;

public class Executor {
    Database db;
    ClientConnection WConn, Aconn;
    History history;
  //volatile: any change made to this variable will be visible immediately in all threads
  public volatile long worldseqnum,amazonseqnum;
    ConcurrentHashMap<Long,Action> W_actions, A_actions;
    private AUError auError; 

  public Executor(Database db, ClientConnection WConn, ClientConnection AConn, ConcurrentHashMap<Long,Action> W_actions, ConcurrentHashMap<Long,Action> A_actions, long wseq, long aseq){
        this.db = db;
        this.Aconn = AConn;
        this.WConn = WConn;
        this.W_actions = W_actions;
        this.A_actions = A_actions;
        this.worldseqnum = wseq;
        this.amazonseqnum = aseq;
        this.history = new History();
    }

  public void execute(AURequestPickup pickup) {
    AUPack aupack = pickup.getPack();
    APack apack = aupack.getPackage();
    int whid = apack.getWhnum();
    long packageid = apack.getShipid();
    int x = aupack.getDestx();
    int y = aupack.getDesty();
    String username = "nil";
    if (aupack.getUpsAccount() != null) {
      username = aupack.getUpsAccount();
    }
    UpdatePackageTable(whid, packageid, x, y, username);
    insertProducts(packageid, apack);
    AssociateUPSAccount(packageid, username);
  }

  private void UpdatePackageTable(int whid, long packageid, int x, int y, String username) {
    // query truck status
    String sql = "SELECT * FROM TRUCK WHERE WHID = " + whid + " AND STATUS = \'traveling\' OR STATUS = \'ARRIVE WAREHOUSE\';";
    ResultSet truckstatus = db.SelectStatement(sql);
    int truckid, x_cood, y_cood;
    try {
      if (truckstatus != null && truckstatus.next()) {
        truckid = truckstatus.getInt("truck_id");
        x_cood = truckstatus.getInt("X");
        y_cood = truckstatus.getInt("Y");
      } else {
        sql = "SELECT * FROM TRUCK WHERE STATUS = \'idle\';";
        ResultSet newtruck = db.SelectStatement(sql);
        if (newtruck != null && newtruck.next()) {
          truckid = newtruck.getInt("truck_id");
          x_cood = newtruck.getInt("X");
          y_cood = newtruck.getInt("Y");
          sql = "UPDATE TRUCK SET STATUS = 'traveling', WHID =" + whid + " WHERE TRUCK_ID =" + truckid + ";";
          db.executeStatement(sql, "failure");
          Action pickup = new Pickup(WConn.getOutputStream(), truckid, whid, worldseqnum);
          W_actions.put(worldseqnum, pickup);
          worldseqnum++;
          pickup.sendMessage();
        } else {
          // send error to amazon saying there is no availabel trucks
          System.out.println("Error in trucks");
          return;
        }
      }
      String update = "INSERT INTO PACKAGE VALUES(" + packageid + "," + x + "," + y + "," + truckid + ",\'" + username
          + "\',\'PICKUP\');";
      db.executeStatement(update, "failure");

      updateHist(truckid, packageid,  "Package is ready to pickup");
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void updateHist(int truckid, long packageid, String str){
    history.updateHistory(db, packageid, truckid, str);
    try{
    history.sendQuery(W_actions, WConn.getOutputStream(), truckid, worldseqnum);
    }catch(Exception e){}
    worldseqnum++;
  }

  private void insertProducts(long packageid,APack apack){
    List<AProduct> product_list = apack.getThingsList();
    for(AProduct aproduct:product_list){
      String sql = "INSERT INTO PRODUCT(PACKAGE_ID,DESCRIPTION,COUNT) VALUES("+packageid+", \'"+aproduct.getDescription()+"\',"+aproduct.getCount()+");";
      db.executeStatement(sql,"failure");
    }
  }

  public void updatePackageHist(int truck_id, String status, String query){
    String q = "SELECT * FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'"+ status +"\';";
    ResultSet res = db.SelectStatement(q);
    try{
      while(res != null && res.next()){
        long packageid = res.getLong("PACKAGE_ID");
        updateHist(truck_id, packageid, query);
      }
    }
    catch(Exception e){}
  }

  public void execute(AUReadyForDelivery delivery) throws IOException {
        int truck_id = delivery.getTruckid();
        Action d = new Deliver(db, WConn.getOutputStream(), truck_id, worldseqnum);
        W_actions.put(worldseqnum, d);
        worldseqnum++;
        d.sendMessage();
        
        updatePackageHist(truck_id, "DELIVERING", "Package is out for delivery");
  }

    //amazon error
    public void execute(Err errA) throws IOException {
        long origin_seqno = errA.getOriginSeqnum();
        long err_seqno = errA.getErrorSeqnum();
        Action a = A_actions.get(origin_seqno);
        int truck_id = a.getTruckid();
        if(a.getType() == "AUPickup"){
          String q1 = "DELETE FROM PRODUCT WHERE PACKAGE_ID IN (SELECT PACKAGE_ID FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\' OR STATUS = \'LOADING\');";
          String q2 = "DELETE FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\' OR STATUS = \'LOADING\';";

          db.executeStatement(q1, "failure");
          db.executeStatement(q2, "failure");

          //update status of truck
          String update = "UPDATE TRUCK SET STATUS = \'ARRIVE WAREHOUSE\' WHERE TRUCK_ID = " + truck_id + ";";
    
        }
    }

  public void execute(UDeliveryMade delivered){
    long packageid = delivered.getPackageid();
    updatePackageStatus(packageid, "delivered");
    try{
    Action deliveryMade = new AUDeliver(Aconn.getOutputStream(),packageid,amazonseqnum);
    A_actions.put(amazonseqnum,deliveryMade);
    amazonseqnum++;
    deliveryMade.sendMessage();

    int truck_id = -1;
    String q = "SELECT * FROM PACKAGE WHERE PACKAGE_ID = " + packageid + ";";
    ResultSet rs = db.SelectStatement(q);
    
    truck_id = rs.getInt("TRUCK_ID");
    updateHist(truck_id, packageid, "Package is delivered");

    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void execute(UTruck truckstatus){
    int truckid = truckstatus.getTruckid();
    String status = truckstatus.getStatus();
    int x = truckstatus.getX();
    int y = truckstatus.getY();
    updateTruckStatus(truckid, status,x,y);
  }

  private void updatePackageStatus(long packageid,String status){
    String sql = "UPDATE PACKAGE SET STATUS = \'"+status+"\' WHERE PACKAGE_ID = "+packageid+";";
    db.executeStatement(sql, "failure");
  }

  private void updateTruckStatus(int truckid,String status,int x, int y){
    String sql = "UPDATE TRUCK SET STATUS = \'"+status+"\', X ="+x+",Y ="+y+" WHERE TRUCK_ID = "+truckid+";";
    db.executeStatement(sql, "failure");
  }
       
  private void AssociateUPSAccount(long packageid,String username) {
    String sql = "SELECT COUNT(*) FROM AUTH_USER WHERE USERNAME = "+username+";";
    try{
    ResultSet rs =db.SelectStatement(sql);
    boolean ismatched;
    if(rs != null){
      ismatched = true;
    }else{
      ismatched = false;
    }
    //form UAIsAssociated from packageid and ismatch
    Action isAssociated = new AUassoc(Aconn.getOutputStream(), packageid, ismatched, amazonseqnum);
    A_actions.put(amazonseqnum,isAssociated);
    amazonseqnum++;
    isAssociated.sendMessage();
    isAssociated.setAck();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void execute(UFinished completions) throws IOException {
    int truck_id = completions.getTruckid();
    String status = completions.getStatus();
    String new_status = "";

    //Pickup request
    if(status.equals("ARRIVE WAREHOUSE")){
      new_status = "loading";
      String update_package = "UPDATE PACKAGE SET STATUS = \'LOADING\' WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\';";
      db.executeStatement(update_package, "failure");

      updatePackageHist(truck_id, "LOADING", "Package is loading");
      
      //send notification to amazon
      Action pickupReady = new AUPickup(Aconn.getOutputStream(), db, truck_id, amazonseqnum); 
      A_actions.put(amazonseqnum,pickupReady);
      amazonseqnum++;
      pickupReady.sendMessage();
    }
    else{
      new_status = "idle";
    }

    //Update truck status
    String update = "UPDATE TRUCK SET STATUS = \'" + new_status + "\' WHERE TRUCK_ID = " + truck_id + ";";
    db.executeStatement(update, "failure");
  }

  //World error
  public void execute(UErr err) throws IOException {
    long origin_seqno = err.getOriginseqnum();
    long err_seqno = err.getSeqnum();
    Action a = W_actions.get(origin_seqno);
    int truck_id = a.getTruckid();

    if(a.getType() == "Pickup"){
      String q1 = "DELETE FROM PRODUCT WHERE PACKAGE_ID IN (SELECT PACKAGE_ID FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\');";
      String q2 = "DELETE FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\';";

      db.executeStatement(q1, "failure");
      db.executeStatement(q2, "failure");

      String update = "UPDATE TRUCK SET STATUS = \'IDLE\' WHERE TRUCK_ID = " + truck_id + ";";
      db.executeStatement(update, "failure");

      //send error back to amazon
      for(Action action : A_actions.values()){
        if(action.getTruckid() == truck_id && action.getType() == "AUPickup"){
          long seqnum = action.getSeqnum();
          String str = "Truck not arriving at warehouse";
          AUError auError = new AUError(Aconn.getOutputStream(), seqnum, amazonseqnum, str);
          auError.sendMessage();
          A_actions.put(amazonseqnum,auError);
          amazonseqnum++;
          break;
        }
      }

    }
    else if(a.getType() == "Deliver"){
      String update = "UPDATE TRUCK SET STATUS = \'ARRIVE WAREHOUSE\' WHERE TRUCK_ID = " + truck_id + ";";
      db.executeStatement(update, "failure");
    }
  }

  //Acks
  public void execute(long acks, Boolean isWorld) throws IOException{
    if(isWorld){
      Action a = W_actions.get(acks);
      a.setAck();
    }
    else{
      Action a = A_actions.get(acks);
      a.setAck();
    }
  }

}
