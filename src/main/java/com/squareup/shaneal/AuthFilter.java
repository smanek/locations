package com.squareup.shaneal;

import org.apache.commons.codec.binary.Base64;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * User: smanek
 * <p/>
 * Implements basic HTTP auth for the location service.
 */
public class AuthFilter implements Filter {
  private String username;
  private String password;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    InputStream inputStream = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/auth.properties");
    Properties props = new Properties();
    try {
      props.load(inputStream);
      inputStream.close();

      username = props.getProperty("username");
      password = props.getProperty("password");
    } catch (IOException e) {
      throw new ServletException("Unable to read auth.properties configuration", e);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    boolean valid = false;

    HttpServletRequest hReq = (HttpServletRequest) request;
    String authHeader = hReq.getHeader("Authorization");

    if (authHeader != null) {
      StringTokenizer st = new StringTokenizer(authHeader);
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();

        if (basic.equalsIgnoreCase("Basic")) {
          String credentials = st.nextToken();

          // The decoded string is in the form
          // "userID:password".
          String[] usernamePassword = new String(Base64.decodeBase64(credentials), "UTF-8").split(":");

          // I'm vulnerable to a sidechannel timing attach here (since String.equals() short-circuits).
          // For this application, I don't really care (hence the HTTP basic auth).
          // Obviously wouldn't do this if I wanted actual security though
          if (usernamePassword.length == 2
              && username.equals(usernamePassword[0])
              && password.equals(usernamePassword[1])) {
            valid = true;
          }
        }
      }
    }

    if (valid) {
      chain.doFilter(request, response);
    } else {
      HttpServletResponse hResp = (HttpServletResponse) response;
      hResp.setHeader("WWW-Authenticate", "Basic realm=\"SquareApp\"");
      hResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  @Override
  public void destroy() {
    // nothing to do
  }
}
