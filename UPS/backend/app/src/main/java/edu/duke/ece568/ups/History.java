package edu.duke.ece568.ups;

import java.sql.Timestamp;
import java.sql.ResultSet;

import edu.duke.ece568.ups.WorldUps.UCommands;
import edu.duke.ece568.ups.WorldUps.UQuery;
import java.io.IOException;
import java.io.OutputStream;

public class History {
    public void updateHistory(Database db, long packageid, int truckid, String str){
        
        String truck_q = "SELECT * FROM TRUCK WHERE TRUCK_ID = " + truckid + ";";
        ResultSet truckstatus = db.SelectStatement(truck_q);
        int x = 1, y = 1;
        try{
            x = truckstatus.getInt("X");
            y = truckstatus.getInt("Y");
        }
        catch(Exception e){}

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String history = "INSERT INTO HISTORY VALUES(" + packageid + ",\'" + str + "\', " + x + ", " + y + "," + timestamp + ");";
        db.executeStatement(history, "failure");
    }
    
    public void sendQuery(OutputStream out, int truckid, long worldseqnum){
        Query query = new Query(out, truckid, worldseqnum);
        try{
            query.sendMessage();
        }
        catch(Exception e){}
    }
}
