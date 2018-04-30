package com.consort.restmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZoneId;

public class LogEntry {

  private String timestamp;
  private String message;
  private String ingestionTime;
  private String status;

  public String getTimestamp() {return timestamp;}
  public String getMessage() {return message;}
  public String getIngestionTime() {return ingestionTime;}
  public String getStatus() {return status;}

  public LogEntry(Long timestamp, String message, Long ingestionTime) {
    Timestamp ts = new Timestamp(timestamp);
    JsonNode node = toJSON(message);
    this.timestamp = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();

    if(node==null) {
      this.message = message;
    } else {
      this.message = node.get("log").asText();
    }

    if(this.message.contains("INFO"))
      this.status = LogStatus.INFO.getCode();
    else if(this.message.contains("WARN"))
      this.status = LogStatus.WARNING.getCode();
    else if(this.message.contains("ERR"))
      this.status = LogStatus.ERROR.getCode();
    else
      this.status = LogStatus.LOG.getCode();

    if(node!=null) {
      this.status = (node.get("stream").asText()=="stderr"?LogStatus.ERROR.getCode():this.status);
    }

    ts = new Timestamp(ingestionTime);
    this.ingestionTime = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
  }

  private static JsonNode toJSON(String json) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.readTree(json);
    } catch (IOException e) {
      return null;
    }
  }
}
