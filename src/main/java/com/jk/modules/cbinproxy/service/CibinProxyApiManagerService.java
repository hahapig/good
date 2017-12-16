package com.jk.modules.cbinproxy.service;

import com.github.pagehelper.PageInfo;
import com.jk.common.base.service.BaseService;
import com.jk.modules.cbinproxy.model.CibinApi;

/**
 * Created by tao on 17-12-17.
 */
public interface CibinProxyApiManagerService extends BaseService<CibinApi> {

    PageInfo<CibinApi> findPage(Integer pageNum, Integer pageSize) throws Exception;
}
