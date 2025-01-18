package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-10
 */
@TableName("ha_qr")
@Data
public class HaQrInfoDo {

        @TableId(type = IdType.AUTO)
        private Integer id; // 主键

        private String qrFormat; // 二维码样式
        private String model; // 风格

        private String userUrl; // 用户URL

        private String description; // 描述

        private String artNumber; // 艺术编号

        private Integer isCompleted;//完成状态，0等待，1进行中，2完成，3出错
        private Integer status; //0不删除，-1删除
        private String style; // 风格
        private String version; // 版本

        @TableField(fill = FieldFill.INSERT)
        private Date createTime; // 创建时间

        @TableField(fill = FieldFill.INSERT_UPDATE)
        private Date updateTime; // 更新时间

        private String qrUrl; // 二维码URL

        private String prompt; // 提示
        private String barcode; // 码眼

        private Integer userId; // 用户ID

        private String proportion; // 比例

        private String imgUuid; // 二维码uuid
        private Integer code; // 二维码格式 0正常
        private Integer isDeducted; // 二维码格式 0正常

}
