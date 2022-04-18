package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAIsAssociated;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class AUassoc implements Action {
    private Command cmd;
    private OutputStream out;
    private UAIsAssociated.Builder assoc;

  public AUassoc(OutputStream out, long packageid,boolean ismatched,long seqnum){
    UAIsAssociated.Builder isAssociated = UAIsAssociated.newBuilder();
    isAssociated.setPackageid(packageid);
    isAssociated.setCheckResult(ismatched);
    this.assoc = isAssociated;
    UACommand.Builder uaCommand = UACommand.newBuilder();
    uaCommand.addLinkResult(isAssociated);

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
        aucommand.addLinkResult(assoc);
    }
    
    public String getType(){
      return "AUAssoc";
    }
}
