package com.squareup.shaneal.stats;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: smanek
 *
 * Keeps track of the stats (rolling averages on response codes).
 */
public class LocationStats {
  private static final LocationStats INSTANCE = new LocationStats();

  private final Map<ResponseType, RollingAverage> perResponseRollingStats;

  public static LocationStats getInstance() {
    return INSTANCE;
  }

  public LocationStats() {
    perResponseRollingStats = new EnumMap<ResponseType, RollingAverage>(ResponseType.class);

    for (ResponseType responseType : ResponseType.values()) {
      perResponseRollingStats.put(responseType, RollingAverage.newRollingAverage(15, TimeUnit.MINUTES));
    }
  }

  public void observe(int code, long time) {
    perResponseRollingStats.get(ResponseType.get(code)).observe(time);
  }

  public Map<ResponseType, RollingAverage> getPerResponseRollingStats() {
    return Collections.unmodifiableMap(perResponseRollingStats);
  }
}
