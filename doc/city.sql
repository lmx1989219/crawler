/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50717
Source Host           : 127.0.0.1:3306
Source Database       : test

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2019-08-21 13:28:05
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for city
-- ----------------------------
DROP TABLE IF EXISTS `city`;
CREATE TABLE `city` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of city
-- ----------------------------
INSERT INTO `city` VALUES ('1', 'bj', '北京');
INSERT INTO `city` VALUES ('178', 'sz', '深圳');
INSERT INTO `city` VALUES ('540', 'sh', '上海');
INSERT INTO `city` VALUES ('600', 'hz', '杭州');
INSERT INTO `city` VALUES ('1207', 'tj', '天津');
INSERT INTO `city` VALUES ('1366', 'wh', '武汉');
INSERT INTO `city` VALUES ('1622', 'nj', '南京');
INSERT INTO `city` VALUES ('1781', 'gz', '广州');
INSERT INTO `city` VALUES ('2232', 'cd', '成都');
INSERT INTO `city` VALUES ('3043', 'gs', '苏州');
INSERT INTO `city` VALUES ('3323', 'wx', '无锡');
INSERT INTO `city` VALUES ('3328', 'xa', '西安');
INSERT INTO `city` VALUES ('3324', 'cq', '重庆');
INSERT INTO `city` VALUES ('6000', 'jx', '嘉兴');
