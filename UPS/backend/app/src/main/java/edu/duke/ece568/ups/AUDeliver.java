package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAReadyForPickup;
import edu.duke.ece568.ups.AmazonUps.UAPackageDelivered;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class AUDeliver implements Action{
    private Command cmd;
    private OutputStream out;
    private UAPackageDelivered.Builder delivered;

  public AUDeliver(OutputStream out, long packageid,long seqnum){
    UAPackageDelivered.Builder delivered = UAPackageDelivered.newBuilder();
    delivered.setPackageid(packageid);
    delivered.setSeqnum(seqnum);
    this.delivered = delivered;
    this.out = out;
    
    UACommand.Builder uaCommand = UACommand.newBuilder();
    uaCommand.addPackageDelivered(delivered);

    this.cmd = new Command(out,uaCommand.build(),seqnum);
  }
    
    public void sendMessage() throws IOException{
        cmd.sendMessage();
    }

    public boolean checkAck() throws IOException{
      return cmd.isAcked;
    }

    public void setAck() throws IOException{
      cmd.isAcked = true;
    }

    public void append(UCommands.Builder ucommand){
        return;
    }

    public void append(UACommand.Builder aucommand){
        aucommand.addPackageDelivered(delivered);
    }
    
    public String getType(){
      return "AUDeliver";
    }

    public int getTruckid(){
      return -1;
    }
}
