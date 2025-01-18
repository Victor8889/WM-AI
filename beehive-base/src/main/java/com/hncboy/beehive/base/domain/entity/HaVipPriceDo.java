package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-10
 */
@TableName("ha_vip_price")
@Data
public class HaVipPriceDo {

        @TableId(type = IdType.AUTO)
        private Integer id; // 主键

        private String mark;
        private Integer price;
        private Integer duration;


        @TableField(fill = FieldFill.INSERT)
        private Date createTime; // 创建时间


}
