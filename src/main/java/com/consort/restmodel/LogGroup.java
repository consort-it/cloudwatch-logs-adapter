package com.consort.restmodel;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LogGroup {
  public String getLogGroupName() {return logGroupName;}
  public String getCreationTime() {return creationTime;}
  public String getArn() {return arn;}
  public Integer getRetentionInDays() {return retentionInDays;}
  public Integer getMetricFilterCount() {return metricFilterCount;}
  public Long getStoredBytes() {return storedBytes;}

  private String logGroupName;
  private String creationTime;
  private String arn;
  private Integer retentionInDays;
  private Integer metricFilterCount;
  private Long storedBytes;

  public LogGroup(String name, Long creationTime, String arn, Integer retentionInDays, Integer metricFilterCount, Long storedBytes) {
    this.logGroupName = name;
    Timestamp ts = new Timestamp(creationTime);
    this.creationTime = ts.toLocalDateTime().atZone(ZoneId.of("+00:00")).toString();
    this.arn = arn;
    this.retentionInDays = retentionInDays;
    this.metricFilterCount = metricFilterCount;
    this.storedBytes = storedBytes;
  }
}
