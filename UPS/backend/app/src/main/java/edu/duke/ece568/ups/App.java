/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import edu.duke.ece568.ups.WorldAmazon.AConnect;
import edu.duke.ece568.ups.WorldAmazon.AConnected;
import edu.duke.ece568.ups.WorldAmazon.AInitWarehouse;
import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UConnect;
import edu.duke.ece568.ups.WorldUps.UConnected;
import edu.duke.ece568.ups.WorldUps.UInitTruck;
import edu.duke.ece568.ups.WorldUps.UResponses;

import edu.duke.ece568.ups.AmazonUps.AUCommand;
import edu.duke.ece568.ups.AmazonUps.AUConnected;
import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAConnect;   

public class App {
  private static BlockingQueue<UResponses.Builder> worldQueue;
  private static BlockingQueue<AUCommand.Builder> amazonQueue;
  private static long worldId;
  private static ConcurrentHashMap<Long,Action>worldActions, amazonActions;
  private static ClientConnection amazonConnection, worldConnection;
  private static Database db;
  private static volatile long worldSeqno, amznSeqno;
  private static Executor executor;

  private static void init(){
    worldQueue = new LinkedBlockingQueue<UResponses.Builder>(100);
    amazonQueue = new LinkedBlockingQueue<AUCommand.Builder>(100);
    worldActions = new ConcurrentHashMap<Long,Action>();
    amazonActions = new ConcurrentHashMap<Long,Action>();
    worldSeqno = 0;
    amznSeqno = 0;
    db = new Database();
  }

  private static void connect() throws IOException{
    db.connectDB();
    worldConnection = new ClientConnection("vcm-24690.vm.duke.edu", 12345);
    System.out.println("Connected to world server");
    amazonConnection = new ClientConnection("vcm-24690.vm.duke.edu", 6666);
    System.out.println("Connected to amazon server");
    executor = new Executor(db, worldConnection, amazonConnection, worldActions, amazonActions, worldSeqno, amznSeqno);
    
    //init for ups side - initialize 100 trucks and create a new world
    worldId = initWorld(db, worldConnection.getInputStream(),worldConnection.getOutputStream());
    System.out.println("World is init");
  }

  public static long initWorld(Database db,InputStream in,OutputStream out) {
    UConnect.Builder connect = UConnect.newBuilder();
    connect.setIsAmazon(false);
    String sql = "";
    for (int i = 0; i < 100; i++) {
      UInitTruck.Builder truck = UInitTruck.newBuilder();
      truck.setId(i);
      truck.setX(1);
      truck.setY(1);
      sql = "INSERT INTO TRUCK VALUES("+i+", -1, \'IDLE\', 1, 1);";
      db.executeStatement(sql, "Error");
      connect.addTrucks(truck);
    }
    MessageTransmitter.sendMsgTo(connect.build(), out);
    UConnected.Builder resp = UConnected.newBuilder();
    MessageTransmitter.recvMsgFrom(resp,in);
    //System.out.println("world id: " + resp.getWorldid());
    //System.out.println("result: " + resp.getResult());
    return resp.getWorldid();
  }

  private static void connectAmazon() throws IOException{
    UAConnect.Builder UAconnect = UAConnect.newBuilder();
    UAconnect.setWorldid(worldId);
    UAconnect.setSeqnum(0);
    System.out.println("Sending connect to amazon");
    MessageTransmitter.sendMsgTo(UAconnect.build(), amazonConnection.getOutputStream());
    System.out.println("Sent connect to amazon");
    AUConnected.Builder auconnected = AUConnected.newBuilder();
    MessageTransmitter.recvMsgFrom(auconnected, amazonConnection.getInputStream());
    //System.out.println("worldID: " + aconnected.getWorldid());
    //System.out.println("result: " + aconnected.getResult());
  }

  private static void run() throws IOException{
    //World deparser
    WDeparser world_deparser = new WDeparser(executor, worldConnection, worldQueue, db);
    Thread world_d = new Thread(world_deparser);
    world_d.start();

    //Amazon deparser
    ADeparser amzn_deparser = new ADeparser(executor, amazonConnection, amazonQueue, db);
    Thread world_a = new Thread(amzn_deparser);
    world_a.start();

    
    //World listening queue
    UWReceiver world_listener = new UWReceiver(worldQueue, worldConnection.getInputStream());
    Thread world_l = new Thread(world_listener);
    world_l.start();

    //Amazon Listening queue
    UAListener amzn_listen = new UAListener(amazonQueue, amazonConnection.getInputStream());
    Thread amzn_l = new Thread(amzn_listen);
    amzn_l.start();

  }

  public static void main(String[] args) throws IOException, InterruptedException {
    init();
    connect();
    connectAmazon();
    run();
    while(true){
    }
  }
}
