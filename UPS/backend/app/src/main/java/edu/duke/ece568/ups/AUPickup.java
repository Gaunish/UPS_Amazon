package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.AmazonUps.UAReadyForPickup;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class AUPickup implements Action{
    private Command cmd;
    private OutputStream out;
    private UAReadyForPickup.Builder pickup;
    
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
        aucommand.addPickupReady(pickup);
    }
    
    public String getType(){
      return "AUPickup";
    }
}
