package edu.duke.ece568.ups;

import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;

import edu.duke.ece568.ups.AmazonUps.*;
import edu.duke.ece568.ups.WorldUps.*;
import edu.duke.ece568.ups.UpsWeb.*;

public class WebDeparser implements Runnable{
    BlockingQueue<WebAddress.Builder> queue;
    Database db;
    HashSet<Long> recvSeq;
    ClientConnection conn;

    public WebDeparser(ClientConnection conn, BlockingQueue<WebAddress.Builder> queue, Database db){
        this.queue = queue;
        this.db = db;
        this.conn = conn;

        recvSeq = new HashSet<>();
    }

    private synchronized void deparse(WebAddress.Builder resp){
        int package_id = resp.getPackageid();
        int x = resp.getX();
        int y = resp.getY();
        long seqnum = resp.getSeqnum();
        //Send ack
        try{
            sendAck(seqnum);
        }
        catch(Exception e){}

        String q = "UPDATE PACKAGE SET X = "+ x +" AND Y = "+ y +" WHERE PACKAGE_ID = "+ package_id +";";
        db.executeStatement(q, "failure");
    }

    private synchronized void sendAck(long seqNum) throws IOException{
        WebAck.Builder cmd = WebAck.newBuilder();
        MessageTransmitter.sendMsgTo(cmd.build(), conn.getOutputStream());
    }

    @Override
    public synchronized void run(){
        while(true){
            WebAddress.Builder resp = WebAddress.newBuilder();
            while ((resp = queue.poll()) != null) {
                deparse(resp);            
            }
        }
    }

}
