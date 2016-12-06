DROP TABLE IF EXISTS `saluki_invoke`;
CREATE TABLE `saluki_invoke` (
  `id` varchar(255),
  `invokeDate` datetime DEFAULT NULL,
  `application` varchar(255) DEFAULT NULL,
  `service` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `consumer` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT '',
  `success` int(11) DEFAULT NULL,
  `failure` int(11) DEFAULT NULL,
  `elapsed` int(11) DEFAULT NULL,
  `concurrent` int(11) DEFAULT NULL,
  `maxInput` int(11) DEFAULT NULL,
  `maxOutput` int(11) DEFAULT NULL,
  `maxElapsed` double DEFAULT NULL,
  `maxConcurrent` int(11) DEFAULT NULL,
  `input` double DEFAULT NULL,
  `output` double DEFAULT NULL,
  PRIMARY KEY (`id`)
);