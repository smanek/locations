package com.squareup.shaneal;

import com.squareup.shaneal.stats.LocationStats;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: smanek
 * <p/>
 * Gets stats on the LocationServlet's response times.
 */
public class MetricsFilter implements Filter {
  private static final LocationStats stats = LocationStats.getInstance();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // nothing to do here
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final long startTime = System.currentTimeMillis();
    final StatusSharingServletResponse wrappedResp = new StatusSharingServletResponse((HttpServletResponse) response);
    chain.doFilter(request, wrappedResp);
    final long endTime = System.currentTimeMillis();
    stats.observe(wrappedResp.getStatus(), endTime - startTime);
  }

  @Override
  public void destroy() {
    // nothing to do here
  }
}
