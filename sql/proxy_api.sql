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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8