package com.consort.controller;

import com.amazonaws.services.logs.model.LogGroup;
import com.amazonaws.services.logs.model.LogStream;
import com.amazonaws.services.logs.model.OutputLogEvent;
import com.consort.exception.CloudWatchLogsException;
import com.consort.restmodel.Errors;
import com.consort.security.AuthorizationFilter;
import com.consort.service.CloudWatchLogsService;
import com.consort.util.EnvironmentContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.osgi.service.log.LogEntry;
import spark.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static spark.Service.ignite;

public class CloudwatchLogsAdapterRouteController implements RouteController {

  private static final String GROUPS_PATH = "/api/v1/cloudwatch-logs-adapter/groups";
  private static final String GROUP_PATH = "/api/v1/cloudwatch-logs-adapter/groups/:gid";
  private static final String GROUP_STREAMS_PATH = "/api/v1/cloudwatch-logs-adapter/groups/:gid/streams";
  private static final String GROUP_STREAM_PATH = "/api/v1/cloudwatch-logs-adapter/groups/:gid/streams/:sid";
  private static final String GROUP_STREAM_LOGS_PATH = "/api/v1/cloudwatch-logs-adapter/groups/:gid/streams/:sid/logs";
  private static final String STREAM_PATH = "/api/v1/cloudwatch-logs-adapter/streams/:sid";
  private static final String STREAM_LOGS_PATH = "/api/v1/cloudwatch-logs-adapter/streams/:sid/logs";
  private static final String SEARCH_GROUPS_PATH = "/api/v1/cloudwatch-logs-adapter/search/groups/:partial";
  private static final String SEARCH_STREAMS_PATH = "/api/v1/cloudwatch-logs-adapter/search/streams/:partial";
  private static final String DOCS_USER_ID_PATH = "/api/v1/cloudwatch-logs-adapter/:microServiceName";

  private static final String AUTHORIZER_NAME = "scope";
  private static final String ROLE_ADMIN = "aws.cognito.signin.user.admin";
  private static final String ROLE_DEVELOPER = "aws.cognito.signin.user.developer";

  public void initRoutes() {
    final Service http = ignite().port(8080);
    enableCORS(http, "*", "GET, POST", "Content-Type, Authorization");

    http.notFound((req, res) -> {
      return getMapper().writeValueAsString(new CloudWatchLogsException(Errors.ERR_NOT_FOUND, "InitRoutes: "+req.queryParams()+req.params()+req.url()+req.host()+req.port()+" not Found."));
    });

    http.internalServerError((req, res) -> {
      return getMapper().writeValueAsString(new CloudWatchLogsException(Errors.ERR_UNKNOWN_ERROR, "InitRoutes: Unknown Error."));
    });

    handleGroup(http);
    handleGroups(http);
    handleStreamsOfGroup(http);
    handleStreamOfGroup(http);
    handleLogOfGroupOfStream(http);
    handleStreams(http);
    handleStreamLogs(http);
    handleSearchGroups(http);
    handleSearchStreams(http);
    handleLogsByMicroServiceName(http);
  }

  private void handleLogsByMicroServiceName(final Service http) {
    http.before(DOCS_USER_ID_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(DOCS_USER_ID_PATH, (req, res) -> {
      try {
        final List<com.consort.restmodel.LogEntry> result = new LinkedList<>();
        final List<LogGroup> groups = CloudWatchLogsService.getInstance().searchGroups(req.params("microServiceName"));
        if(groups.isEmpty())
          throw new CloudWatchLogsException(Errors.ERR_UNDEFINED_LOGGROUPPARTIAL, "CloudwatchLogsAdapter::handleLogsByMicroServiceName");
        final List<LogStream> streams = CloudWatchLogsService.getInstance().getStreamsOfGroup(groups.get(0));
        if(streams.isEmpty())
          throw new CloudWatchLogsException(Errors.ERR_EMPTY_LOGGROUP, "CloudwatchLogsAdapter::handleLogsByMicroServiceName");
//        final List<OutputLogEvent> logs = CloudWatchLogsService.getInstance().getLogsByMicroServiceName(req.params("microServiceName"));
        final List<OutputLogEvent> logs = CloudWatchLogsService.getInstance().getLogByGroupAndStream(groups.get(0), streams.get(0), 20);
        Iterator<OutputLogEvent> it = logs.iterator();
        OutputLogEvent item = null;
        while (it.hasNext()) {
          item = it.next();
          result.add(new com.consort.restmodel.LogEntry(item.getTimestamp(), item.getMessage(), item.getIngestionTime()));
        }

        //      if(result == null) res.status(404);
        //      else res.type("application/json");

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleGroups(final Service http) {
    http.before(GROUPS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(GROUPS_PATH, (req, res) -> {
      final ObjectMapper mapper = new ObjectMapper();

      final List<com.consort.restmodel.LogGroup> result = new LinkedList<>();
      final List<LogGroup> groups = CloudWatchLogsService.getInstance().getGroups();
      Iterator<LogGroup> it = groups.iterator();
      LogGroup item = null;
      while (it.hasNext()) {
        item = it.next();
        result.add(new com.consort.restmodel.LogGroup(item.getLogGroupName(), item.getCreationTime(), item.getArn(), item.getRetentionInDays(), item.getMetricFilterCount(), item.getStoredBytes()));
      }

      return getMapper().writeValueAsString(result);
    });
  }

  private void handleGroup(final Service http) {
    http.before(GROUP_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(GROUP_PATH, (req, res) -> {
      try {
        LogGroup item = CloudWatchLogsService.getInstance().getGroup(req.params("gid"));
//        com.consort.restmodel.LogGroup result = new com.consort.restmodel.LogGroup(item.getLogGroupName(), item.getCreationTime(), item.getArn(), item.getRetentionInDays(), item.getMetricFilterCount(), item.getStoredBytes());
        return getMapper().writeValueAsString(new com.consort.restmodel.LogGroup(item.getLogGroupName(), item.getCreationTime(), item.getArn(), item.getRetentionInDays(), item.getMetricFilterCount(), item.getStoredBytes()));
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleStreamsOfGroup(final Service http) {
    http.before(GROUP_STREAMS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(GROUP_STREAMS_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final LogGroup group = CloudWatchLogsService.getInstance().getGroup(req.params("gid"));
        final List<LogStream> streams = CloudWatchLogsService.getInstance().getStreamsOfGroup(group);
        final List<com.consort.restmodel.LogStream> result = new LinkedList<>();
        Iterator<LogStream> it = streams.iterator();
        LogStream item = null;
        while (it.hasNext()) {
          item = it.next();
          result.add(new com.consort.restmodel.LogStream(item.getLogStreamName(), item.getFirstEventTimestamp(), item.getLastEventTimestamp(), item.getLastIngestionTime(), item.getUploadSequenceToken(), item.getArn(), item.getStoredBytes()));
        }

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleStreamOfGroup(final Service http) {
    http.before(GROUP_STREAM_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(GROUP_STREAM_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final LogGroup group = CloudWatchLogsService.getInstance().getGroup(req.params("gid"));
        final LogStream item = CloudWatchLogsService.getInstance().getStreamOfGroup(group, req.params("sid"));
        final com.consort.restmodel.LogStream result;
        result = (new com.consort.restmodel.LogStream(item.getLogStreamName(), item.getFirstEventTimestamp(), item.getLastEventTimestamp(), item.getLastIngestionTime(), item.getUploadSequenceToken(), item.getArn(), item.getStoredBytes()));

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleLogOfGroupOfStream(final Service http) {
    http.before(GROUP_STREAM_LOGS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(GROUP_STREAM_LOGS_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final LogGroup group = CloudWatchLogsService.getInstance().getGroup(req.params("gid"));
        final LogStream stream = CloudWatchLogsService.getInstance().getStreamOfGroup(group, req.params("sid"));
        final List<OutputLogEvent> events = CloudWatchLogsService.getInstance().getLogByGroupAndStream(group, stream, (req.queryParams().contains("limit")?Integer.parseInt(req.queryParams("limit")):100));
        final List<com.consort.restmodel.LogEntry> result = new LinkedList<>();
        Iterator<OutputLogEvent> it = events.iterator();
        OutputLogEvent item = null;
        while (it.hasNext()) {
          item = it.next();
          result.add(new com.consort.restmodel.LogEntry(item.getTimestamp(), item.getMessage(), item.getIngestionTime()));
        }

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleStreams(final Service http) {
    http.before(STREAM_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(STREAM_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final List<LogStream> streams = CloudWatchLogsService.getInstance().getStreams(req.params("sid"));
        final List<com.consort.restmodel.LogStream> result = new LinkedList<>();
        Iterator<LogStream> it = streams.iterator();
        LogStream item = null;
        while (it.hasNext()) {
          item = it.next();
          result.add(new com.consort.restmodel.LogStream(item.getLogStreamName(), item.getFirstEventTimestamp(), item.getLastEventTimestamp(), item.getLastIngestionTime(), item.getUploadSequenceToken(), item.getArn(), item.getStoredBytes()));
        }

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleStreamLogs(final Service http) {
    http.before(STREAM_LOGS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(STREAM_LOGS_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final List<List<OutputLogEvent>> events = CloudWatchLogsService.getInstance().getLogsByStream(req.params("sid"), (req.queryParams().contains("limit")?Integer.parseInt(req.queryParams("limit")):100));
        final List<List<com.consort.restmodel.LogEntry>> result = new LinkedList<>();
        Iterator<List<OutputLogEvent>> it = events.iterator();
        List<OutputLogEvent> item = null;
        while (it.hasNext()) {
          LinkedList<com.consort.restmodel.LogEntry> temp = new LinkedList<>();

          item = it.next();
          Iterator<OutputLogEvent> it2 = item.iterator();
          OutputLogEvent item2 = null;
          while (it2.hasNext()) {
            item2 = it2.next();
            temp.add(new com.consort.restmodel.LogEntry(item2.getTimestamp(), item2.getMessage(), item2.getIngestionTime()));
          }
          result.add(temp);
        }

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleSearchGroups(final Service http) {
    http.before(SEARCH_GROUPS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(SEARCH_GROUPS_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final List<LogGroup> groups = CloudWatchLogsService.getInstance().searchGroups(req.params("partial"));
        List<com.consort.restmodel.LogGroup> result = new LinkedList<>();
        Iterator<LogGroup> it = groups.iterator();
        LogGroup item = null;
        while (it.hasNext()) {
          item = it.next();
          result.add(new com.consort.restmodel.LogGroup(item.getLogGroupName(), item.getCreationTime(), item.getArn(), item.getRetentionInDays(), item.getMetricFilterCount(), item.getStoredBytes()));
        }

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private void handleSearchStreams(final Service http) {
    http.before(SEARCH_STREAMS_PATH, new AuthorizationFilter(AUTHORIZER_NAME, ROLE_ADMIN));
    http.get(SEARCH_STREAMS_PATH, (req, res) -> {
      try {
        final ObjectMapper mapper = new ObjectMapper();

        final List<LogStream> streams = CloudWatchLogsService.getInstance().searchStreams(req.params("partial"));
        List<com.consort.restmodel.LogStream> result = new LinkedList<>();
        Iterator<LogStream> it = streams.iterator();
        LogStream item = null;
        while (it.hasNext()) {
          item = it.next();
          result.add(new com.consort.restmodel.LogStream(item.getLogStreamName(), item.getFirstEventTimestamp(), item.getLastEventTimestamp(), item.getLastIngestionTime(), item.getUploadSequenceToken(), item.getArn(), item.getStoredBytes()));
        }

        return getMapper().writeValueAsString(result);
      } catch(CloudWatchLogsException e) {
        System.err.println(e);
        res.status(e.getStatus());
        return getMapper().writeValueAsString(e);
      }
    });
  }

  private static void enableCORS(final Service http, final String origin, final String methods, final String headers) {
    http.options("/*", (req, res) -> {
      final String acRequestHeaders = req.headers("Access-Control-Request-Headers");
      if (acRequestHeaders != null) {
        res.header("Access-Control-Allow-Headers", acRequestHeaders);
      }

      final String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    http.before((req, res) -> {
      res.header("Access-Control-Allow-Origin", origin);
      res.header("Access-Control-Request-Method", methods);
      res.header("Access-Control-Allow-Headers", headers);
      res.type("application/json");
      res.header("Server", "-");
    });
  }

  private ObjectMapper getMapper() {
    return new ObjectMapper();
  }
}
