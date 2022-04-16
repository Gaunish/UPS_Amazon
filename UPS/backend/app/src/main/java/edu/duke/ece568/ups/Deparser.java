package edu.duke.ece568.ups;

import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.duke.ece568.ups.AmazonUps.*;
import edu.duke.ece568.ups.WorldUps.*;

public class Deparser {
    private ClientConnection conn;

    public Deparser(ClientConnection conn){
        this.conn = conn;
    }

    //returns true if we have seqNum already
    public synchronized boolean checkSeqNum(long seqNum, ArrayList<Long>ackList, HashSet<Long> recvSeq){
        ackList.add(seqNum);
        if(recvSeq.contains(seqNum)){
            return true;
        }
        recvSeq.add(seqNum);
        return false;
    } 

    public synchronized void sendAcks_A(ArrayList<Long> ackList) throws IOException{
        UACommand.Builder cmd = UACommand.newBuilder();
        for(Long ack : ackList){
            cmd.addAcks(ack);
        }
        MessageTransmitter.sendMsgTo(cmd.build(), conn.getOutputStream());
    }

    public synchronized void sendAcks_W(ArrayList<Long> ackList) throws IOException{
        UCommands.Builder cmd = UCommands.newBuilder();
        for(Long ack : ackList){
            cmd.addAcks(ack);
        }
        MessageTransmitter.sendMsgTo(cmd.build(), conn.getOutputStream());
    }
}
