package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.google.protobuf.GeneratedMessageV3;

import edu.duke.ece568.ups.WorldUps.UCommands;

public class Command{
    OutputStream out;
    GeneratedMessageV3 message;
    long timeofSending;
    public boolean isAcked;
    long seqNo;
    
    public <T extends GeneratedMessageV3> Command(OutputStream out, T msg, long seqNo){
        this.out = out;
        this.message = msg;
        this.isAcked = false;
        this.seqNo = seqNo;
      }
    
      public void sendMessage() throws IOException{
        Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                  if(isAcked){
                    timer.cancel();
                  }
                  else{
                  System.out.println("Sqenum "+seqNo+": Sending Message to world");
                    MessageTransmitter.sendMsgTo(message, out);
                  }
                }
              }, 0, 5000);//1s timeout
      }
    
}

