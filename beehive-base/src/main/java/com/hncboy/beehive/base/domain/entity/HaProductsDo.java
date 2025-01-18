package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-10
 */
@TableName("ha_product")
@Data
public class HaProductsDo {

        @TableId(type = IdType.AUTO)
        private Integer id; // 主键

        private String model;
        private String name;
        private String showName;
        private Integer records;
        private Integer vipRecords;
        private Integer isInvalid;
        //private String apiModel;


        @TableField(fill = FieldFill.INSERT)
        private Date createTime; // 创建时间
        private String descript;


        private Integer isChat;
        private String apiModel;
        private String showKey;
        private String useScene;
        private Integer maxTokens;
        private Integer isContext;//>0表示关联上下文，是几就是是几个<=0表示不关联--强制性





}
