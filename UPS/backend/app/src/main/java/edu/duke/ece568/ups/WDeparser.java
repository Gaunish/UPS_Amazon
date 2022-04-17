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

    public WDeparser(ClientConnection conn, BlockingQueue<UResponses.Builder> queue, Database db){
        this.queue = queue;
        this.db = db;
        this.conn = conn;

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
                //executor.execute(comp);
            }
        }

        if(resp.getDeliveredCount() > 0){
            for(UDeliveryMade deliver : resp.getDeliveredList()){
                //deparse pickup
                if(deparser.checkSeqNum(deliver.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
            }
        }

        if(resp.getTruckstatusCount() > 0){
            for(UTruck truck : resp.getTruckstatusList()){
                //deparse pickup
                if(deparser.checkSeqNum(truck.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
            }
        }

        if(resp.getErrorCount() > 0){
            for(UErr err : resp.getErrorList()){
                //deparse pickup
                if(deparser.checkSeqNum(err.getSeqnum(), ackList, recvSeq)){
                    continue;
                }
            }
        }

        if(resp.hasFinished()){
            boolean finished = resp.getFinished();
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
