package com.consort.restmodel;

import java.sql.Timestamp;
import java.time.ZoneId;

public class LogStream {
  public String getLogStreamName() {return logStreamName;}
  public void setLogStreamName(String logStreamName) {this.logStreamName = logStreamName;}
  public String getFirstEventTimestamp() {return firstEventTimestamp;}
  public void setFirstEventTimestamp(String firstEventTimestamp) {this.firstEventTimestamp = firstEventTimestamp;}
  public String getLastEventTimestamp() {return lastEventTimestamp;}
  public void setLastEventTimestamp(String lastEventTimestamp) {this.lastEventTimestamp = lastEventTimestamp;}
  public String getLastIngestionTime() {return lastIngestionTime;}
  public void setLastIngestionTime(String lastIngestionTime) {this.lastIngestionTime = lastIngestionTime;}
  public String getUploadSequenceToken() {return uploadSequenceToken;}
  public void setUploadSequenceToken(String uploadSequenceToken) {this.uploadSequenceToken = uploadSequenceToken;}
  public String getArn() {return arn;}
  public void setArn(String arn) {this.arn = arn;}
  public long getStoredBytes() {return storedBytes;}
  public void setStoredBytes(long storedBytes) {this.storedBytes = storedBytes;}

  private String logStreamName;
  private String firstEventTimestamp;
  private String lastEventTimestamp;
  private String lastIngestionTime;
  private String uploadSequenceToken;
  private String arn;
  private Long storedBytes;

  public LogStream(String name, Long firstEventTimestamp, Long lastEventTimestamp, Long lastIngestionTime, String uploadSequenceToken, String arn, Long storedBytes) {
    Timestamp ts = new Timestamp(firstEventTimestamp);
    this.logStreamName = name;
    this.firstEventTimestamp = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
    ts = new Timestamp(lastEventTimestamp);
    this.lastEventTimestamp = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
    ts = new Timestamp(lastIngestionTime);
    this.lastIngestionTime = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
    this.uploadSequenceToken = uploadSequenceToken;
    this.arn = arn;
    this.storedBytes = storedBytes;
  }
}
