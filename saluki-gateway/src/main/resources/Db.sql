use gateway;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `authority`;
DROP TABLE IF EXISTS `user_authority`;
DROP TABLE IF EXISTS `oauth_access_token`;
DROP TABLE IF EXISTS `oauth_refresh_token`;
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
    `is_grpc` BOOL DEFAULT 0,
    `group` varchar(100) DEFAULT NULL,
    `version` varchar(100) DEFAULT NULL,
    `service_name` varchar(500) DEFAULT NULL,
    `method` varchar(100) DEFAULT NULL,
     PRIMARY KEY (`id`)
);
CREATE TABLE `api-jar` (
    `id` int NOT NULL AUTO_INCREMENT,
    `jar_version` varchar(100) DEFAULT NULL,
    `jar_url` varchar(100) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
     PRIMARY KEY (`id`)
);

CREATE TABLE user (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  email VARCHAR(50),
  password VARCHAR(500),
  activated BOOLEAN DEFAULT FALSE,
  activationkey VARCHAR(50) DEFAULT NULL,
  resetpasswordkey VARCHAR(50) DEFAULT NULL,
  intervalInMills BIGINT(10) DEFAULT NULL,
  limit INT DEFAULT NULL
);

CREATE TABLE authority (
  name VARCHAR(50) NOT NULL PRIMARY KEY
);

CREATE TABLE user_authority (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES user (username),
    FOREIGN KEY (authority) REFERENCES authority (name),
    UNIQUE INDEX user_authority_idx_1 (username, authority)
);

CREATE TABLE oauth_access_token (
  token_id VARCHAR(256) DEFAULT NULL,
  token BLOB,
  authentication_id VARCHAR(256) DEFAULT NULL,
  user_name VARCHAR(256) DEFAULT NULL,
  client_id VARCHAR(256) DEFAULT NULL,
  authentication BLOB,
  refresh_token VARCHAR(256) DEFAULT NULL
);

CREATE TABLE oauth_refresh_token (
  token_id VARCHAR(256) DEFAULT NULL,
  token BLOB,
  authentication BLOB
);


INSERT INTO user (username,email, password, activated) VALUES ('admin', 'admin@mail.me', 'b8f57d6d6ec0a60dfe2e20182d4615b12e321cad9e2979e0b9f81e0d6eda78ad9b6dcfe53e4e22d1', true);
INSERT INTO user (username,email, password, activated) VALUES ('user', 'user@mail.me', 'b8f57d6d6ec0a60dfe2e20182d4615b12e321cad9e2979e0b9f81e0d6eda78ad9b6dcfe53e4e22d1', true);

INSERT INTO authority (name) VALUES ('ROLE_USER');
INSERT INTO authority (name) VALUES ('ROLE_ADMIN');

INSERT INTO user_authority (username,authority) VALUES ('user', 'ROLE_USER');
INSERT INTO user_authority (username,authority) VALUES ('admin', 'ROLE_USER');
INSERT INTO user_authority (username,authority) VALUES ('admin', 'ROLE_ADMIN');