package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.OutputStream;

import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAIsAssociated;
import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UCommands.Builder;

public class IsAssociated implements Action {
  private Command cmd;
  private OutputStream out;

  public IsAssociated(OutputStream out, long packageid,boolean ismatched,long seqnum){
    UAIsAssociated.Builder isAssociated = UAIsAssociated.newBuilder();
    isAssociated.setPackageid(packageid);
    isAssociated.setCheckResult(ismatched);
    UACommand.Builder uaCommand = UACommand.newBuilder();
    uaCommand.addLinkResult(isAssociated);

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
  public void append(Builder ucommand) {
    // TODO Auto-generated method stub
    
  }

}
