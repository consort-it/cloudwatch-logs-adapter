package com.consort.security;

import com.consort.exception.CloudWatchLogsException;
import com.consort.restmodel.Errors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.sparkjava.SparkWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class RestHttpActionAdapter implements HttpActionAdapter<Object, SparkWebContext> {

  public RestHttpActionAdapter() {
  }

  public Object adapt(int code, SparkWebContext context) {
    try {
      if (code == 401) {
        Spark.halt(401, getMapper().writeValueAsString(new CloudWatchLogsException(Errors.ERR_AUTH_FORBIDDEN, "RestHttpActionAdapter::adapt")));
      } else if (code == 403) {
        Spark.halt(403, getMapper().writeValueAsString(new CloudWatchLogsException(Errors.ERR_AUTH_REQUIRED, "RestHttpActionAdapter::adapt")));
      } else if (code == 404) {
        Spark.halt(404, getMapper().writeValueAsString(new CloudWatchLogsException(Errors.ERR_NOT_FOUND, "RestHttpActionAdapter::adapt")));
      } else if (code == 200) {
        Spark.halt(200, context.getSparkResponse().body());
      } else if (code == 302) {
        context.getSparkResponse().redirect(context.getLocation());
      }
    } catch(JsonProcessingException e) {
      // Fallback routine in case sophisticated error handling cant be done
      System.err.println("CRITICAL ERROR when trying to process Exception. Can't provide proper Response sending nothing in body.");

      if (code == 401) {
        Spark.halt(401, Errors.ERR_AUTH_FORBIDDEN.getMessage());
      } else if (code == 403) {
        Spark.halt(403, Errors.ERR_AUTH_REQUIRED.getMessage());
      } else if (code == 404) {
        Spark.halt(404, Errors.ERR_NOT_FOUND.getMessage());
      }
    }
    return null;
  }

  private ObjectMapper getMapper() {
    return new ObjectMapper();
  }
}