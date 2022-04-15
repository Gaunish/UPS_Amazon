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


public class Query implements Action{
    private Command cmd;
    private OutputStream out;
    private int truckid;
    private long seqnum;


    public Query(OutputStream out, int truckid, long seqnum){
        this.out = out;
        this.truckid = truckid;
        this.seqnum = seqnum;

        UQuery.Builder query = UQuery.newBuilder();
        query.setTruckid(truckid);
        query.setSeqnum(seqnum);

        UCommands.Builder uCommand = UCommands.newBuilder();
        uCommand.addQueries(query);
        
        cmd = new Command(out,uCommand.build(),seqnum);
    }

    public void sendMessage() throws IOException{
        cmd.sendMessage();
    }
    
    public boolean isTimeout(){
        return cmd.isTimeout();
    }

    public void checkAck() throws IOException{
        cmd.checkAck();
    }

    public void setAck(long ackNo) throws IOException{
        cmd.setAck(ackNo);
    }
    
}
