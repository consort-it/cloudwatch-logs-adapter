package com.consort.security;

import org.pac4j.core.authorization.authorizer.RequireAnyAttributeAuthorizer;

public class CloudWatchLogsAdapterAttributeAuthorizer extends RequireAnyAttributeAuthorizer {

  public CloudWatchLogsAdapterAttributeAuthorizer(final String attribute, final String valueToMatch) {
    super(valueToMatch);
    setElements(attribute);
  }
}
