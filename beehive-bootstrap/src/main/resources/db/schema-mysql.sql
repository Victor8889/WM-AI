/*
 Navicat Premium Data Transfer
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bh_cell
-- ----------------------------
DROP TABLE IF EXISTS `bh_cell`;
CREATE TABLE `bh_cell`  (
                            `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
                            `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '封面',
                            `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '唯一编码',
                            `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，值大的排前面',
                            `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
                            `introduce` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '介绍',
                            `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                            `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                            `type` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ai类型',
                            PRIMARY KEY (`id`) USING BTREE,
                            UNIQUE INDEX `uq_code`(`code`) USING BTREE COMMENT '唯一编码'
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'cell 表实体类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_cell_config
-- ----------------------------
DROP TABLE IF EXISTS `bh_cell_config`;
CREATE TABLE `bh_cell_config`  (
                                   `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `cell_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cell code',
                                   `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
                                   `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '唯一编码，cell 中唯一',
                                   `default_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '默认值',
                                   `example_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '示例值',
                                   `is_required` tinyint(1) NOT NULL COMMENT '是否必填，0 否 1 是',
                                   `is_have_default_value` tinyint(1) NOT NULL COMMENT '是否有默认值，0 否 1 是',
                                   `is_user_can_use_default_value` tinyint(1) NOT NULL COMMENT '用户是否可以使用默认值，0 否 1 是',
                                   `is_user_visible` tinyint(1) NOT NULL COMMENT '用户是否可见，0 否 1 是',
                                   `is_user_value_visible` tinyint(1) NOT NULL COMMENT '用户是否可见默认值，0 否 1 是',
                                   `is_user_modifiable` tinyint(1) NOT NULL COMMENT '用户是否可修改，0 否 1 是',
                                   `is_user_live_modifiable` tinyint(1) NOT NULL COMMENT '用户创建房间后是否可修改，0 否 1 是',
                                   `introduce` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '介绍，用户端查看',
                                   `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注，管理端查看',
                                   `front_component_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '前端组件内容',
                                   `front_component_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '前端组件类型',
                                   `is_deleted` tinyint(0) NULL DEFAULT NULL COMMENT '是否删除 0 否 NULL 是',
                                   `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                   `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `uq_cell_key`(`cell_code`, `code`, `is_deleted`) USING BTREE COMMENT 'cell 配置项编码唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'cell 配置项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_cell_config_permission
-- ----------------------------
DROP TABLE IF EXISTS `bh_cell_config_permission`;
CREATE TABLE `bh_cell_config_permission`  (
                                              `id` bigint(0) NOT NULL COMMENT '主键',
                                              `user_id` int(0) NOT NULL COMMENT '用户 id',
                                              `cell_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cell code',
                                              `cell_config_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cell 配置项 code',
                                              `type` tinyint(0) NOT NULL COMMENT '权限类型',
                                              `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                              `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                              PRIMARY KEY (`id`) USING BTREE,
                                              UNIQUE INDEX `uq_user_cell_config`(`user_id`, `cell_code`, `cell_config_code`, `type`) USING BTREE COMMENT '用户 cell 配置项唯一'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'cell 配置项权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_cell_permission
-- ----------------------------
DROP TABLE IF EXISTS `bh_cell_permission`;
CREATE TABLE `bh_cell_permission`  (
                                       `id` bigint(0) NOT NULL COMMENT '主键',
                                       `user_id` int(0) NOT NULL COMMENT '用户 id',
                                       `cell_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cell code',
                                       `type` tinyint(0) NOT NULL COMMENT '类型',
                                       `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                       `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE INDEX `uq_user_cell`(`user_id`, `cell_code`, `type`) USING BTREE COMMENT '权限关系唯一'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'cell 权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_email_verify_code
-- ----------------------------
DROP TABLE IF EXISTS `bh_email_verify_code`;
CREATE TABLE `bh_email_verify_code`  (
                                         `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         `to_email_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '验证码接收邮箱地址',
                                         `verify_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '验证码',
                                         `is_used` tinyint(1) NOT NULL COMMENT '是否使用 0 否 1 是',
                                         `verify_ip` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '核销IP，方便识别一些机器人账号',
                                         `expire_at` datetime(0) NOT NULL COMMENT '验证码过期时间',
                                         `biz_type` tinyint(0) NOT NULL COMMENT '当前邮箱业务',
                                         `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                         `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                         PRIMARY KEY (`id`) USING BTREE,
                                         INDEX `uq_verify_code`(`verify_code`) USING BTREE COMMENT '防止验证码重复'
) ENGINE = InnoDB AUTO_INCREMENT = 351 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮箱验证码核销记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_front_user_base
-- ----------------------------
DROP TABLE IF EXISTS `bh_front_user_base`;
CREATE TABLE `bh_front_user_base`  (
                                       `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
                                       `status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
                                       `description` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
                                       `avatar_version` int(0) NOT NULL COMMENT '头像版本号',
                                       `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上一次登录 IP',
                                       `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                       `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                       `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9944 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '前端用户基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_front_user_extra_binding
-- ----------------------------
DROP TABLE IF EXISTS `bh_front_user_extra_binding`;
CREATE TABLE `bh_front_user_extra_binding`  (
                                                `id` int(0) NOT NULL AUTO_INCREMENT,
                                                `binding_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '绑定类型,qq,wechat,sina,github,email,phone',
                                                `extra_info_id` int(0) NOT NULL COMMENT '额外信息表ID',
                                                `base_user_id` int(0) NOT NULL COMMENT '基础用户表的ID',
                                                `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
                                                `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
                                                PRIMARY KEY (`id`) USING BTREE,
                                                UNIQUE INDEX `front_user_extra_binding_pk2`(`binding_type`, `base_user_id`, `extra_info_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 185 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '前端用户绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_front_user_extra_email
-- ----------------------------
DROP TABLE IF EXISTS `bh_front_user_extra_email`;
CREATE TABLE `bh_front_user_extra_email`  (
                                              `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                              `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱账号',
                                              `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加密后的密码',
                                              `salt` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加密盐',
                                              `verified` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否验证过，0 否 1 是',
                                              `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                              `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                              PRIMARY KEY (`id`) USING BTREE,
                                              UNIQUE INDEX `front_user_extra_email_pk2`(`username`) USING BTREE COMMENT '邮箱唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 240 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '前端用户邮箱登录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_openai_api_key
-- ----------------------------
DROP TABLE IF EXISTS `bh_openai_api_key`;
CREATE TABLE `bh_openai_api_key`  (
                                      `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'apiKey',
                                      `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求地址',
                                      `use_scenes` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '使用场景列表',
                                      `total_balance` decimal(10, 3) NOT NULL COMMENT '总额度（美元）',
                                      `usage_balance` decimal(10, 3) NOT NULL COMMENT '已使用额度（美元）',
                                      `remain_balance` decimal(10, 3) NOT NULL COMMENT '剩余额度（美元）',
                                      `balance_water_line` decimal(10, 3) NOT NULL COMMENT '余额水位线（美元）',
                                      `refresh_status_time` datetime(0) NOT NULL COMMENT '刷新状态时间',
                                      `refresh_balance_time` datetime(0) NOT NULL COMMENT '刷新余额时间',
                                      `is_refresh_balance` tinyint(0) NOT NULL COMMENT '是否刷新余额',
                                      `is_refresh_status` tinyint(0) NOT NULL COMMENT '是否刷新状态',
                                      `weight` int(0) NOT NULL COMMENT '权重，权重高的优先执行',
                                      `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态enable',
                                      `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备注',
                                      `update_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '更新理由',
                                      `error_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
                                      `version` int(0) NOT NULL COMMENT '版本',
                                      `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                      `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 620 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OpenAi ApiKey 表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room
-- ----------------------------
DROP TABLE IF EXISTS `bh_room`;
CREATE TABLE `bh_room`  (
                            `id` bigint(0) NOT NULL COMMENT '主键',
                            `user_id` int(0) NOT NULL COMMENT '用户 ID',
                            `color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颜色，十六进制',
                            `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
                            `pin_time` bigint(0) NOT NULL DEFAULT 0 COMMENT '固定时间戳',
                            `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ip',
                            `cell_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cell code',
                            `is_deleted` tinyint(0) NOT NULL COMMENT '是否删除 0 否 1 是',
                            `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                            `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `userid`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '房间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_bing
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_bing`;
CREATE TABLE `bh_room_bing`  (
                                 `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                 `user_id` int(0) NOT NULL COMMENT '用户 id',
                                 `mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing 模式',
                                 `conversation_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing conversationId',
                                 `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing clientId',
                                 `conversation_signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing conversationSignature',
                                 `max_num_user_messages_in_conversation` tinyint(0) NOT NULL COMMENT '最大提问次数',
                                 `num_user_messages_in_conversation` tinyint(0) NOT NULL COMMENT '累计提问次数',
                                 `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                 `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                 PRIMARY KEY (`room_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NewBing 房间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_bing_msg
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_bing_msg`;
CREATE TABLE `bh_room_bing_msg`  (
                                     `id` bigint(0) NOT NULL COMMENT '主键',
                                     `parent_message_id` bigint(0) NULL DEFAULT NULL COMMENT '父消息 id',
                                     `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                     `user_id` int(0) NOT NULL COMMENT '用户 id',
                                     `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip',
                                     `type` tinyint(0) NOT NULL COMMENT '消息类型',
                                     `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
                                     `mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing 模式',
                                     `conversation_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing conversationId',
                                     `client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing clientId',
                                     `conversation_signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'bing conversationSignature',
                                     `max_num_user_messages_in_conversation` tinyint(0) NOT NULL COMMENT '最大提问次数',
                                     `num_user_messages_in_conversation` tinyint(0) NOT NULL COMMENT '累计提问次数',
                                     `suggest_responses` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'bing 推荐提问',
                                     `source_attributions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'bing 数据来源',
                                     `is_new_topic` tinyint(0) NOT NULL COMMENT '是否新话题',
                                     `refresh_room_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '刷新房间原因',
                                     `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                     `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NewBing 房间消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_config_param
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_config_param`;
CREATE TABLE `bh_room_config_param`  (
                                         `id` bigint(0) NOT NULL COMMENT '主键',
                                         `user_id` int(0) NOT NULL COMMENT '用户 ID',
                                         `room_id` bigint(0) NOT NULL COMMENT '房间 ID',
                                         `cell_config_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置项 code',
                                         `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置项值',
                                         `is_deleted` tinyint(0) NULL DEFAULT NULL COMMENT '是否删除 0 否 NULL 是',
                                         `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                         `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                         PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '房间配置项参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_midjourney_msg
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_midjourney_msg`;
CREATE TABLE `bh_room_midjourney_msg`  (
                                           `id` bigint(0) NOT NULL COMMENT '主键',
                                           `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                           `user_id` int(0) NOT NULL COMMENT '用户 id',
                                           `type` tinyint(0) NOT NULL COMMENT '消息类型',
                                           `prompt` varchar(4900) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户输入',
                                           `final_prompt` varchar(4900) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最终的输入',
                                           `response_content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '响应内容',
                                           `action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指令动作',
                                           `compressed_image_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '压缩图名称',
                                           `original_image_name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '原图名称',
                                           `uv_parent_id` bigint(0) NULL DEFAULT NULL COMMENT 'uv 指令的父消息 id',
                                           `u_use_bit` int(0) NULL DEFAULT NULL COMMENT 'u 指令使用比特位',
                                           `uv_index` tinyint(1) NULL DEFAULT NULL COMMENT 'uv 位置',
                                           `status` tinyint(0) NOT NULL COMMENT '状态',
                                           `discord_finish_time` datetime(0) NULL DEFAULT NULL COMMENT 'discord 结束时间',
                                           `discord_start_time` datetime(0) NULL DEFAULT NULL COMMENT 'discord 开始时间',
                                           `discord_message_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'discord 消息 id',
                                           `discord_channel_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'discord 频道 id',
                                           `discord_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'discord 图片地址',
                                           `failure_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '失败原因',
                                           `is_deleted` tinyint(0) NOT NULL COMMENT '是否删除 0 否 1 是',
                                           `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                           `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                           `is_deducted` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否扣除积分',
                                           `progressing` int(2) UNSIGNED ZEROFILL NULL DEFAULT 00 COMMENT '进度',
                                           `base_img` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参考图',
                                           `duck_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'duck服务器ID',
                                           `params` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数信息',
                                           `mj_type` int(0) NULL DEFAULT NULL COMMENT 'mj类型，1fast2relax3turbo',
                                           `buttons` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'mj返回buttons信息',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           INDEX `userid`(`user_id`) USING BTREE,
                                           INDEX `status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Midjourney 房间消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_openai_chat_msg
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_openai_chat_msg`;
CREATE TABLE `bh_room_openai_chat_msg`  (
                                            `id` bigint(0) UNSIGNED NOT NULL COMMENT '主键',
                                            `user_id` int(0) NOT NULL COMMENT '用户 id',
                                            `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                            `parent_question_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级问题消息 id',
                                            `message_type` int(0) NOT NULL COMMENT '消息类型枚举',
                                            `model_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型名称',
                                            `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ApiKey',
                                            `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
                                            `original_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息的原始请求或响应数据',
                                            `response_error_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误的响应数据',
                                            `prompt_tokens` bigint(0) NULL DEFAULT NULL COMMENT '输入消息的 tokens',
                                            `completion_tokens` bigint(0) NULL DEFAULT NULL COMMENT '输出消息的 tokens',
                                            `total_tokens` bigint(0) NULL DEFAULT NULL COMMENT '累计 Tokens',
                                            `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip',
                                            `status` int(0) NOT NULL COMMENT '消息状态',
                                            `room_config_param_json` json NULL COMMENT '房间配置项参数 json',
                                            `create_time` timestamp(0) NOT NULL COMMENT '创建时间',
                                            `update_time` timestamp(0) NOT NULL COMMENT '更新时间',
                                            `is_deducted` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否扣除积分',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uq_parent_question_message_id`(`parent_question_message_id`) USING BTREE COMMENT '父消息只能有一个子消息',
                                            INDEX `status`(`status`) USING BTREE,
                                            INDEX `userid`(`user_id`) USING BTREE,
                                            INDEX `roomid`(`room_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OpenAi 对话房间消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_openai_chat_msg_copy1
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_openai_chat_msg_copy1`;
CREATE TABLE `bh_room_openai_chat_msg_copy1`  (
                                                  `id` bigint(0) UNSIGNED NOT NULL COMMENT '主键',
                                                  `user_id` int(0) NOT NULL COMMENT '用户 id',
                                                  `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                                  `parent_question_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级问题消息 id',
                                                  `message_type` int(0) NOT NULL COMMENT '消息类型枚举',
                                                  `model_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型名称',
                                                  `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ApiKey',
                                                  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
                                                  `original_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息的原始请求或响应数据',
                                                  `response_error_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误的响应数据',
                                                  `prompt_tokens` bigint(0) NULL DEFAULT NULL COMMENT '输入消息的 tokens',
                                                  `completion_tokens` bigint(0) NULL DEFAULT NULL COMMENT '输出消息的 tokens',
                                                  `total_tokens` bigint(0) NULL DEFAULT NULL COMMENT '累计 Tokens',
                                                  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip',
                                                  `status` int(0) NOT NULL COMMENT '消息状态',
                                                  `room_config_param_json` json NULL COMMENT '房间配置项参数 json',
                                                  `create_time` timestamp(0) NOT NULL COMMENT '创建时间',
                                                  `update_time` timestamp(0) NOT NULL COMMENT '更新时间',
                                                  `is_deducted` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否扣除积分',
                                                  PRIMARY KEY (`id`) USING BTREE,
                                                  UNIQUE INDEX `uq_parent_question_message_id`(`parent_question_message_id`) USING BTREE COMMENT '父消息只能有一个子消息',
                                                  INDEX `status`(`status`) USING BTREE,
                                                  INDEX `userid`(`user_id`) USING BTREE,
                                                  INDEX `roomid`(`room_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OpenAi 对话房间消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_openai_chat_web_msg
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_openai_chat_web_msg`;
CREATE TABLE `bh_room_openai_chat_web_msg`  (
                                                `id` bigint(0) NOT NULL COMMENT '主键',
                                                `request_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求的 messageId',
                                                `request_conversation_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求的 conversationId',
                                                `request_parent_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求的 parentMessageId',
                                                `user_id` int(0) NOT NULL COMMENT '用户 id',
                                                `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                                `message_type` int(0) NOT NULL COMMENT '消息类型枚举',
                                                `model_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型名称',
                                                `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
                                                `original_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息的原始请求或响应数据',
                                                `response_error_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误的响应数据',
                                                `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip',
                                                `status` int(0) NOT NULL COMMENT '消息状态',
                                                `room_config_param_json` json NULL COMMENT '房间配置项参数 json',
                                                `create_time` timestamp(0) NOT NULL COMMENT '创建时间',
                                                `update_time` timestamp(0) NOT NULL COMMENT '更新时间',
                                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OpenAi 对话 Web 房间消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_room_openai_image_msg
-- ----------------------------
DROP TABLE IF EXISTS `bh_room_openai_image_msg`;
CREATE TABLE `bh_room_openai_image_msg`  (
                                             `id` bigint(0) NOT NULL COMMENT '主键',
                                             `user_id` int(0) NOT NULL COMMENT '用户 id',
                                             `room_id` bigint(0) NOT NULL COMMENT '房间 id',
                                             `parent_question_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级问题消息 id',
                                             `message_type` int(0) NOT NULL COMMENT '消息类型枚举',
                                             `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ApiKey',
                                             `size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '尺寸大小',
                                             `prompt` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '输入内容',
                                             `openai_image_url` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'openai 图片地址',
                                             `image_name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图片名称',
                                             `original_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息的原始请求或响应数据',
                                             `response_error_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误的响应数据',
                                             `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip',
                                             `status` int(0) NOT NULL COMMENT '消息状态',
                                             `room_config_param_json` json NULL COMMENT '房间配置项参数 json',
                                             `create_time` timestamp(0) NOT NULL COMMENT '创建时间',
                                             `update_time` timestamp(0) NOT NULL COMMENT '更新时间',
                                             `is_deducted` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否扣除积分',
                                             `model` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型版本',
                                             PRIMARY KEY (`id`) USING BTREE,
                                             UNIQUE INDEX `uq_parent_question_message_id`(`parent_question_message_id`) USING BTREE COMMENT '父消息只能有一个子消息',
                                             INDEX `userid`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'OpenAi 图像房间消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `bh_sensitive_word`;
CREATE TABLE `bh_sensitive_word`  (
                                      `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '敏感词内容',
                                      `status` tinyint(0) NOT NULL COMMENT '状态 1 启用 2 停用',
                                      `is_deleted` tinyint(0) NULL DEFAULT 0 COMMENT '是否删除 0 否 NULL 是',
                                      `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                      `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE INDEX `uk_word`(`word`, `is_deleted`) USING BTREE COMMENT '敏感词唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 42151 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '敏感词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_sys_email_send_log
-- ----------------------------
DROP TABLE IF EXISTS `bh_sys_email_send_log`;
CREATE TABLE `bh_sys_email_send_log`  (
                                          `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                          `from_email_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发件人邮箱',
                                          `to_email_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收件人邮箱',
                                          `biz_type` int(0) NOT NULL COMMENT '业务类型',
                                          `request_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求 ip',
                                          `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送内容',
                                          `message_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送后会返回一个messageId',
                                          `status` tinyint(0) NOT NULL COMMENT '发送状态，0失败，1成功',
                                          `message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送后的消息，用于记录成功/失败的信息，成功默认为success',
                                          `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                          `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 495 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮箱发送日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_sys_front_user_login_log
-- ----------------------------
DROP TABLE IF EXISTS `bh_sys_front_user_login_log`;
CREATE TABLE `bh_sys_front_user_login_log`  (
                                                `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                `base_user_id` int(0) NOT NULL COMMENT '登录的基础用户ID',
                                                `login_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录方式（注册方式），邮箱登录，手机登录等等',
                                                `login_extra_info_id` int(0) NOT NULL COMMENT '登录信息ID与login_type有关联，邮箱登录时关联front_user_extra_email',
                                                `login_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录的IP地址',
                                                `login_status` tinyint(1) NOT NULL COMMENT '登录状态，1登录成功，0登录失败',
                                                `message` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结果，如果成功一律success；否则保存错误信息',
                                                `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1400 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '前端用户登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bh_sys_param
-- ----------------------------
DROP TABLE IF EXISTS `bh_sys_param`;
CREATE TABLE `bh_sys_param`  (
                                 `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
                                 `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'key',
                                 `param_value` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'value',
                                 `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
                                 `is_deleted` tinyint(0) NULL DEFAULT NULL COMMENT '是否删除 0 否 NULL 是',
                                 `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                 `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_key`(`param_key`, `is_deleted`) USING BTREE COMMENT 'key 唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_expense_records
-- ----------------------------
DROP TABLE IF EXISTS `ha_expense_records`;
CREATE TABLE `ha_expense_records`  (
                                       `id` int(0) NOT NULL AUTO_INCREMENT,
                                       `user_id` int(0) NULL DEFAULT NULL,
                                       `amount` int(0) NULL DEFAULT NULL COMMENT '消费金额',
                                       `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '消费时间',
                                       `model` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消费类型',
                                       `hold_bi` int(0) NULL DEFAULT NULL COMMENT 'hold币',
                                       `count` int(0) NULL DEFAULT NULL COMMENT '消费次数',
                                       `token` int(0) NULL DEFAULT NULL COMMENT '消费的token',
                                       `model_id` bigint(0) NULL DEFAULT NULL COMMENT '模型记录id',
                                       `mark` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                       PRIMARY KEY (`id`) USING BTREE,
                                       INDEX `userid`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 170917 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_free_points
-- ----------------------------
DROP TABLE IF EXISTS `ha_free_points`;
CREATE TABLE `ha_free_points`  (
                                   `id` int(0) NOT NULL,
                                   `user_id` int(0) NULL DEFAULT NULL,
                                   `create_time` timestamp(0) NULL DEFAULT NULL,
                                   `mark` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                   `points` int(0) NULL DEFAULT NULL,
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_invite
-- ----------------------------
DROP TABLE IF EXISTS `ha_invite`;
CREATE TABLE `ha_invite`  (
                              `id` int(0) NOT NULL AUTO_INCREMENT,
                              `user_id` int(0) NULL DEFAULT NULL COMMENT '用户id',
                              `create_time` timestamp(0) NULL DEFAULT NULL,
                              `mark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                              `invite_user_id` int(0) NULL DEFAULT NULL COMMENT '被邀请用户id',
                              `points` int(0) NULL DEFAULT NULL COMMENT '获得积分',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `userid`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 106393 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_notice
-- ----------------------------
DROP TABLE IF EXISTS `ha_notice`;
CREATE TABLE `ha_notice`  (
                              `id` int(0) NOT NULL,
                              `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                              `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                              `create_time` timestamp(0) NULL DEFAULT NULL,
                              `is_enable` int(0) NULL DEFAULT NULL,
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_product
-- ----------------------------
DROP TABLE IF EXISTS `ha_product`;
CREATE TABLE `ha_product`  (
                               `id` int(0) NOT NULL AUTO_INCREMENT,
                               `model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                               `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                               `records` int(0) NOT NULL,
                               `vip_records` int(0) NOT NULL,
                               `is_invalid` int(0) NULL DEFAULT NULL,
                               `create_time` timestamp(0) NULL DEFAULT NULL,
                               `descript` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                               `show_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                               `api_model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                               `is_chat` int(0) NULL DEFAULT NULL COMMENT '1表示chat',
                               `show_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                               `max_tokens` int(0) NULL DEFAULT NULL,
                               `use_scene` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                               `is_context` int(0) NULL DEFAULT NULL COMMENT '>0表示关联上下文，是几就是是几个<=0表示不关联--强制性,为空则不影响',
                               `is_upload` int(0) NULL DEFAULT NULL COMMENT '1识图2识文档3识视频，，，向上兼容',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 206 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_qr
-- ----------------------------
DROP TABLE IF EXISTS `ha_qr`;
CREATE TABLE `ha_qr`  (
                          `id` bigint(0) NOT NULL AUTO_INCREMENT,
                          `model` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `user_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `art_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `style` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `version` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `create_time` timestamp(0) NULL DEFAULT NULL,
                          `update_time` timestamp(0) NULL DEFAULT NULL,
                          `qr_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `prompt` varchar(222) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `barcode` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `user_id` bigint(0) NULL DEFAULT NULL,
                          `proportion` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `qr_format` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `is_completed` int(0) NULL DEFAULT NULL COMMENT '创建状态完成状态，0等待，5进行中，1完成，2出错',
                          `status` int(0) NULL DEFAULT NULL COMMENT '状态，是否删除0不删除，-1删除',
                          `img_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                          `code` int(0) NULL DEFAULT NULL,
                          `is_deducted` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否扣除积分',
                          PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_recharge_records
-- ----------------------------
DROP TABLE IF EXISTS `ha_recharge_records`;
CREATE TABLE `ha_recharge_records`  (
                                        `id` int(0) NOT NULL AUTO_INCREMENT,
                                        `user_id` int(0) NULL DEFAULT NULL,
                                        `amount` int(0) NULL DEFAULT NULL COMMENT '充值金额',
                                        `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '订单创建时间',
                                        `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '支付时间',
                                        `status` int(0) NULL DEFAULT NULL COMMENT '订单状态0未支付1成功2失败3异常4关闭',
                                        `mark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                        `points` int(0) NULL DEFAULT NULL,
                                        `type` int(0) NULL DEFAULT NULL COMMENT '支付类型',
                                        `order_id` varchar(122) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                        `pay_mark` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                        `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                        `buyer_logon_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                        `is_add_points` int(1) UNSIGNED ZEROFILL NULL DEFAULT NULL,
                                        `pay_Result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'payResult',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        INDEX `userid`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 121085 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_shops
-- ----------------------------
DROP TABLE IF EXISTS `ha_shops`;
CREATE TABLE `ha_shops`  (
                             `id` int(0) NOT NULL,
                             `points` int(0) NOT NULL,
                             `amount` int(0) NOT NULL,
                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                             `label` varchar(90) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                             `enable` int(0) NULL DEFAULT NULL COMMENT '1可用',
                             `create_time` timestamp(0) NULL DEFAULT NULL,
                             `eachpoint` float NULL DEFAULT NULL,
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_user_param
-- ----------------------------
DROP TABLE IF EXISTS `ha_user_param`;
CREATE TABLE `ha_user_param`  (
                                  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `userid` int(0) NOT NULL COMMENT '用户id',
                                  `apikey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户自己的key',
                                  `max_tokens` int(0) NULL DEFAULT NULL COMMENT '最大tokens',
                                  `temperature` float NULL DEFAULT NULL COMMENT '随机性',
                                  `key_strategy` varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '策略。。。弃用',
                                  `context_count` int(0) NULL DEFAULT NULL COMMENT '上下文数量',
                                  `system_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认消息',
                                  `presence_penalty` float NULL DEFAULT NULL COMMENT '话题新鲜度',
                                  `context_related_time_hour` int(0) NULL DEFAULT NULL,
                                  `enable_local_sensitive_word` int(0) NULL DEFAULT NULL COMMENT '是否启用本地敏感词监测，1启用，2不启用，没用',
                                  `create_time` timestamp(0) NOT NULL,
                                  `update_time` timestamp(0) NOT NULL,
                                  `chat_model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型',
                                  `is_context` int(0) NULL DEFAULT NULL COMMENT '是否使用上下文0不1使用',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `userid`(`userid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 179 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_user_permissions
-- ----------------------------
DROP TABLE IF EXISTS `ha_user_permissions`;
CREATE TABLE `ha_user_permissions`  (
                                        `id` int(0) NOT NULL AUTO_INCREMENT,
                                        `user_id` int(0) NULL DEFAULT NULL,
                                        `permission_level` int(0) NULL DEFAULT NULL,
                                        `is_enable` int(0) NULL DEFAULT NULL COMMENT '是否可用1，可；2不可',
                                        `remain_hold_bi` int(0) NULL DEFAULT NULL COMMENT '剩余积分',
                                        `remain_hold_count` int(0) NULL DEFAULT NULL COMMENT '基础对话积分',
                                        `recharge_count` int(0) NULL DEFAULT NULL COMMENT '充值次数',
                                        `validy_date` timestamp(0) NULL DEFAULT NULL COMMENT '有效期',
                                        `is_count` int(0) NULL DEFAULT NULL COMMENT '次卡，1是，2不是',
                                        `is_vip` int(0) NULL DEFAULT NULL COMMENT '会员卡，1是，2不是',
                                        `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
                                        `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
                                        `invite_encode` int(0) NULL DEFAULT NULL COMMENT '自己的邀请码',
                                        `other_user_id` int(0) NULL DEFAULT NULL COMMENT '邀请自己的人',
                                        `vip_time` timestamp(0) NULL DEFAULT NULL,
                                        `is_giving` int(0) NULL DEFAULT NULL COMMENT '是否已经赠送了1是，2不是',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        INDEX `userid`(`user_id`) USING BTREE,
                                        INDEX `invite_encode`(`invite_encode`) USING BTREE,
                                        INDEX `otherUserId`(`other_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 169 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ha_vip_price
-- ----------------------------
DROP TABLE IF EXISTS `ha_vip_price`;
CREATE TABLE `ha_vip_price`  (
                                 `id` int(0) NOT NULL AUTO_INCREMENT,
                                 `mark` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                 `price` int(0) NULL DEFAULT NULL,
                                 `duration` int(0) NULL DEFAULT NULL COMMENT '天数',
                                 `create_time` timestamp(0) NULL DEFAULT NULL,
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
