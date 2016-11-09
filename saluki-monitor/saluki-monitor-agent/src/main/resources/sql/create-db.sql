DROP TABLE IF EXISTS `saluki_invoke`;
CREATE TABLE `saluki_invoke` (
  `id` varchar(255) PRIMARY KEY,
  `invoke_date` datetime DEFAULT NULL,
  `service` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `consumer` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT '',
  `invoke_time` bigint(20) DEFAULT NULL,
  `success` int(11) DEFAULT NULL,
  `failure` int(11) DEFAULT NULL,
  `elapsed` int(11) DEFAULT NULL,
  `concurrent` int(11) DEFAULT NULL,
  `inPutParam` BINARY(5000) DEFAULT NULL,
  `outPutParam` BINARY(5000) DEFAULT NULL,
  PRIMARY KEY (`id`)
);