CREATE TABLE `cibin_api` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `api_name` varchar(100) NOT NULL,
  `method` varchar(10) NOT NULL,
  `param_names` varchar(600) DEFAULT NULL,
  `checked_params` varchar(500) DEFAULT NULL,
  `security_check` tinyint(4) NOT NULL DEFAULT '1',
  `active` tinyint(4) NOT NULL DEFAULT '1',
  `dependency` varchar(50) DEFAULT NULL,
  `request_path` varchar(45) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `modify_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `api_name_UNIQUE` (`api_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

CREATE TABLE `cibin_proxy_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_name` varchar(50) NOT NULL COMMENT '访问接口名称',
  `trace_id` varchar(80) DEFAULT NULL COMMENT '请求的唯一id',
  `client_identity` varchar(100) DEFAULT NULL COMMENT '终端唯一标示',
  `request_param` varchar(800) DEFAULT NULL COMMENT '请求参数',
  `request_url` varchar(300) DEFAULT NULL COMMENT '(全路径)',
  `response` varchar(500) DEFAULT NULL COMMENT '响应参数',
  `error_code` varchar(100) DEFAULT NULL COMMENT '错误代码',
  `exception_stack_trace` varchar(5000) DEFAULT NULL COMMENT '异常栈',
  `request_ip` varchar(32) DEFAULT NULL COMMENT '客户端IP',
  `time_consuming` bigint DEFAULT NULL COMMENT '时间消耗',
  `user_agent` varchar(2000) DEFAULT NULL COMMENT '请求的userAgent',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='cibin代理请求日志表';
