package com.consort.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.*;
import com.consort.exception.CloudWatchLogsException;
import com.consort.restmodel.Errors;
import com.consort.util.EnvironmentContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CloudWatchLogsService {

  private static CloudWatchLogsService instance = null;
  private AWSLogs logsReader;

  private CloudWatchLogsService() {
    if(EnvironmentContext.getInstance().getenv("queueaccesskey") != null && EnvironmentContext.getInstance().getenv("queuesecretkey") != null) {
      final BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
          EnvironmentContext.getInstance().getenv("queueaccesskey"),
          EnvironmentContext.getInstance().getenv("queuesecretkey"));

      this.logsReader = AWSLogsClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1.getName()).withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
    }
  }

  public void withLogsReader(AWSLogs logsReader) {
    this.logsReader = logsReader;
  }

  public static CloudWatchLogsService getInstance() {
    if (instance == null) {
      instance = new CloudWatchLogsService();
    }

    return instance;
  }

  public List<LogGroup> getGroups() {
    String nextToken = null;
    DescribeLogGroupsRequest reqLogGroup = new DescribeLogGroupsRequest();
    DescribeLogGroupsResult resLogGroup;
    List<LogGroup> groups = new ArrayList<LogGroup>();

    do {
      // Usually LogGroups are served in packages of size 50 so look for more by token given
      if(nextToken!=null) {
        reqLogGroup.withNextToken(nextToken);
      }

      resLogGroup = this.logsReader.describeLogGroups(reqLogGroup);
      groups.addAll(resLogGroup.getLogGroups());

      nextToken = resLogGroup.getNextToken();
    } while(nextToken!= null);

    return groups; //this.logsReader.describeLogGroups().getLogGroups();
  }

  public LogGroup getGroup(final String id) throws CloudWatchLogsException {
    LogGroup group = null;

    if(id == null || id.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGGROUP, "CloudWatchLogsService::getGroup()");

    for(LogGroup logGroup: this.getGroups()) {
      if (logGroup.getLogGroupName().equals(id)) {
        group = logGroup;
        break;
      }
    }

    if(group == null)
      throw new CloudWatchLogsException(Errors.ERR_NO_SUCH_LOGGROUP, "CloudWatchLogsService::getGroup()");

    return group;
  }

  public List<LogStream> getStreamsOfGroup(final LogGroup group) throws CloudWatchLogsException {
    String nextToken = null;

    if(group == null) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGGROUP, "CloudWatchLogsService::getStreamsOfGroup()");

    DescribeLogStreamsRequest reqLogStream = new DescribeLogStreamsRequest().withLogGroupName(group.getLogGroupName());
    DescribeLogStreamsResult resLogStream;
    List<LogStream> streams = new ArrayList<LogStream>();

    do {
      // Usually LogGroups are served in packages of size 50 so look for more by token given
      if(nextToken!=null) {
        reqLogStream.withNextToken(nextToken);
      }

      resLogStream = this.logsReader.describeLogStreams(reqLogStream);
      streams.addAll(resLogStream.getLogStreams());

      nextToken = resLogStream.getNextToken();
    } while(nextToken!= null);

    return streams; //this.logsReader.describeLogGroups().getLogGroups();
  }

  public LogStream getStreamOfGroup(final LogGroup group, final String id) throws CloudWatchLogsException {
    LogStream stream = null;

    if(id == null || id.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGSTREAM, "CloudWatchLogsService::getStreamsOfGroup()");
    if(group == null) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGGROUP, "CloudWatchLogsService::getStreamsOfGroup()");

    for(LogStream logStream: this.getStreamsOfGroup(group)) {
      if (logStream.getLogStreamName().equals(id)) {
        stream = logStream;
        break;
      }
    }

    if(stream == null)
      throw new CloudWatchLogsException(Errors.ERR_NO_SUCH_LOGSTREAM, "CloudWatchLogsService::getStreamOfGroup()");

    return stream;
  }

  public List<LogStream> getStreams(final String id) throws CloudWatchLogsException {
    List<LogStream> streams = new ArrayList<LogStream>();

    if(id == null || id.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGSTREAM, "CloudWatchLogsService::getStreams()");

    for(LogGroup logGroup: this.getGroups()) {
      for (LogStream logStream : this.getStreamsOfGroup(logGroup)) {
        if (logStream.getLogStreamName().equals(id)) {
          streams.add(logStream);
          break;
        }
      }
    }

    if(streams.isEmpty())
      throw new CloudWatchLogsException(Errors.ERR_NO_SUCH_LOGSTREAM, "CloudWatchLogsService::getStreams()");

    return streams;
  }

  public List<OutputLogEvent> getLogByGroupAndStream(final LogGroup group, final LogStream stream, final Integer limit) throws CloudWatchLogsException {
    if(group == null) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGGROUP, "CloudWatchLogsService::getLogByGroupAndStream()");
    if(stream == null) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGSTREAM, "CloudWatchLogsService::getLogByGroupAndStream()");
    if(limit <= 0) throw new CloudWatchLogsException(Errors.ERR_NEGATIVE_LIMIT, "CloudWatchLogsService::getLogByGroupAndStream()");

    GetLogEventsRequest reqLogEvents = new GetLogEventsRequest().withLogGroupName(group.getLogGroupName()).withLogStreamName(stream.getLogStreamName());
    System.out.println("Triggered at: " + group.getLogGroupName() + "(group) " + stream.getLogStreamName() + "(stream)");
    List<OutputLogEvent> output = this.logsReader.getLogEvents(reqLogEvents).getEvents();

    if(output == null)
      throw new CloudWatchLogsException(Errors.ERR_NO_SUCH_LOGENTRY, "CloudWatchLogsService::getLogByGroupAndStream()");

    return output.subList((output.size()-limit>0?output.size()-limit:0), output.size());
  }

  public List<List<OutputLogEvent>> getLogsByStream(final String id, final Integer limit) throws CloudWatchLogsException {
    List<List<OutputLogEvent>> logsSet = new ArrayList<List<OutputLogEvent>>();

    if(id == null || id.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGSTREAM, "CloudWatchLogsService::getLogsByStream()");
    if(limit <= 0) throw new CloudWatchLogsException(Errors.ERR_NEGATIVE_LIMIT, "CloudWatchLogsService::getLogByStream()");

    for(LogGroup group: this.getGroups()) {
      LogStream stream = this.getStreamOfGroup(group, id);
      if(stream != null) {
        List<OutputLogEvent> logs = this.getLogByGroupAndStream(group, stream, limit);
        if (logs != null && logs.size() > 0)
          logsSet.add(logs);
      }
    }

    if(logsSet == null || logsSet.isEmpty())
      throw new CloudWatchLogsException(Errors.ERR_NO_SUCH_LOGSTREAM, "CloudWatchLogsService::getLogsByStream()");

    return logsSet;
  }

  public List<LogGroup> searchGroups(final String partial) throws CloudWatchLogsException {
    List<LogGroup> groups = this.getGroups();

    if(partial == null || partial.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGGROUPPARTIAL, "CloudWatchLogsService::searchGroups()");

    groups.removeIf(group -> !group.getLogGroupName().contains(partial));
//    for (Iterator<LogGroup> iter = groups.iterator(); iter.hasNext(); ) {
//      LogGroup group = iter.next();
//      if (!group.getLogGroupName().contains(partial)) {
//        iter.remove();
//      }
//    }

    return groups;
  }

  public List<LogStream> searchStreams(final String partial) throws CloudWatchLogsException {
    List<LogGroup> groups = this.getGroups();
    List<LogStream> streamsColl = new ArrayList<LogStream>();

    if(partial == null || partial.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGSTREAMPARTIAL, "CloudWatchLogsService::searchStreams()");

    for (LogGroup group : groups) {
      final List<LogStream> streams = this.getStreamsOfGroup(group);
      streams.removeIf(stream -> !stream.getLogStreamName().contains(partial));
//      for (Iterator<LogStream> iter = streams.iterator(); iter.hasNext(); ) {
//        LogStream stream = iter.next();
//        if (!stream.getLogStreamName().contains(partial)) {
//          iter.remove();
//        }
//      }
      streamsColl.addAll(streams);
    }

    return streamsColl;
  }

  public List<OutputLogEvent> getLogsByMicroServiceName(final String partial) throws CloudWatchLogsException {
    DescribeLogStreamsRequest reqLogStream;
    DescribeLogStreamsResult resLogStream;
    List<LogStream> availableStreams;
    String nextToken = null;

    if(partial == null || partial.isEmpty()) throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGSTREAMPARTIAL, "CloudWatchLogsService::getLogsByMicroServiceName()");

    for(LogGroup logGroup: this.logsReader.describeLogGroups().getLogGroups()) {
      do {
        // If LogGroup contains more than 50 LogStreams a token becomes available representing the next up to 50 LogStreams
        if(nextToken!=null) {
          reqLogStream = new DescribeLogStreamsRequest().withLogGroupName(logGroup.getLogGroupName()).withNextToken(nextToken);
        } else {
          reqLogStream = new DescribeLogStreamsRequest().withLogGroupName(logGroup.getLogGroupName());
        }
        resLogStream = this.logsReader.describeLogStreams(reqLogStream);
        availableStreams = resLogStream.getLogStreams();

        // iterate on LogStreams of LogGroup and remove LogStream insufficient for request
        for (Iterator<LogStream> iter = availableStreams.iterator(); iter.hasNext(); ) {
          LogStream stream = iter.next();
          if (!stream.getLogStreamName().contains(partial)) {
            iter.remove();
          }
        }
        // in case LogStreams are still available a match has been detected that needs to be handled as result of the request
        if (!availableStreams.isEmpty()) {
          LogStream latest = availableStreams.get(0);
          for (LogStream stream : availableStreams) {
            if (stream.getLastEventTimestamp()!=null && latest.getLastEventTimestamp() < stream.getLastEventTimestamp()) {
              latest = stream;
            }
          }

          GetLogEventsRequest reqLogEvents = new GetLogEventsRequest().withLogGroupName(logGroup.getLogGroupName()).withLogStreamName(latest.getLogStreamName());
          List<OutputLogEvent> output = logsReader.getLogEvents(reqLogEvents).getEvents();
          return logsReader.getLogEvents(reqLogEvents).getEvents().subList((output.size()-20>0?output.size()-20:0), this.logsReader.getLogEvents(reqLogEvents).getEvents().size());
        }
        nextToken = resLogStream.getNextToken();
      } while(nextToken!= null);
    }
    return new LinkedList<>();
  }
}
