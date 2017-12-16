package com.jk.modules.cbinproxy.model;

import com.jk.common.base.model.BaseEntity;
import lombok.Data;

/**
 * Created by tao on 17-12-17.
 */
@Data
public class CibinApi extends BaseEntity{

    private String apiName;
    private String method = "GET";
    private String paramNames;
    private String checkedParams;
    private Boolean securityCheck;
    private Boolean active = true;
    private String dependency;
    private String requestPath;

}
