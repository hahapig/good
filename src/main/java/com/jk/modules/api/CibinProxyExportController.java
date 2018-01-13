package com.jk.modules.api;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jk.common.util.HttpRequestUtil;
import com.jk.common.util.RSACryptography;
import com.jk.modules.cbinproxy.model.CibinApi;
import com.jk.modules.cbinproxy.model.CibinProxyLog;
import com.jk.modules.cbinproxy.service.CibinProxyApiManagerService;
import com.jk.modules.cbinproxy.service.CibinProxyConstant;
import com.jk.modules.cbinproxy.service.CibinProxyLogService;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 */
@Slf4j
@Controller
public class CibinProxyExportController extends BaseController {

  private static Executor executor = Executors
      .newFixedThreadPool(5, new CustomizableThreadFactory("cibinprox-log"));

  private static final String CONFIG_ERROR = "500";

  private static final String SEPARATOR = ",";

  private static final Boolean SIGN_CHECK = Boolean.FALSE;

  @Autowired
  private CibinProxyApiManagerService cibinProxyApiManagerService;

  @Autowired
  private CibinProxyLogService cibinProxyLogService;

  @RequestMapping(value = "/cibinprox/{apiName}", method = {RequestMethod.POST, RequestMethod.GET})
  @ResponseBody
  public Object proxyAPI(@RequestBody Map<String, String> paramMap,
      @PathVariable("apiName") String apiName, HttpServletRequest request) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    CibinApi cibinApi = new CibinApi();
    cibinApi.setMethod(null);
    cibinApi.setApiName(apiName);
    CibinApi cibinApis = cibinProxyApiManagerService.findOne(cibinApi);

    Map<String, String> result = Maps.newHashMap();
    if (cibinApis == null) {
      log.warn("请求错误，无此接口配置apiName={},requestParam={}", apiName, paramMap);
      result.put(MESSAGE_NAME, "配置错误，无此接口,无法访问此接口");
      result.put(CODE_NAME, CONFIG_ERROR);
      return result;
    }
    if (!validateParam(paramMap, cibinApis)) {
      log.warn("请求错误，缺少必要参数apiName={}, requestParam = {}", apiName, paramMap);
      result.put(MESSAGE_NAME, "缺少必要参数");
      result.put(CODE_NAME, CONFIG_ERROR);
      return result;
    }
    if (!checkSign(paramMap)) {
      log.warn("请求错误，签名错误apiName={}, requestParam = {}", apiName, paramMap);
      result.put(MESSAGE_NAME, "签名错误");
      result.put(CODE_NAME, CONFIG_ERROR);
      return result;
    }
    // do real request
    Map<String, String> preparedParam = extractCibinParam(paramMap, cibinApis);

    try {
      JsonObject jsonObject = cibinProxyApiManagerService.requestRemote(preparedParam, cibinApi);
      if (!CibinProxyConstant.SUCCESS_CODE
          .equalsIgnoreCase(jsonObject.get(CibinProxyConstant.RESPONSE_CODE_NAME).getAsString())) {
        CibinProxyLog log = buildLog(apiName, paramMap, request, jsonObject, stopWatch);
        executor.execute(() -> {
          cibinProxyLogService.save(log);
        });
      }
      return jsonObject;
    } catch (Exception e) {
      CibinProxyLog log = buildLog(apiName, paramMap, request, null, stopWatch);
      log.setExceptionStackTrace(ExceptionUtils.getMessage(e));
      executor.execute(() -> {
        cibinProxyLogService.save(log);
      });
      throw e;
    }
  }

  private CibinProxyLog buildLog(String apiName, Map<String, String> requestParam,
      HttpServletRequest request, JsonObject response, StopWatch stopWatch) {
    String ip = HttpRequestUtil.ipAddress(request);
    String userAgent = HttpRequestUtil.getUserAgent(request);
    String deviceId = request.getHeader("device-id");

    CibinProxyLog log = new CibinProxyLog();
    log.setClientIdentity(deviceId);
    if (response != null) {
      log.setErrorCode(response.get(CibinProxyConstant.RESPONSE_CODE_NAME).getAsString());
      log.setResponse(response.toString());
    }
    log.setRequestIp(ip);
    log.setRequestParam(new Gson().toJson(requestParam));
    log.setRequestUrl(HttpRequestUtil.extractRequestUrl(request));
    log.setUserAgent(userAgent);
    log.setTraceId(MDC.get("seq"));
    log.setApiName(apiName);
    stopWatch.stop();
    log.setTimeConsuming(stopWatch.getTime());
    return log;
  }

  private boolean checkSign(Map<String, String> paramMap) {
    if (SIGN_CHECK == Boolean.FALSE) {
      return true;
    }
    if (!paramMap.containsKey("sign")) {
      return false;
    }
    try {
      RSACryptography.decrypt(Base64.getUrlDecoder().decode(paramMap.get("sign")), RSACryptography.privateKey);
    } catch (Exception e) {
      log.warn("can not decrypt sign . paramMap=" + paramMap, e);
      return false;
    }
    // 验证签名，待协商
    return true;
  }

  private Map<String, String> extractCibinParam(Map<String, String> paramMap, CibinApi cibinApis) {
    if (cibinApis.getParamNames() == null) {
      return paramMap;
    }
    String[] param = cibinApis.getParamNames().split(SEPARATOR);
    Map<String, String> result = Maps.newHashMap();
    for (String paramName : param) {
      result.put(paramName, paramMap.get(paramName));
    }
    return result;
  }

  private boolean validateParam(Map<String, String> paramMap, CibinApi cibinApis) {
    String[] requiredParam = cibinApis.getCheckedParams() == null ? new String[]{}
        : cibinApis.getCheckedParams().split(SEPARATOR);
    for (String param : requiredParam) {
      if (!paramMap.containsKey(param)) {
        log.warn("bad request . expect param {}. but not found", param);
        return false;
      }
    }
    return true;
  }
}
