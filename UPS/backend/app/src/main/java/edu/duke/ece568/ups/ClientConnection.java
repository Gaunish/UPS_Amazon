package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientConnection implements Connection {
  private Socket clientSocket;
   /**
   * Start a socket connecting to the client with the specified hostname and port
   * number
   * 
   * @param ip   is the hostname of the server we want to connect to
   * @param port is the port number of the listening port of the server
   */
  public ClientConnection(String ip, int port) throws IOException {
    clientSocket = new Socket(ip, port);
  }

  public InputStream getInputStream() throws IOException{
    return clientSocket.getInputStream();
  }

  public OutputStream getOutputStream() throws IOException{
    return clientSocket.getOutputStream();
  }

  /**
   * Close all connection with the server
   */
  public void stop() throws IOException {
    clientSocket.close();
  }
}
