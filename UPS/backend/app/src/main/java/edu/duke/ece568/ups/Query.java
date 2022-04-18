package edu.duke.ece568.ups;

import java.io.OutputStream;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;

import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UDeliveryLocation;
import edu.duke.ece568.ups.WorldUps.UGoDeliver;
import edu.duke.ece568.ups.WorldUps.UGoPickup;
import edu.duke.ece568.ups.WorldUps.UQuery;
import edu.duke.ece568.ups.AmazonUps.UACommand;


public class Query implements Action{
    private Command cmd;
    private OutputStream out;
    private int truckid;
    private long seqnum;
    UQuery.Builder query;


    public Query(OutputStream out, int truckid, long seqnum){
        this.out = out;
        this.truckid = truckid;
        this.seqnum = seqnum;

        UQuery.Builder query = UQuery.newBuilder();
        query.setTruckid(truckid);
        query.setSeqnum(seqnum);
        this.query = query;

        UCommands.Builder uCommand = UCommands.newBuilder();
        uCommand.addQueries(query);
        
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
        ucommand.addQueries(query);
    }

    public void append(UACommand.Builder aucommand){
      return;    
    }
    
    public String getType(){
      return "Query";
    }
}
