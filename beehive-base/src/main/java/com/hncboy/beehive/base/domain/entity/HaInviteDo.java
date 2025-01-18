package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-10
 */
@TableName("ha_invite")
@Data
public class HaInviteDo {

        @TableId(type = IdType.AUTO)
        private Integer id; // 主键

        private Integer userId;

        private String mark;

        private Integer inviteUserId;

        private Integer points;

        @TableField(fill = FieldFill.INSERT)
        private Date createTime; // 创建时间


}
