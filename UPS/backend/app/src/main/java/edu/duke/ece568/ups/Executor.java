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
import edu.duke.ece568.ups.WorldUps.UDeliveryMade;
import edu.duke.ece568.ups.WorldUps.UTruck;

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

    public void execute(Err errA) throws IOException {
        long origin_seqno = errA.getOriginSeqnum();
        long err_seqno = errA.getErrorSeqnum();
        Action a = A_actions.get(origin_seqno);
    }

  public void execute(UDeliveryMade delivered){
    long packageid = delivered.getPackageid();
    updatePackageStatus(packageid, "delivered");
    try{
    Action deliveryMade = new AUDeliver(Aconn.getOutputStream(),packageid,amazonseqnum);
    A_actions.put(amazonseqnum,deliveryMade);
    amazonseqnum++;
    deliveryMade.sendMessage();
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
    String sql = "UPDATE PACKAGE SET STATUS = '"+status+"' WHERE PACKAGE_ID = "+packageid+";";
    db.executeStatement(sql, "failure");
  }

  private void updateTruckStatus(int truckid,String status,int x, int y){
    String sql = "UPDATE TRUCK SET STATUS = '"+status+"', X ="+x+",Y ="+y+" WHERE TRUCK_ID = "+truckid+";";
    db.executeStatement(sql, "failure");
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

}
