package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

import edu.duke.ece568.ups.AmazonUps.APack;
import edu.duke.ece568.ups.AmazonUps.AUCommand;
import edu.duke.ece568.ups.AmazonUps.AUPack;
import edu.duke.ece568.ups.AmazonUps.AURequestPickup;

public class ADeparser {
  BlockingQueue<AUCommand.Builder> queue;
  Database db;
  ArrayList<Long>acklist;
  HashSet<Long> receivedseq;
  

  public ADeparser(BlockingQueue<AUCommand.Builder> queue, Database db,HashSet<Long> receivedseq,OutputStream out){
    this.queue = queue;
    this.db = db;
    this.receivedseq = receivedseq;
    acklist = new ArrayList<Long>();
  }

  public void parseMessage(){
    AUCommand.Builder aResp;
    if((aResp = queue.poll()) != null){
      for(int i = 0;i<aResp.getPickupRequestCount();i++){
        performPickupRequest(aResp.getPickupRequest(i));
        acklist.add(aResp.getPickupRequest(i).getSeqnum());
      }
    }
  }

  public void performPickupRequest(AURequestPickup request){
    AUPack aupack =request.getPack();
    acklist.add(request.getSeqnum());
    APack apack = aupack.getPackage();
    long packageid = apack.getShipid();
    int whid = apack.getWhnum();
    //check whether a new truck is needed
    int truckid;
    //relate package id with the truckid in database
    String accountName = aupack.getUpsAccount();
    //link account by name
    for(int i=0;i<apack.getThingsCount();i++){
      //add product to database
    }
  }
}
