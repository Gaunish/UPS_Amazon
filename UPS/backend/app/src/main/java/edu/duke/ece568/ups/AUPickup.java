package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.io.IOException;

import edu.duke.ece568.ups.AmazonUps.AUCommand;
import edu.duke.ece568.ups.AmazonUps.AUPack;
import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAReadyForPickup;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class AUPickup implements Action{
    private Command cmd;
    private OutputStream out;
    private UAReadyForPickup.Builder pickup;
    private int truck_id;

    public AUPickup(OutputStream out, Database db, int truckid, long seqnum){
      this.truck_id = truckid;
      this.out = out;

      int whid = get_whid(truckid, db);
      if(whid == -1){
        return;
      }

      UAReadyForPickup.Builder pickup = UAReadyForPickup.newBuilder();
      pickup.setWhnum(whid);
      pickup.setTruckid(truckid);
      pickup.setSeqnum(seqnum);

      String q = "SELECT * FROM PACKAGE WHERE TRUCK_ID = " + truckid + " AND STATUS = \'PICKUP\';";
      ResultSet rs = db.SelectStatement(q);
      try{
        while(rs.next()){
          long id = rs.getLong("PACKAGE_ID");
          pickup.addPackageid(id);
        }
      }
      catch(Exception e){ return; }

      this.pickup = pickup;
      UACommand.Builder ua = UACommand.newBuilder();
      ua.addPickupReady(pickup);
      cmd = new Command(out,ua.build(),seqnum);
    }

    private int get_whid(int truckid, Database db){
      String q_whid = "SELECT WHID FROM TRUCK WHERE TRUCK_ID = " + truckid + ";";
      ResultSet rs = db.SelectStatement(q_whid);
      int whid;
      try{
        if(rs.next()){
          int id = rs.getInt(1);
          return id;
        }
      }
      catch(Exception e){ }
      return -1;
    }

    public void sendMessage() throws IOException{
        cmd.sendMessage();
    }

    public boolean checkAck() throws IOException{
      return cmd.isAcked;
    }

    public void setAck() throws IOException{
      cmd.isAcked = true;
    }

    public void append(UCommands.Builder ucommand){
        return;
    }

    public void append(UACommand.Builder aucommand){
        aucommand.addPickupReady(pickup);
    }
    
    public String getType(){
      return "AUPickup";
    }

    public int getTruckid(){
      return truck_id;
    }
}
