package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.OutputStream;

import com.google.protobuf.GeneratedMessageV3;


import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.AmazonUps.UACommand;

public interface Action {
    public void sendMessage() throws IOException;
    public boolean checkAck() throws IOException;
    public void setAck() throws IOException;
    public String getType();
    public void append(UCommands.Builder ucommand);
    public void append(UACommand.Builder aucommand);

    public int getTruckid();
}
