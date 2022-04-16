/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.duke.ece568.ups.WorldAmazon.AConnect;
import edu.duke.ece568.ups.WorldAmazon.AConnected;
import edu.duke.ece568.ups.WorldAmazon.AInitWarehouse;
import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UConnect;
import edu.duke.ece568.ups.WorldUps.UConnected;
import edu.duke.ece568.ups.WorldUps.UFinished;
import edu.duke.ece568.ups.WorldUps.UGoPickup;
import edu.duke.ece568.ups.WorldUps.UInitTruck;
import edu.duke.ece568.ups.WorldUps.UResponses;

public class App {
  public static void UWsendAck(ArrayList<Long> acks,OutputStream out) {
    UCommands.Builder uCommand = UCommands.newBuilder();
    for (int i = 0; i < acks.size(); i++) {
      uCommand.addAcks(acks.get(i));
    }
    MessageTransmitter.sendMsgTo(uCommand.build(), out);
  }

  public static long initWorld(InputStream in,OutputStream out) {

    UConnect.Builder connect = UConnect.newBuilder();
    connect.setIsAmazon(false);
    for (int i = 0; i < 100; i++) {
      UInitTruck.Builder truck = UInitTruck.newBuilder();
      truck.setId(i);
      truck.setX(1);
      truck.setY(1);
      connect.addTrucks(truck);
    }
    MessageTransmitter.sendMsgTo(connect.build(), out);
    UConnected.Builder resp = UConnected.newBuilder();
    MessageTransmitter.recvMsgFrom(resp,in);
    System.out.println("world id: " + resp.getWorldid());
    System.out.println("result: " + resp.getResult());
    return resp.getWorldid();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    long worldid;
    BlockingQueue<UResponses.Builder> queue = new LinkedBlockingQueue<UResponses.Builder>(30);
    ClientConnection worldConnection = new ClientConnection("localhost", 12345);
    UWReceiver listener = new UWReceiver(queue, worldConnection.getInputStream());
    Thread t = new Thread(listener);

    //Connect to database
    Database database = new Database(); 
    database.connectDB();

    //init for ups side - initialize 100 trucks and create a new world
    worldid = initWorld(worldConnection.getInputStream(),worldConnection.getOutputStream());

    ClientConnection WAConnection = new ClientConnection("localhost", 23456);
    AInitWarehouse.Builder warehouse1 = AInitWarehouse.newBuilder();
    warehouse1.setId(1);
    warehouse1.setX(5);
    warehouse1.setY(5);

    AConnect.Builder Aconnect = AConnect.newBuilder();
    Aconnect.setWorldid(worldid);
    Aconnect.addInitwh(warehouse1);
    Aconnect.setIsAmazon(true);

    MessageTransmitter.sendMsgTo(Aconnect.build(), WAConnection.getOutputStream());
    AConnected.Builder aconnected = AConnected.newBuilder();
    MessageTransmitter.recvMsgFrom(aconnected, WAConnection.getInputStream());
    System.out.println("worldID: " + aconnected.getWorldid());
    System.out.println("result: " + aconnected.getResult());

    UGoPickup.Builder goPickup = UGoPickup.newBuilder();
    goPickup.setTruckid(1);
    goPickup.setWhid(1);
    goPickup.setSeqnum(10);

    t.start();
    UCommands.Builder uCommand = UCommands.newBuilder();
    uCommand.addPickups(goPickup);
    MessageTransmitter.sendMsgTo(uCommand.build(), worldConnection.getOutputStream());
    while (true) {
      UResponses.Builder Uresp;
      while ((Uresp = queue.poll()) != null) {
        ArrayList<Long> acks = new ArrayList<Long>();
        if (Uresp.getCompletionsCount() > 0) {
          UFinished.Builder finished = Uresp.getCompletionsBuilder(0);
          System.out.println("Truck id is: " + finished.getTruckid());
          System.out.println("Status is: " + finished.getStatus());
        }
        for (int i = 0; i < Uresp.getAcksCount(); i++) {
          System.out.println("Ack is : " + Uresp.getAcks(i));
          acks.add(Uresp.getAcks(i));
        }
        UWsendAck(acks, worldConnection.getOutputStream());
      }
    }
  }
}
