package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;

import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UDeliveryLocation;
import edu.duke.ece568.ups.WorldUps.UGoDeliver;
import edu.duke.ece568.ups.WorldUps.UGoPickup;
import edu.duke.ece568.ups.AmazonUps.UACommand;

public class Pickup implements Action{
    private Command cmd;
    private OutputStream out;
    private int truckid, whid;
    private long seqnum;
    private UGoPickup.Builder pickup;

  public Pickup(OutputStream out, int truckid,int whid, long seqnum){
        this.out = out;
        this.truckid = truckid;
        this.whid = whid;
        this.seqnum = seqnum;

        UGoPickup.Builder goPickup = UGoPickup.newBuilder();
        goPickup.setTruckid(truckid);
        goPickup.setWhid(whid);
        goPickup.setSeqnum(seqnum);
        pickup = goPickup;
    
        UCommands.Builder uCommand = UCommands.newBuilder();
        uCommand.addPickups(goPickup);
        
        cmd = new Command(out,uCommand.build(),seqnum);
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
        ucommand.addPickups(pickup);
    }
    
    public void append(UACommand.Builder aucommand){
      return;    
    }
    
    public String getType(){
      return "Pickup";
    }

    public int getTruckid(){
      return truckid;
    }
    public long getSeqnum(){
      return seqnum;
    }

}
