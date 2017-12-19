package com.jk.modules.api;

import com.google.common.collect.Maps;
import com.jk.modules.cbinproxy.model.CibinApi;
import com.jk.modules.cbinproxy.service.CibinProxyApiManagerService;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 */
@RestController("export/cibinproxy")
@Slf4j
public class CibinProxyExportController extends BaseController {

  private static final String CONFIG_ERROR = "5000";

  private static final String SEPARATOR = ",";

  @Autowired
  private CibinProxyApiManagerService cibinProxyApiManagerService;

  @PostMapping(value = "/api/{apiName}")
  @ResponseBody
  public Map index(@RequestBody Map<String, String> paramMap,
      @PathVariable("apiName") String apiName) {
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

    return result;
  }

  private boolean checkSign(Map<String, String> paramMap) {
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
