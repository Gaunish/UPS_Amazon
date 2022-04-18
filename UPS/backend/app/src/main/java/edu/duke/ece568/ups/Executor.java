package edu.duke.ece568.ups;

import edu.duke.ece568.ups.AmazonUps.AUReadyForDelivery;
import edu.duke.ece568.ups.AmazonUps.AURequestPickup;
import edu.duke.ece568.ups.AmazonUps.Err;
import java.io.IOException;
import java.util.HashMap;

public class Executor {
    Database db;
    ClientConnection WConn, Aconn;
    HashMap<Long,Action> W_actions, A_actions; 

    public Executor(Database db, ClientConnection WConn, ClientConnection AConn, HashMap<Long,Action> W_actions, HashMap<Long,Action> A_actions){
        this.db = db;
        this.Aconn = AConn;
        this.WConn = WConn;
        this.W_actions = W_actions;
        this.A_actions = A_actions;
    }

    public void execute(AURequestPickup pickup){
        //RequestPickup p = new RequestPickup(pickup);
        //p.performActions();
    }

    public void execute(AUReadyForDelivery delivery) throws IOException {
        int truck_id = delivery.getTruckid();
        long seqnum = delivery.getSeqnum();
        Deliver d = new Deliver(db, WConn.getOutputStream(), truck_id, seqnum);
        d.sendMessage();
        //String update_q = "UPDATE TRUCK SET STATUS = ";
    }

    public void execute(Err errA) throws IOException {
        long origin_seqno = errA.getOriginSeqnum();
        long err_seqno = errA.getErrorSeqnum();
        Action a = A_actions.get(origin_seqno);
        /*if(a.getType() == "AUDeliver"){

        }
        else if(a.getType() == "AUPickup"){
            
        }
        else if(a.getType() == "AUAssoc"){

        }*/

    }
    
}
