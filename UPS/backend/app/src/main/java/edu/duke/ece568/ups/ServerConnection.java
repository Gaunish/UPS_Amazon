package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection implements Connection{
  private ServerSocket listeningSocket;
  private Socket connectionSocket;

  public ServerConnection(int port) throws IOException{
    listeningSocket = new ServerSocket(port);
    connectionSocket = listeningSocket.accept();
  }
  
  @Override
  public InputStream getInputStream() throws IOException {
    return connectionSocket.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return connectionSocket.getOutputStream();
  }

  @Override
  public void stop() throws IOException {
    connectionSocket.close();
    listeningSocket.close();
  }

}
