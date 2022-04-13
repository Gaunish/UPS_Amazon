package edu.duke.ece568.ups;

import java.io.IOException;
import edu.duke.ece568.ups.WorldAmazon;
import edu.duke.ece568.ups.WorldAmazon.AInitWarehouse;
public class MockAmazon {
  public static void run() {
    try {
      ClientConnection worldConnection = new ClientConnection("localhost", 23456);
      AInitWarehouse.Builder warehouse1 = AInitWarehouse.newBuilder();
      warehouse1.setId(1);
      warehouse1.setX(5);
      warehouse1.setY(5);
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
