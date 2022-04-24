package edu.duke.ece568.ups;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;

import edu.duke.ece568.ups.AmazonUps.*;

public class UAListener implements Runnable{
    BlockingQueue<AUCommand.Builder> queue;
    InputStream in;

    public UAListener(BlockingQueue<AUCommand.Builder> queue, InputStream in) {
      this.queue = queue;
      this.in = in;
    }

    @Override
    public void run() {
        System.out.println("Started amazon listener");
        //Forever listening port
        while(true){
            try{
                AUCommand.Builder recv = AUCommand.newBuilder();
                if (MessageTransmitter.recvMsgFrom(recv, in)) {
                    queue.put(recv);
                }
            }
            catch (InterruptedException e){
                System.out.println(e.toString());
                break;
            }
        }
    }
    
}
