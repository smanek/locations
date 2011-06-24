package com.squareup.shaneal.location;

/**
 * User: smanek
 *
 * Represent the response to a location query.
 */
public class LocationResponse {
  private final int code;
  private final String content;

  public LocationResponse(int code, String content) {
    this.code = code;
    this.content = content;
  }

  public int getCode() {
    return code;
  }

  public String getContent() {
    return content;
  }
}
