package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAIsAssociated;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class AUassoc {
    private Command cmd;
    private OutputStream out;
    private UAIsAssociated.Builder assoc;
    
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
