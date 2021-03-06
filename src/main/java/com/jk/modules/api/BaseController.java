package com.jk.modules.api;

import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author
 * Created by JK on 2017/3/10.
 */
@RequestMapping("/api/v1")
public class BaseController {

    protected final transient Log log = LogFactory.get(this.getClass());

    protected final transient String API_URL = "http://localhost/api/v1";

    protected final String MESSAGE_NAME = "msg";
    protected final String CODE_NAME = "code";

    /**
     * 默认页为1
     */
    protected static final Integer PAGENUM = 1;
    /**
     * 页码大小10
     */
    protected static final Integer PAGESIZE = 10;
}
