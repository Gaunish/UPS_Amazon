package edu.duke.ece568.ups;

import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import edu.duke.ece568.ups.AmazonUps.AUCommand;
import edu.duke.ece568.ups.AmazonUps.AURequestPickup;
import edu.duke.ece568.ups.AmazonUps.AUReadyForDelivery;
import edu.duke.ece568.ups.AmazonUps.Err;

import edu.duke.ece568.ups.WorldUps.*;

public class ADeparser implements Runnable{
    BlockingQueue<AUCommand.Builder> queue;
    Database db;
    ArrayList<Long>ackList;
    HashSet<Long> recvSeq;
    Deparser deparser;
    ClientConnection conn;
    Executor exec;

    public ADeparser(Executor e, ClientConnection conn, BlockingQueue<AUCommand.Builder> queue, Database db){
        this.queue = queue;
        this.db = db;
        this.conn = conn;
        this.exec = e;

        ackList = new ArrayList<>();
        recvSeq = new HashSet<>();
        deparser = new Deparser(conn);
    }

    private synchronized void deparse(AUCommand.Builder resp){
        if(resp.getPickupRequestCount() > 0){
            for(AURequestPickup pickup : resp.getPickupRequestList()){
                //deparse pickup
                if(deparser.checkSeqNum(pickup.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
                
            }
        }

        if(resp.getDeliveryReadyCount() > 0){
            for(AUReadyForDelivery delivery : resp.getDeliveryReadyList()){
                //deparse delivery
                if(deparser.checkSeqNum(delivery.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
                try{
                    exec.execute(delivery);
                }
                catch(Exception e){}
            }
        }

        if(resp.getErrorCount() > 0){
            for(Err err : resp.getErrorList()){
                //deparse error
                if(deparser.checkSeqNum(err.getErrorSeqnum(), ackList, recvSeq)){
                    continue;
                }
            }
        }

        if(resp.getAcksCount() > 0){
            for(long ack : resp.getAcksList()){
                //deparse acks
            }
        }
    }

    @Override
    public synchronized void run(){        
        while(true){
            AUCommand.Builder resp;
            while ((resp = queue.poll()) != null) {
                ackList.clear();
                deparse(resp);
                try{
                    deparser.sendAcks_A(ackList);
                }
                catch(Exception e){

                }
            }
        }
    }

}
