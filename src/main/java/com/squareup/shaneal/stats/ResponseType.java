package com.squareup.shaneal.stats;

import java.util.HashMap;
import java.util.Map;

/**
 * User: smanek
 * <p/>
 * Represents the various types of response codes.
 */
public enum ResponseType {
  ONE_HUNDRED(100),
  TWO_HUNDRED(200),
  THREE_HUNDRED(300),
  FOUR_HUNDRED(400),
  FIVE_HUNDRED(500),
  UNRECOGNIZED(0, "Unknown");

  private final int code;
  private final String printableName;
  private static final Map<Integer, ResponseType> LOOKUP_TABLE;

  static {
    LOOKUP_TABLE = new HashMap<Integer, ResponseType>(ResponseType.values().length);
    for (ResponseType type : ResponseType.values()) {
      LOOKUP_TABLE.put(type.getCode(), type);
    }
  }

  public String getPrintableResponseFamily() {
    return printableName;
  }

  private ResponseType(int code) {
    this(code, Integer.toString(code).substring(0, 1) + "xx");
  }

  private ResponseType(int code, String name) {
    this.code = code;
    this.printableName = name;
  }

  private int getCode() {
    return code;
  }

  public static ResponseType get(int code) {
    if (code < 100 || code >= 600) {
      return UNRECOGNIZED;
    } else {
      return LOOKUP_TABLE.get((code / 100) * 100);
    }
  }
}