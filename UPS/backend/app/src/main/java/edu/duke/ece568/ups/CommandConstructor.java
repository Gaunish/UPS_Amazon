package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;

import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UDeliveryLocation;
import edu.duke.ece568.ups.WorldUps.UGoDeliver;
import edu.duke.ece568.ups.WorldUps.UGoPickup;

public class CommandConstructor {
  
  public static Command constructUGoPickup(OutputStream out, int truckid,int whid, long seqnum){
    UGoPickup.Builder goPickup = UGoPickup.newBuilder();
    goPickup.setTruckid(truckid);
    goPickup.setWhid(whid);
    goPickup.setSeqnum(seqnum);

    UCommands.Builder uCommand = UCommands.newBuilder();
    uCommand.addPickups(goPickup);

    return new Command(out,uCommand.build(),seqnum);
  }

  public static Command constructUGoDeliver(OutputStream out, int truckid, ArrayList<UDeliveryLocation> locations, long seqnum){
    UGoDeliver.Builder goDeliver = UGoDeliver.newBuilder();
    goDeliver.setTruckid(truckid);
    goDeliver.setSeqnum(seqnum);
    for(int i=0;i<locations.size();i++){
      goDeliver.addPackages(locations.get(i));
    }

    UCommands.Builder uCommand = UCommands.newBuilder();
    uCommand.addDeliveries(goDeliver);
    return new Command(out,uCommand.build(),seqnum);
  }
}
