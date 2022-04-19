package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.OutputStream;

import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAPackageDelivered;
import edu.duke.ece568.ups.WorldUps.UCommands.Builder;

public class Delivered implements Action {
private Command cmd;
  UAPackageDelivered.Builder delivered;

  public Delivered(OutputStream out, long packageid,long seqnum){
    UAPackageDelivered.Builder delivered = UAPackageDelivered.newBuilder();
    delivered.setPackageid(packageid);
    delivered.setSeqnum(seqnum);
    this.delivered = delivered;
    
    UACommand.Builder uaCommand = UACommand.newBuilder();
    uaCommand.addPackageDelivered(delivered);

    this.cmd = new Command(out,uaCommand.build(),seqnum);
  }
  
  @Override
  public void sendMessage() throws IOException {
    cmd.sendMessage();
    
  }

  @Override
  public boolean checkAck() throws IOException {
    return cmd.isAcked;
  }

  @Override
  public void setAck() throws IOException {
    cmd.isAcked = true;
    
  }

  @Override
  public String getType() {
    return "Delivered";
  }

  @Override
  public void append(Builder ucommand) {
  }

  @Override
  public void append(edu.duke.ece568.ups.AmazonUps.UACommand.Builder ucommand) {
    ucommand.addPackageDelivered(delivered);
    
  }

}
