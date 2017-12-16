package com.jk.modules.cbinproxy.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jk.common.base.service.impl.BaseServiceImpl;
import com.jk.modules.cbinproxy.model.CibinApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by tao on 17-12-17.
 */
@Transactional
@Service
public class CibinProxyApiManagerServiceImpl extends BaseServiceImpl<CibinApi> implements CibinProxyApiManagerService{

    public PageInfo<CibinApi> findPage(Integer pageNum , Integer pageSize) throws Exception {
        Example example = new Example(CibinApi.class);
        PageHelper.startPage(pageNum,pageSize);
        List<CibinApi> cibinApiList = this.selectByExample(example);

        return new PageInfo<>(cibinApiList);
    }

}
