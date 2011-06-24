package com.squareup.shaneal.location;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import sun.nio.cs.Surrogate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

/**
 * User: smanek
 * <p/>
 * Just a utility class for the Location handler methods.
 */
public class LocationRequestHandlers {
  private static final JsonFactory FACTORY = new JsonFactory();
  private static final LocationStore STORE = MemoryLocationStore.getInstance();

  private LocationRequestHandlers() {
    throw new IllegalStateException("Don't instantiate me");
  }

  private static class JsonWriter {
    private final StringWriter writer;
    private final JsonGenerator generator;

    private JsonWriter() throws IOException {
      writer = new StringWriter();
      generator = FACTORY.createJsonGenerator(writer);
      generator.useDefaultPrettyPrinter();
    }

    public JsonGenerator getGenerator() {
      return generator;
    }

    public String getJson() throws IOException {
      generator.close();
      return writer.toString();
    }
    
  }

  public static final LocationRequestHandler GET_HANDLER = new LocationRequestHandler() {
    @Override
    public LocationResponse handle(String locId) throws IOException, StorageException {
      final JsonWriter writer = new JsonWriter();
      writer.getGenerator().writeStartObject();

      int responseCode = HttpServletResponse.SC_OK;

      if (locId == null) {
        writer.getGenerator().writeArrayFieldStart("locations");
        for (String loc : STORE.list()) {
          writer.getGenerator().writeString(loc);
        }
        writer.getGenerator().writeEndArray();
      } else {
        boolean present = STORE.contains(locId);
        writer.getGenerator().writeBooleanField("present", present);
        responseCode = present ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND;
      }

      writer.getGenerator().writeEndObject();

      return new LocationResponse(responseCode, writer.getJson());
    }
  };

  public static final LocationRequestHandler DELETE_HANDLER = new LocationRequestHandler() {
    @Override
    public LocationResponse handle(String locId) throws StorageException, IOException {
      STORE.remove(locId);
      final JsonWriter writer = new JsonWriter();
      writer.getGenerator().writeStartObject();

      writer.getGenerator().writeBooleanField("completed", true);
      writer.getGenerator().writeEndObject();

      return new LocationResponse(HttpServletResponse.SC_OK, writer.getJson());
    }
  };

  public static final LocationRequestHandler PUT_HANDLER = new LocationRequestHandler() {
    @Override
    public LocationResponse handle(String locId) throws IOException, StorageException {
      STORE.add(locId);

      final JsonWriter writer = new JsonWriter();
      
      writer.getGenerator().writeStartObject();
      writer.getGenerator().writeBooleanField("completed", true);
      writer.getGenerator().writeEndObject();

      return new LocationResponse(HttpServletResponse.SC_CREATED, writer.getJson());
    }
  };
}
