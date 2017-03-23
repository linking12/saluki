use gateway;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `zuul_routes`;

CREATE TABLE `zuul_routes` (
    `id` int NOT NULL AUTO_INCREMENT,
    `route_id` varchar(500) DEFAULT NULL,
    `path` varchar(500) DEFAULT NULL,
    `service_id` varchar(500) DEFAULT NULL,
    `url` varchar(500) DEFAULT NULL,
    `strip_prefix` BOOL DEFAULT NULL,
    `retryable` BOOL DEFAULT NULL,
    `sensitiveHeaders` varchar(500) DEFAULT NULL,
     PRIMARY KEY (`id`)
);
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `enabled` BOOL DEFAULT NULL,
   PRIMARY KEY (`id`)
);
CREATE TABLE `oauth_access_token` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_name` varchar(500) DEFAULT NULL,
    `token_id` varchar(500) DEFAULT NULL,
     PRIMARY KEY (`id`)
);