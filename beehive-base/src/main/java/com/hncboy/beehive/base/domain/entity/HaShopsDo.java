package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-9-12
 */
@TableName("ha_shops")
@Data
public class HaShopsDo {
    @TableId(type = IdType.AUTO)
    private Integer id; // 主键

    private Integer points;

    private String name;

    private Integer amount;

    private Integer enable;
    private String label;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime; // 创建时间
}
