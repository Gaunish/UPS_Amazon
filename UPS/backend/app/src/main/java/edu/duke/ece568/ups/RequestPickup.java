package edu.duke.ece568.ups;

import java.io.OutputStream;
import java.util.ArrayList;

import edu.duke.ece568.ups.AmazonUps.*;
import edu.duke.ece568.ups.AmazonUps.AURequestPickup;
import edu.duke.ece568.ups.AmazonUps.UAIsAssociated;

public class RequestPickup {
  Database db;
  OutputStream out;

  public RequestPickup(Database db, OutputStream out){
    this.db = db;
    this.out = out;
  }

  public UAIsAssociated performPickup(AURequestPickup request){
    //two options: send right away, or return a UAISAssociated
    
    /********************extract info*****************/
    AUPack aupack =request.getPack();
    APack apack = aupack.getPackage();
    long packageid = apack.getShipid();
    int whid = apack.getWhnum();
    databaseActions();
    return formMessage();
  }

  public void databaseActions(){
    //store package id and products into database
    //query Truck table to see if there's already a truck on the way
    //assign the truck id to package id correspondingly
  }

  public UAIsAssociated formMessage(){
    //check whether we can link the product with the username
    //form the message whether the product can be linked to the account
    return null;
  }
}
