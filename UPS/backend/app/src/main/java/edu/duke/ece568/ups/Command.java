package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.OutputStream;

import com.google.protobuf.GeneratedMessageV3;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class Command{
    OutputStream out;
    GeneratedMessageV3 message;
    long timeofSending;
    boolean isAcked;
    long seqNo;
    
    public <T extends GeneratedMessageV3> Command(OutputStream out, T msg, long seqNo){
        this.out = out;
        this.message = msg;
        this.isAcked = false;
        this.seqNo = seqNo;
      }
    
      public void sendMessage() throws IOException{
        if(!MessageTransmitter.sendMsgTo(message, out)){
          throw new IOException("Error when sending message to the world");
        }
        timeofSending = System.currentTimeMillis();
      }
    
      public boolean isTimeout(){
        long currentTime = System.currentTimeMillis();
        if((currentTime-timeofSending)>5000){
          return true;
        }
        return false;
      }
    
      public void checkAck() throws IOException{
        if(!isAcked){
          if(isTimeout()){
            sendMessage();
          }
        }
      }
    
      public void setAck(long ackNo) throws IOException{
        UCommands.Builder uCommand = UCommands.newBuilder();
        uCommand.addAcks(ackNo);
    
        if(!MessageTransmitter.sendMsgTo(uCommand.build(), out)){
          throw new IOException("Error when trying to send ack to world");
        }
        isAcked = true;
      }
    
}
