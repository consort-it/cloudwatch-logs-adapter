package com.consort.restmodel;

import java.util.Arrays;

public enum LogStatus {
  LOG("LOG"), INFO("INFO"), WARNING("WARNING"), ERROR("ERROR");
  private String code;

  LogStatus(String ...error) {
    this.code = Arrays.asList(error).get(0);
  }

  public String getCode() {
    return this.code;
  }
}

