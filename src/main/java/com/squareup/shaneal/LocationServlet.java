package com.squareup.shaneal;

import com.squareup.shaneal.location.LocationRequestHandler;
import com.squareup.shaneal.location.LocationRequestHandlers;
import com.squareup.shaneal.location.LocationResponse;
import com.squareup.shaneal.location.StorageException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: smanek
 * <p/>
 * Provides the Location REST service.
 */
public class LocationServlet extends HttpServlet {

  // route all the different supported request methods to their appropriate handler
  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    route(LocationRequestType.PUT, req, resp);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    route(LocationRequestType.DELETE, req, resp);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    route(LocationRequestType.GET, request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST Method not supported for Location Servlet");
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HEAD Method not supported for Location Servlet");
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "OPTIONS Method not supported for Location Servlet");
  }

  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "TRACE Method not supported for Location Servlet");
  }


  private void route(LocationRequestType type, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    LocationResponse locResp;
    final String location = getLocation(request.getPathInfo());

    if (location == null && !type.takesNullLocId()) {
      locResp = new LocationResponse(HttpServletResponse.SC_BAD_REQUEST, "Must specify a location Id");
    } else {
      try {
        locResp = type.getHandler().handle(location);
      } catch (StorageException e) {
        locResp = new LocationResponse(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Storage Error");
      }
    }

    response.setStatus(locResp.getCode());
    response.getWriter().write(locResp.getContent());
  }

  private static String getLocation(String path) {
    if (path == null) {
      return null;
    }

    // trim the leading forward slash
    path = path.substring(1);

    if (path.length() == 0) {
      return null;
    }

    return path;
  }

  /**
   * Represents HTTP handlers for the location methods.
   */
  public static enum LocationRequestType {

    GET(true, LocationRequestHandlers.GET_HANDLER),
    DELETE(false, LocationRequestHandlers.DELETE_HANDLER),
    PUT(false, LocationRequestHandlers.PUT_HANDLER);

    private final boolean takesNullLocId;
    private final LocationRequestHandler handler;

    LocationRequestType(boolean takesNullLocId, LocationRequestHandler handler) {
      this.takesNullLocId = takesNullLocId;
      this.handler = handler;
    }

    public boolean takesNullLocId() {
      return takesNullLocId;
    }

    public LocationRequestHandler getHandler() {
      return handler;
    }
  }
}
