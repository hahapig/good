package com.jk.modules.cbinproxy.service;

import static com.jk.modules.cbinproxy.service.CibinProxyConstant.COMMON_PARAM_CHANNEL;
import static com.jk.modules.cbinproxy.service.CibinProxyConstant.COMMON_PARAM_TIME;
import static com.jk.modules.cbinproxy.service.CibinProxyConstant.EXPIRATION_AHEAD;
import static com.jk.modules.cbinproxy.service.CibinProxyConstant.REMOTE_API_ERROR;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jk.common.base.service.impl.BaseServiceImpl;
import com.jk.common.exception.BaseException;
import com.jk.modules.cbinproxy.model.CibinApi;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

/**
 * Created by tao on 17-12-17.
 */
@Transactional
@Service
@Slf4j
public class CibinProxyApiManagerServiceImpl extends BaseServiceImpl<CibinApi> implements
    CibinProxyApiManagerService {

  private volatile CibinSecret cibinSecret = null;
  @Autowired
  private RestTemplate restTemplate;

  @Value("${cibinproxy.host}")
  private String cibinHost;

  @Value("${cibinproxy.channelId}")
  private String cibinChannelId;

  public PageInfo<CibinApi> findPage(Integer pageNum, Integer pageSize) throws Exception {
    Example example = new Example(CibinApi.class);
    PageHelper.startPage(pageNum, pageSize);
    List<CibinApi> cibinApiList = this.selectByExample(example);
    return new PageInfo<>(cibinApiList);
  }

  @Override
  public JsonObject requestRemote(Map<String, String> preparedParam, CibinApi cibinApi) {
    JsonObject jsonObject = doRequestRemote(preparedParam, cibinApi);
    if ( CibinProxyConstant.SECRET_OUTAGE_CODE.equalsIgnoreCase(jsonObject.get(CibinProxyConstant.RESPONSE_CODE_NAME).getAsString())) {
      log.warn("secret outage.reset secret.retry ....");
      cibinSecret = null;
      jsonObject = doRequestRemote(preparedParam, cibinApi);
    }
    return jsonObject;
  }

  private JsonObject doRequestRemote(Map<String, String> preparedParam, CibinApi cibinApi) {
    // 1.获取secret
    String secret = null;
    if (cibinSecret != null && !cibinSecret.isExpire()) {
      secret = cibinSecret.getSecret();
    } else {
      // 发起远程访问获取合法的secret
      Map<String, String> secretRequest = Maps.newHashMap();
      secretRequest.put("chnid", cibinChannelId);
      JsonObject jsonObject = restTemplate
          .getForObject(getValidURL(CibinProxyConstant.SECRET_REQUEST_PATH), JsonObject.class,
              secretRequest);
      String codeVal = jsonObject.get(CibinProxyConstant.RESPONSE_CODE_NAME).getAsString();
      if (!CibinProxyConstant.SUCCESS_CODE.equalsIgnoreCase(codeVal)) {
        log.warn("刷新密钥接口出现未知错误，请求参数={}，响应参数={}", new Gson().toJson(secretRequest), jsonObject);
        return jsonObject;
      } else {
        JsonElement jsonElement = jsonObject.get(CibinProxyConstant.CONTENT_NAME);
        if (jsonElement == null || !jsonElement.isJsonObject() ||
            !jsonElement.getAsJsonObject().has(CibinProxyConstant.SECRET_NAME) || !jsonElement
            .getAsJsonObject().has(CibinProxyConstant.EXPIRE_TIME_NAME)) {
          log.warn("秘钥接口返回异常，无法获取到正确的data请求参数={}，响应参数={}", new Gson().toJson(secretRequest), jsonObject);
          jsonObject.addProperty(CibinProxyConstant.RESPONSE_CODE_NAME, REMOTE_API_ERROR);
          return jsonObject;
        }
        CibinSecret temp = new CibinSecret();
        temp.setExpiration(jsonElement.getAsJsonObject().getAsLong());
        temp.setSecret(jsonElement.getAsJsonObject().getAsString());
        cibinSecret = temp;
        secret = cibinSecret.getSecret();
      }
    }

    TreeMap treeMap = new TreeMap(preparedParam);
    treeMap.put(COMMON_PARAM_TIME, System.currentTimeMillis());
    treeMap.put(COMMON_PARAM_CHANNEL, cibinChannelId);
    URLCodec encoder = new URLCodec();
    StringBuilder queryParam = new StringBuilder("?");
    treeMap.forEach((k, v) -> {
      try {
        queryParam.append(k).append(encoder.encode(v)).append("&");
      } catch (EncoderException e) {
        // should not happen
        log.warn("encode error param={}", v, e);
        throw new BaseException(500, "不能编码参数：" + v, e);
      }
    });
    String confirmedParam = queryParam.toString();
    if (!isEmpty(confirmedParam)) {
      confirmedParam = confirmedParam.substring(0, queryParam.length() -1);
    }
    confirmedParam += secret;
    String sign = DigestUtils.md5Hex(confirmedParam).toUpperCase();
    confirmedParam += ("&sign=" + sign);
    String confirmedUrl = getValidURL(cibinApi.getRequestPath()) + confirmedParam;
    JsonObject jsonObject = restTemplate.getForObject(confirmedUrl, JsonObject.class);
    if ( !CibinProxyConstant.SUCCESS_CODE.equalsIgnoreCase(jsonObject.get(CibinProxyConstant.RESPONSE_CODE_NAME).getAsString())) {
      log.warn("接口返回异常，无法获取到正确返回请求参数={}，响应参数={}，requestURL={}", new Gson().toJson(treeMap), jsonObject, confirmedUrl);
    }
    return jsonObject;
  }

  private String getValidURL(String requestPath) {
    StringBuilder url = new StringBuilder(cibinHost);
    if (!cibinHost.endsWith("/")) {
      url.append("/");
    }
    if (requestPath.startsWith("/")) {
      url.append(requestPath.substring(1));
    }
    return url.toString().endsWith("/") ? url.toString().substring(0, url.length() -1) : url.toString();
  }

  @ToString
  static class CibinSecret {
    private String secret;
    private long expiration;

    public long getExpiration() {
      return expiration;
    }

    public void setExpiration(long expiration) {
      this.expiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiration) - EXPIRATION_AHEAD;
    }

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public boolean isExpire() {
      return System.currentTimeMillis() > expiration;
    }
  }

}
