package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;

import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UDeliveryLocation;
import edu.duke.ece568.ups.WorldUps.UGoDeliver;
import edu.duke.ece568.ups.WorldUps.UGoPickup;
import edu.duke.ece568.ups.AmazonUps.UACommand;

import java.sql.ResultSet;

public class Deliver implements Action{
    private Command cmd;
    private OutputStream out;
    private int truckid;
    private long seqnum;
    private Database db;
    UGoDeliver.Builder deliver;

    public Deliver(Database db, OutputStream out, int truck_id, long seqnum){
      this.db = db;
      this.out = out;
      this.truckid = truck_id;
      this.seqnum = seqnum;

      cmd = null;

      UGoDeliver.Builder goDeliver = UGoDeliver.newBuilder();
      goDeliver.setTruckid(truckid);
      goDeliver.setSeqnum(seqnum);

      String q = "SELECT * FROM PACKAGE WHERE TRUCK_ID = " + truck_id + ";";
      ResultSet res = db.SelectStatement(q);
      try{
        while(res.next()){
          UDeliveryLocation.Builder del = UDeliveryLocation.newBuilder();
          del.setPackageid(res.getLong("PACKAGE_ID"));
          del.setX(res.getInt("X"));
          del.setY(res.getInt("Y"));
          goDeliver.addPackages(del);
        }
      } 
      catch(Exception e){ return; }

      this.deliver = goDeliver;
      UCommands.Builder uCommand = UCommands.newBuilder();
      uCommand.addDeliveries(deliver);
      cmd = new Command(out,uCommand.build(),seqnum);
    }

    public void sendMessage() throws IOException{
      if(cmd == null){ return; }  
      cmd.sendMessage();
    }

    public boolean checkAck() throws IOException{
      return cmd.isAcked;
    }

    public void setAck() throws IOException{
      cmd.isAcked = true;
    }

    public void append(UCommands.Builder ucommand){
        ucommand.addDeliveries(deliver);
    }

    public void append(UACommand.Builder aucommand){
      return;    
    }

    public String getType(){
      return "Deliver";
    }
}
