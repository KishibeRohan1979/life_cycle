/*
 Navicat Premium Data Transfer

 Source Server         : MySQL-localhost
 Source Server Type    : MySQL
 Source Server Version : 50737
 Source Host           : localhost:3306
 Source Schema         : life_cycle

 Target Server Type    : MySQL
 Target Server Version : 50737
 File Encoding         : 65001

 Date: 27/07/2023 14:15:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for life_cycle_tactics
-- ----------------------------
DROP TABLE IF EXISTS `life_cycle_tactics`;
CREATE TABLE `life_cycle_tactics`  (
  `tactics_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '策略（该）表主键id',
  `scheduler_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '定时任务id，该字段是唯一的（约定使用（表名+主键名+主键值）当作定时任务id）',
  `tactics_type` int(11) NULL DEFAULT NULL COMMENT '策略类型（1、根据使用频率的周期；2、根据固定周期；）',
  `create_time` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
  `last_access_time` bigint(20) NULL DEFAULT NULL COMMENT '最后访问时间',
  `deadline` bigint(20) NULL DEFAULT NULL COMMENT '存储周期（-1表示永久）',
  `execution_time` bigint(20) NULL DEFAULT NULL COMMENT '执行时间',
  `access_address` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '访问地址（包括关系型数据库表名、ES索引、文件路径等）',
  `detailed_data` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '某个对象的详细数据（转json）',
  `data_object_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '这个对象的类型（一般都是自定义对象，但是也要写，后续可以转回去）',
  `primary_key_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主键（关系型数据库表的主键、es索引的_id、文件的具体文件名）',
  `primary_key_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主键在（Java）程序中的数据类型',
  `primary_key_value` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '主键的值',
  `data_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问数据的类型（数据表、索引、文件夹统一写“list”；具体数据行、文档、文件用“item”表示）',
  PRIMARY KEY (`tactics_id`) USING BTREE,
  UNIQUE INDEX `scheduler_id`(`scheduler_id`) USING BTREE COMMENT '定时id应该唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for life_cycle_test
-- ----------------------------
DROP TABLE IF EXISTS `life_cycle_test`;
CREATE TABLE `life_cycle_test`  (
  `test_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '这个表的主键id',
  `user_name` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `synopsis` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '简介',
  PRIMARY KEY (`test_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
