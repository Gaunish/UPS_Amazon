package edu.duke.ece568.ups;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Connection {
  public InputStream getInputStream() throws IOException;

  public OutputStream getOutputStream() throws IOException;

  public void stop() throws IOException;
}
