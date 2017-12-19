package com.jk.modules.cbinproxy.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jk.common.base.service.impl.BaseServiceImpl;
import com.jk.common.exception.BaseException;
import com.jk.modules.cbinproxy.model.CibinApi;
import com.sun.javafx.fxml.builder.URLBuilder;
import com.xiaoleilu.hutool.json.JSONObject;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
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
public class CibinProxyApiManagerServiceImpl extends BaseServiceImpl<CibinApi> implements CibinProxyApiManagerService{
    private volatile CibinSecret cibinSecret = null;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${cibinHost}")
    private String cibinHost;

    @Value("${cibinChannelId}")
    private String cibinChannelId;

    public PageInfo<CibinApi> findPage(Integer pageNum , Integer pageSize) throws Exception {
        Example example = new Example(CibinApi.class);
        PageHelper.startPage(pageNum,pageSize);
        List<CibinApi> cibinApiList = this.selectByExample(example);
        return new PageInfo<>(cibinApiList);
    }

    @Override
    public Map<String, String> requestRemote(Map<String, String> preparedParam, CibinApi cibinApi) {
        // 1.获取secret
        String secret = null;
        if (cibinSecret != null && !cibinSecret.isExpire()) {
            secret = cibinSecret.getSecret();
        } else {
            // 发起远程访问获取合法的secret
            Map<String, String> params = Maps.newHashMap();
            params.put("chnid", cibinChannelId);
            JsonObject jsonObject = restTemplate.getForObject(getValidURL(CibinProxyConstant.SECRET_REQUEST_PATH), JsonObject.class, params);
            String codeVal = jsonObject.get(CibinProxyConstant.RESPONSE_CODE_NAME).getAsString();
            if (!CibinProxyConstant.SUCCESS_CODE.equalsIgnoreCase(codeVal)) {
                return jsonObject.;
            }


        }


        TreeMap treeMap = new TreeMap(preparedParam);
        URLCodec encoder = new URLCodec();
        StringBuilder queryParam = new StringBuilder("?");
        treeMap.forEach((k,v)->{
            try {
                queryParam.append(k).append(encoder.encode(v)).append("&");
            } catch (EncoderException e) {
                // should not happen
                log.warn("encode error param={}",v, e);
                throw new BaseException(500, "不能编码参数：" + v, e);
            }
        });
        return null;
    }

    private String getValidSecret() {


    }

    private String getValidURL(String requestPath){
        StringBuilder url = new StringBuilder(cibinHost);
        if (!cibinHost.endsWith("/")) {
            url.append("/");
        }
        if (requestPath.startsWith("/")) {
            url.append(requestPath.substring(1));
        }
        return url.toString();
    }

    @Data
    static class CibinSecret {
        private String secret;
        private long expiration;

        public boolean isExpire(){
           return System.currentTimeMillis() > expiration;
        }
    }

}
