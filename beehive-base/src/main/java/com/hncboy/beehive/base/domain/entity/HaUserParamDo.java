package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-24
 */
@TableName("ha_user_param")
@Data
public class HaUserParamDo {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userid;

    /**
     * 用户自己的key
     */
    private String apikey;

    /**
     * 最大tokens
     */
    private Integer maxTokens;

    /**
     * 随机性
     */
    private Float temperature;

    /**
     * 策略。。。
     */
    private String keyStrategy;

    /**
     * 上下文数量
     */
    private Integer contextCount;

    /**
     * 默认消息
     */
    private String systemMessage;

    /**
     * 话题新鲜度
     */
    private Float presencePenalty;

    /**
     * 上下文相关时间（小时）
     */
    private Integer contextRelatedTimeHour;

    /**
     * 是否启用本地敏感词，1启用，2不启用
     */
    private Integer enableLocalSensitiveWord;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 模型
     */
    private String chatModel;

    /**
     * 是否使用 上下文
     * 0使用
     * 1不使用
     */
    private Integer isContext;
}
