package com.hncboy.beehive.web.domain.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-10
 */
@Data
public class HaInviteVo {

        private int id; // 主键


        private String mark;


        private Integer points;

        private Date createTime; // 创建时间


}
