package com.squareup.shaneal;

import com.squareup.shaneal.stats.LocationStats;
import com.squareup.shaneal.stats.ResponseType;
import com.squareup.shaneal.stats.RollingAverage;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.PrettyPrinter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * User: smanek
 *
 * Displays moving averages of families of return codes for the LocationServlet.
 */
public class MetricsServlet extends HttpServlet {

  private static final JsonFactory FACTORY = new JsonFactory();

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    JsonGenerator generator = FACTORY.createJsonGenerator(response.getWriter());
    generator.useDefaultPrettyPrinter();
    
    generator.writeStartObject();

    generator.writeObjectFieldStart("MovingAverageByResponseCode");
    Map<ResponseType, RollingAverage> stats =
        LocationStats.getInstance().getPerResponseRollingStats();
    for (Map.Entry<ResponseType, RollingAverage> entry : stats.entrySet()) {
      generator.writeObjectFieldStart(entry.getKey().getPrintableResponseFamily());

      generator.writeNumberField("count", entry.getValue().count());
      generator.writeNumberField("meanTime", entry.getValue().mean());
      generator.writeNumberField("variance", entry.getValue().variance());

      generator.writeEndObject();
    }
    generator.writeEndObject();
    
    generator.writeEndObject();
    generator.close();
  }
}
