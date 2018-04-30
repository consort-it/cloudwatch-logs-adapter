package com.consort.exception;

import com.consort.restmodel.Errors;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.time.ZoneId;

@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed"})
public class CloudWatchLogsException extends RuntimeException {
  private int status;
  private String code;
  private String message;
  private String location;
  private String time;
  private StackTraceElement[] trace;

  public int getStatus() {
    return status;
  }
  public String getMessage() {
    return message;
  }
  public String getLocation() {
    return location;
  }
  public String getTime() {
    return time;
  }
  public StackTraceElement[] getTrace() {
    return this.trace.clone();
  }

  public CloudWatchLogsException(Errors err, String location, Exception originalException) {
    super(err.getMessage(), originalException);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    this.status = err.getStatus();
    this.code = err.getCode();
    this.message = err.getMessage();
    this.location = location;
    this.trace = this.getStackTrace();
    this.time = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
  }
  public CloudWatchLogsException(Errors err, String location) {
    super(err.getMessage());
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    this.status = err.getStatus();
    this.code = err.getCode();
    this.message = err.getMessage();
    this.location = location;
    this.trace = this.getStackTrace();
    this.time = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
  }
}
