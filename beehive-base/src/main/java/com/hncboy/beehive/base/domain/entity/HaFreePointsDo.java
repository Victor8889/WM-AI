package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-10
 */
@TableName("ha_free_points")
@Data
public class HaFreePointsDo {

        @TableId(type = IdType.AUTO)
        private Integer id; // 主键

        private Integer userId;

        private String mark;

        private Integer points;

        @TableField(fill = FieldFill.INSERT)
        private Date createTime; // 创建时间


}
