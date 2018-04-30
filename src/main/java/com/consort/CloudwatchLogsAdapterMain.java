package com.consort;

import com.consort.controller.ActuatorRouteController;
import com.consort.controller.RouteController;
import com.consort.controller.CloudwatchLogsAdapterRouteController;
import com.consort.util.EnvironmentContext;

import java.util.HashSet;
import java.util.Set;

public class CloudwatchLogsAdapterMain {

  private static Set<RouteController> routeControllers = new HashSet<>();

  public static void main(String[] args) {
    registerRouteControllers();
    initRoutes();
  }

  private static void registerRouteControllers() {
    routeControllers.add(new CloudwatchLogsAdapterRouteController());
    routeControllers.add(new ActuatorRouteController());
  }

  private static void initRoutes() {
    for(final RouteController routeController : routeControllers) {
      routeController.initRoutes();
    }
  }
}

