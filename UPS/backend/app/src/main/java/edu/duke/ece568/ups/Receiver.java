package edu.duke.ece568.ups;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import edu.duke.ece568.ups.WorldUps.UResponses;

public class Receiver implements Runnable {
  BlockingQueue<UResponses.Builder> queue;
  InputStream in;

  public Receiver(BlockingQueue<UResponses.Builder> queue, InputStream in) {
    this.queue = queue;
    this.in = in;
  }

  @Override
  public void run() {
    while (true) {
      try {
        UResponses.Builder resp = UResponses.newBuilder();
        if (MessageTransmitter.recvMsgFrom(resp, in)) {
          queue.put(resp);
        }
      } 
      catch (InterruptedException e){
        break;
      }
    }
  }
}
