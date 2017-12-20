package com.jk.modules.cbinproxy.model;

import com.jk.common.base.model.BaseEntity;
import lombok.Data;

@Data
public class CibinProxyLog extends BaseEntity {
  private String apiName;
  private String traceId;
  private String clientIdentity;
  private String requestParam;
  private String requestUrl;
  private String response;
  private String errorCode;
  private String exceptionStackTrace;
  private String requestIp;
  private Long timeConsuming;
  private String userAgent;
}
