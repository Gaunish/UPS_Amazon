package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.OutputStream;

import com.google.protobuf.GeneratedMessageV3;


import edu.duke.ece568.ups.WorldUps.UCommands;

public interface Action {
    public void sendMessage() throws IOException;
    public boolean isTimeout();
    public boolean checkAck() throws IOException;
    public void setAck() throws IOException;
    public void append(UCommands.Builder ucommand);
}
