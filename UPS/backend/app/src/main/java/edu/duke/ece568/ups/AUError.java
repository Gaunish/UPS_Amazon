package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import edu.duke.ece568.ups.AmazonUps.*;
import edu.duke.ece568.ups.WorldUps.UCommands;

public class AUError implements Action{
    private Command cmd;
    private OutputStream out;
    private Err.Builder err;
    private long seqnum;
    
    public AUError(OutputStream out, long originno,long seqnum, String str){
        this.seqnum = seqnum;
        this.out = out;
        
        UACommand.Builder uacmd = UACommand.newBuilder();
        Err.Builder e = Err.newBuilder();
        e.setErrorInfo(str);
        e.setOriginSeqnum(originno);
        e.setErrorSeqnum(seqnum);
        uacmd.addError(e);

        this.cmd = new Command(out, uacmd.build(), seqnum); 
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
        aucommand.addError(err);
    }
    
    public String getType(){
      return "AUError";
    }

    public int getTruckid(){
      return -1;
    }

    public long getSeqnum(){
      return seqnum;
    }
}
