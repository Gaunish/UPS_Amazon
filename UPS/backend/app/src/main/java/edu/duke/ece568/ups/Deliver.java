package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;

import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UDeliveryLocation;
import edu.duke.ece568.ups.WorldUps.UGoDeliver;
import edu.duke.ece568.ups.WorldUps.UGoPickup;

public class Deliver implements Action{
    private Command cmd;
    private OutputStream out;
    private int truckid;
    private ArrayList<UDeliveryLocation> loc;
    private long seqnum;
    UGoDeliver.Builder deliver;

    public Deliver(OutputStream out, int truckid, ArrayList<UDeliveryLocation> locations, long seqnum){
        this.out = out;
        this.truckid = truckid;
        this.loc = locations;
        this.seqnum = seqnum;
        
        UGoDeliver.Builder goDeliver = UGoDeliver.newBuilder();
        goDeliver.setTruckid(truckid);
        goDeliver.setSeqnum(seqnum);
        for(int i=0;i<locations.size();i++){
          goDeliver.addPackages(locations.get(i));
        }
        deliver = goDeliver;
    
        UCommands.Builder uCommand = UCommands.newBuilder();
        uCommand.addDeliveries(goDeliver);
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
        ucommand.addDeliveries(deliver);
    }
}
