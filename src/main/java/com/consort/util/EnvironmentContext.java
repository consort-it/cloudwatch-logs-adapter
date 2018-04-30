package com.consort.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.StringUtils;

public class EnvironmentContext {

    private static EnvironmentContext context = null;
    private Dotenv dotenv = null;

    private EnvironmentContext() {
        initEnvironment();
    }

    public static EnvironmentContext getInstance() {
        if (context == null) {
            context = new EnvironmentContext();
        }

        return context;
    }

    private void initEnvironment() {
        try {
            dotenv = Dotenv.configure().directory("./src/main/resources").load();
        } catch (Exception e) {
            System.out.println("INFO: Dotenv configuration failed! Ignore if running on prod!");
        }
    }

    public String getenv(final String propertyName) {
        if(dotenv != null) {
            return dotenv.get(propertyName);
        } else {
            final String systemProperty = System.getenv(propertyName);
            if(StringUtils.isBlank(systemProperty)) {
                System.out.println("WARNING: Could not find system property -> " + propertyName + "!");
            }
            return systemProperty;
        }
    }
}
