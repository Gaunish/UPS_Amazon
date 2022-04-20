package edu.duke.ece568.ups;

import java.io.IOException;

import edu.duke.ece568.ups.AmazonUps.UACommand;
import edu.duke.ece568.ups.WorldUps.UCommands;

public interface Action {
    public void sendMessage() throws IOException;
    public boolean checkAck() throws IOException;
    public void setAck() throws IOException;
    public String getType();
    public void append(UCommands.Builder ucommand);
    public void append(UACommand.Builder aucommand);
    public long getSeqnum();
    public int getTruckid();
}
