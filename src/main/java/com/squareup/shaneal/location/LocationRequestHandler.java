package com.squareup.shaneal.location;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;

/**
 * User: smanek
 * <p/>
 * A class that handles a particular kind of HTTP request.
 */
public interface LocationRequestHandler {
  LocationResponse handle(String locId) throws StorageException, IOException;
}
