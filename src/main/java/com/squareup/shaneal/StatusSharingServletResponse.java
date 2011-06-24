package com.squareup.shaneal;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * User: smanek
 * <p/>
 * Servlet's HttpServletResponse doesn't expose the status code that's set,
 * so we have to do this.
 */
public class StatusSharingServletResponse extends HttpServletResponseWrapper {

  private int httpStatus = HttpServletResponse.SC_OK;

  public StatusSharingServletResponse(HttpServletResponse response) {
    super(response);
  }

  @Override
  public void sendError(int sc) throws IOException {
    httpStatus = sc;
    super.sendError(sc);
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {
    httpStatus = sc;
    super.sendError(sc, msg);
  }


  @Override
  public void setStatus(int sc) {
    httpStatus = sc;
    super.setStatus(sc);
  }

  public int getStatus() {
    return httpStatus;
  }
}
