package edu.duke.ece568.ups;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import edu.duke.ece568.ups.WorldUps.UResponses;

public class UWReceiver implements Runnable {
  BlockingQueue<UResponses.Builder> queue;
  InputStream in;

  public UWReceiver(BlockingQueue<UResponses.Builder> queue, InputStream in) {
    this.queue = queue;
    this.in = in;
  }

  @Override
  public void run() {
    System.out.println("Started World listener");
    while (true) {
      try {
        UResponses.Builder resp = UResponses.newBuilder();
        if (MessageTransmitter.recvMsgFrom(resp, in)) {
          System.out.println("Recieved message from world");
          queue.put(resp);
        }
      } 
      catch (InterruptedException e){
        System.out.println(e.toString());
        break;
      }
    }
  }
}
