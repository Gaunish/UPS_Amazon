package edu.duke.ece568.ups;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
  //volatile: any change made to this variable will be visible immediately in all threads
  public volatile long worldseqnum,amazonseqnum;
    ConcurrentHashMap<Long,Action> W_actions, A_actions; 

  public Executor(Database db, ClientConnection WConn, ClientConnection AConn, ConcurrentHashMap<Long,Action> W_actions, ConcurrentHashMap<Long,Action> A_actions, long wseq, long aseq){
        this.db = db;
        this.Aconn = AConn;
        this.WConn = WConn;
        this.W_actions = W_actions;
        this.A_actions = A_actions;
        this.worldseqnum = wseq;
        this.amazonseqnum = aseq;
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
    String sql = "SELECT TRUCK_ID FROM TRUCK WHERE WHID = " + whid + " AND STATUS = \'traveling\';";
    ResultSet truckstatus = db.SelectStatement(sql);
    int truckid;
    try {
      if (truckstatus.next()) {
        truckid = truckstatus.getInt("truck_id");
      } else {
        sql = "SELECT TRUCK_ID FROM TABLE TRUCK WHERE STATUS = \'idle\' OR STATUS = \'delivering\' ORDER BY FIELD(STATUS, \'idle\', \'delivering\');";
        ResultSet newtruck = db.SelectStatement(sql);
        if (newtruck.next()) {
          truckid = newtruck.getInt("truck_id");
          sql = "UPDATE TRUCK SET STATUS = \'traveling\' AND WHID =" + whid + " WHERE TRUCK_ID =" + truckid + ";";
          db.executeStatement(sql, "failure");
          Action pickup = new Pickup(WConn.getOutputStream(), truckid, whid, worldseqnum);
          W_actions.put(worldseqnum, pickup);
          worldseqnum++;
          pickup.sendMessage();
        } else {
          // send error to amazon saying there is no availabel trucks
          return;
        }
      }
      String update = "INSERT INTO PACKAGE VALUES(" + packageid + "," + x + "," + y + "," + truckid + "," + username
          + ",\'pickup\');";
      db.executeStatement(update, "failure");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void insertProducts(long packageid,APack apack){
    List<AProduct> product_list = apack.getThingsList();
    for(AProduct aproduct:product_list){
      String sql = "INSERT INTO PRODUCT(PACKAGE_ID,DESCRIPTION,COUNT) VALUES("+packageid+", '"+aproduct.getDescription()+"',"+aproduct.getCount()+");";
      db.executeStatement(sql,"failure");
    }
  }

    public void execute(AUReadyForDelivery delivery) throws IOException {
        int truck_id = delivery.getTruckid();
        Action d = new Deliver(db, WConn.getOutputStream(), truck_id, worldseqnum);
        W_actions.put(worldseqnum, d);
        worldseqnum++;
        d.sendMessage();
        //String update_q = "UPDATE TRUCK SET STATUS = ";
    }

    //amazon error
    public void execute(Err errA) throws IOException {
        long origin_seqno = errA.getOriginSeqnum();
        long err_seqno = errA.getErrorSeqnum();
        Action a = A_actions.get(origin_seqno);
        int truck_id = a.getTruckid();
        if(a.getType() == "AUPickup"){
          String q1 = "DELETE FROM PRODUCT WHERE PACKAGE_ID IN (SELECT PACKAGE_ID FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\');";
          String q2 = "DELETE FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\';";

          db.executeStatement(q1, "failure");
          db.executeStatement(q2, "failure");
        }
    }
       

  private void AssociateUPSAccount(long packageid,String username) {
    String sql = "SELECT COUNT(*) FROM USERS WHERE USERNAME = "+username+";";
    try{
    ResultSet rs =db.SelectStatement(sql);
    boolean ismatched;
    if(rs.getInt("1") > 0){
      ismatched = true;
    }else{
      ismatched = false;
    }
    //form UAIsAssociated from packageid and ismatch
    Action isAssociated = new AUassoc(Aconn.getOutputStream(), packageid, ismatched, amazonseqnum);
    A_actions.put(amazonseqnum,isAssociated);
    amazonseqnum++;
    isAssociated.sendMessage();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void execute(UFinished completions) throws IOException {
    int truck_id = completions.getTruckid();
    String status = completions.getStatus();
     
    String new_status = "";

    //Pickup request
    if(status.equals("arrive warehouse")){
      new_status = "loading";

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
    Action a = A_actions.get(origin_seqno);
    int truck_id = a.getTruckid();

    if(a.getType() == "Pickup"){
      String q1 = "DELETE FROM PRODUCT WHERE PACKAGE_ID IN (SELECT PACKAGE_ID FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\');";
      String q2 = "DELETE FROM PACKAGE WHERE TRUCK_ID = " + truck_id + " AND STATUS = \'PICKUP\';";

      db.executeStatement(q1, "failure");
      db.executeStatement(q2, "failure");

      String update = "UPDATE TRUCK SET STATUS = \'IDLE\' WHERE TRUCK_ID = " + truck_id + ";";
      db.executeStatement(update, "failure");

    }
    else if(a.getType() == "Deliver"){
      String update = "UPDATE TRUCK SET STATUS = \'ARRIVE WAREHOUSE\' WHERE TRUCK_ID = " + truck_id + ";";
      db.executeStatement(update, "failure");
    }
  }

}
