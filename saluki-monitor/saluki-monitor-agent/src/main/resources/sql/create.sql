SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `saluki_invoke`;
CREATE TABLE `saluki_invoke` (
  `id` varchar(255) NOT NULL DEFAULT '',
  `invoke_date` datetime DEFAULT NULL,
  `service` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `consumer` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT '',
  `invoke_time` datetime DEFAULT NULL,
  `success` int(11) DEFAULT NULL,
  `failure` int(11) DEFAULT NULL,
  `elapsed` int(11) DEFAULT NULL,
  `concurrent` int(11) DEFAULT NULL,
  `max_elapsed` int(11) DEFAULT NULL,
  `max_concurrent` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_service` (`service`) USING BTREE,
  KEY `index_method` (`method`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

SET FOREIGN_KEY_CHECKS = 1;
