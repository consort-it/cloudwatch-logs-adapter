package com.consort.restmodel;

import java.util.Arrays;

public enum Errors {
  ERR_UNKNOWN_ERROR("500", "CLA-0", "Unmapped Error ocurred! Aborted."),
  ERR_CONNECTING("500", "CLA-1", "No CloudwatchLogs Connection available! Aborted."),
  ERR_UNDEFINED_LOGGROUP("400", "CLA-2", "LogGroup is undefined! Aborted."),
  ERR_UNDEFINED_LOGGROUPPARTIAL("400", "CLA-3", "Partial of LogGroup is undefined! Aborted."),
  ERR_UNDEFINED_LOGSTREAM("400", "CLA-4", "LogStream is undefined! Aborted."),
  ERR_UNDEFINED_LOGSTREAMPARTIAL("400", "CLA-5", "Partial of LogStream is undefined! Aborted."),
  ERR_NO_SUCH_LOGGROUP("400", "CLA-6", "LogGroup can't be found! Aborted."),
  ERR_NO_SUCH_LOGSTREAM("400", "CLA-7", "LogStream can't be found! Aborted."),
  ERR_NO_SUCH_LOGENTRY("400", "CLA-8", "LogEntry can't be found! Aborted."),
  ERR_EMPTY_LOGGROUP("500", "CLA-8", "LogGroup is empty! Has an instance already been launched? Aborted."),
  ERR_NEGATIVE_LIMIT("400", "CLA-9", "Limit was negative! Aborted."),
  ERR_SIMULATED_CRASH("500", "CLA-996", "Simulated crash! Aborted."),
  ERR_AUTH_FORBIDDEN("401", "CLA-997", "Forbidden! Aborted."),
  ERR_AUTH_REQUIRED("403", "CLA-998", "Authentication required! Aborted."),
  ERR_NOT_FOUND("404", "CLA-999", "Requested URI is not available! Aborted.");

  private Integer status;
  private String code;
  private String message;

  Errors(String ...error) {
    this.status = Integer.parseInt(Arrays.asList(error).get(0));
    this.code = Arrays.asList(error).get(1);
    this.message = Arrays.asList(error).get(2);
  }

  public Integer getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
