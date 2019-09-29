package com.ctrip.framework.apollo.core;

public interface ConfigConsts {
  String NAMESPACE_APPLICATION = "application";
  String CLUSTER_NAME_DEFAULT = "default";
  String KEY_EUREKA_SERVICE_URL = "eureka.service.url";
  String CLUSTER_NAMESPACE_SEPARATOR = "+";
  String APOLLO_CLUSTER_KEY = "apollo.cluster";
  String APOLLO_META_KEY = "apollo.meta";
  String CONFIG_FILE_CONTENT_KEY = "content";
  String NO_APPID_PLACEHOLDER = "ApolloNoAppIdPlaceHolder";
  long NOTIFICATION_ID_PLACEHOLDER = -1;

  boolean DEFAULT_APOLLO_BOOTSTRAP_ENABLED = true;
}
