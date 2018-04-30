package com.consort.controller;

import com.consort.exception.CloudWatchLogsException;
import com.consort.restmodel.Errors;
import com.consort.service.ActuatorService;
import com.consort.util.EnvironmentContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Service;

import static spark.Service.ignite;

public class ActuatorRouteController implements RouteController {
  private boolean simulateError = false;

  public void initRoutes() {
    final Service http = ignite().port(8081);
    http.get("/api/v1/cloudwatch-logs-adapter/health", (req, res) -> {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(simulateError?"DOWN":"UP");//new Status("UP"));
    });
    http.get("/api/v1/cloudwatch-logs-adapter/metrics", (req, res) -> {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(ActuatorService.getInstance().getCounters(res));
    });
    http.get("/api/v1/cloudwatch-logs-adapter/metrics/:name", (req, res) -> {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(null);//ActuatorService.getInstance().getCounterByName(req.params("name")));
    });

/*    if(EnvironmentContext.getInstance().getenv("PROD_ENABLED") == null) {
      http.get("/api/v1/cloudwatch-logs-adapter/show-error", (req, res) -> {
        this.simulateError = true;
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(new CloudWatchLogsException(Errors.ERR_SIMULATED_CRASH, "ActuatorRouteController::initRoutes"));
      });
    }*/
  }
}
