package edu.duke.ece568.ups;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.duke.ece568.ups.AmazonUps.AUPack;
import edu.duke.ece568.ups.AmazonUps.AUReadyForDelivery;
import edu.duke.ece568.ups.AmazonUps.AURequestPickup;
import edu.duke.ece568.ups.WorldAmazon.APack;
import edu.duke.ece568.ups.WorldAmazon.AProduct;

public class Executor {
  Database db;
  OutputStream out;
  InputStream in;
  public volatile long worldseqnum;
  public volatile long amazonseqnum;
  ConcurrentHashMap<Long, Action> worldActions;
  ConcurrentHashMap<Long, Action> amazonActions;

  public Executor(Database db, InputStream in, OutputStream out, ConcurrentHashMap<Long, Action> worldActions, long worldseqnum,long amazonseqnum) {
    this.db = db;
    this.in = in;
    this.out = out;
    this.worldseqnum = worldseqnum;
    this.amazonseqnum = amazonseqnum;
    this.worldActions = worldActions;
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
    String sql = "SELECT TRUCK_ID FROM TABLE TRUCK WHERE WHID = " + whid + " AND STATUS = 'en route';";
    ResultSet truckstatus = db.SelectStatement(sql);
    int truckid;
    try {
      if (truckstatus.next()) {
        truckid = truckstatus.getInt("truck_id");
      } else {
        sql = "SELECT TRUCK_ID FROM TABLE TRUCK WHERE STATUS = 'idle' OR STATUS = 'delivering' ORDER BY FIELD(STATUS, 'idle', 'delivering');";
        ResultSet newtruck = db.SelectStatement(sql);
        if (newtruck.next()) {
          truckid = newtruck.getInt("truck_id");
          sql = "UPDATE TRUCK SET STATUS = 'en route' AND WHID =" + whid + " WHERE TRUCK_ID =" + truckid + ";";
          db.executeStatement(sql, "failure");
          Action pickup = new Pickup(out, truckid, whid, worldseqnum);
          worldActions.put(worldseqnum, pickup);
          worldseqnum++;
          pickup.sendMessage();
        } else {
          // send error to amazon saying there is no availabel trucks
          return;
        }
      }
      String update = "INSERT INTO PACKAGE VALUES(" + packageid + "," + x + "," + y + "," + truckid + "," + username
          + ",'pickup');";
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
    Action isAssociated = new IsAssociated(out, packageid, ismatched, amazonseqnum);
    amazonActions.put(amazonseqnum,isAssociated);
    amazonseqnum++;
    isAssociated.sendMessage();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public void execute(AUReadyForDelivery delivery) {

  }

}
