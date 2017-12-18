package com.jk.modules.api;

import com.google.common.collect.Maps;
import com.jk.modules.cbinproxy.model.CibinApi;
import com.jk.modules.cbinproxy.service.CibinProxyApiManagerService;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
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

  private static final String NO_SUCH_API_ERROR = "5000";

  @Autowired
  private CibinProxyApiManagerService cibinProxyApiManagerService;

  @PostMapping(value = "/api/{apiName}")
  @ResponseBody
  public Map index(@RequestBody Map<String, String> paramMap, @PathVariable("apiName") String apiName) {
    CibinApi cibinApi = new CibinApi();
    cibinApi.setMethod(null);
    cibinApi.setApiName(apiName);
    CibinApi cibinApis = cibinProxyApiManagerService.findOne(cibinApi);

    Map<String, String> result = Maps.newHashMap();
    if (cibinApis == null) {
      log.warn("请求错误，无此接口配置apiName={},requestParam={}", apiName, paramMap);
      result.put(MESSAGE_NAME, "配置错误，无法访问此接口");
      result.put(CODE_NAME, NO_SUCH_API_ERROR);
      return result;
    }

    return result;
  }

  private boolean validateParam(Map<String, String> paramMap, CibinApi cibinApis) {
    return false;
  }
}
