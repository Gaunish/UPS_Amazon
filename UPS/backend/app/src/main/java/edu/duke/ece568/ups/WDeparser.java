package edu.duke.ece568.ups;

import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;

import edu.duke.ece568.ups.AmazonUps.*;
import edu.duke.ece568.ups.WorldUps.*;

public class WDeparser implements Runnable{
    BlockingQueue<UResponses.Builder> queue;
    Database db;
    ArrayList<Long>ackList;
    HashSet<Long> recvSeq;
    ClientConnection conn;
    Deparser deparser;
    Executor executor;

    public WDeparser(Executor e, ClientConnection conn, BlockingQueue<UResponses.Builder> queue, Database db){
        this.queue = queue;
        this.db = db;
        this.conn = conn;
        this.executor = e;

        ackList = new ArrayList<>();
        recvSeq = new HashSet<>();
        deparser = new Deparser(conn);
    }

    private synchronized void deparse(UResponses.Builder resp){
        if(resp.getCompletionsCount() > 0){
            for(UFinished comp : resp.getCompletionsList()){
                //deparse pickup
                if(deparser.checkSeqNum(comp.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
                try{
                    executor.execute(comp);
                }
                catch(Exception e){}
            }
        }

        if(resp.getDeliveredCount() > 0){
            for(UDeliveryMade deliver : resp.getDeliveredList()){
                //deparse pickup
                if(deparser.checkSeqNum(deliver.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
                try{
                    executor.execute(deliver);
                }
                catch(Exception e){}
            }
        }

        if(resp.getTruckstatusCount() > 0){
            for(UTruck truck : resp.getTruckstatusList()){
                //deparse pickup
                if(deparser.checkSeqNum(truck.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
                try{
                    executor.execute(truck);
                }
                catch(Exception e){}
            }
        }

        if(resp.getErrorCount() > 0){
            for(UErr err : resp.getErrorList()){
                //deparse pickup
                if(deparser.checkSeqNum(err.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
                try{
                    executor.execute(err);
                }
                catch(Exception e){}
                
            }
        }

        if(resp.getAcksCount() > 0){
            for(long acks : resp.getAcksList()){
                //deparse acks
                try{
                    executor.execute(acks, true);
                }
                catch(Exception e){}
    
            }
        }

        if(resp.hasFinished()){
            boolean finished = resp.getFinished();
            //TODO : Implement finished
        }
    }

    @Override
    public synchronized void run(){
        while(true){
            UResponses.Builder resp;
            while ((resp = queue.poll()) != null) {
                ackList.clear();
                deparse(resp);
                try{
                    deparser.sendAcks_W(ackList);
                }
                catch(Exception e){

                }
            }
        }
    }

}
