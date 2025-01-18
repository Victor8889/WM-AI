package com.hncboy.beehive.web.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-8-23
 */
@Data
public class HaUserInfoQuery {
    @Schema(title = "积分")
    private String remainHoldBi;

    @Schema(title = "inviteEncode")
    private String inviteEncode;

    @Schema(title = "email")
    private String userName;

    @Schema(title = "validyDate")
    private Date validyDate;

    @Schema(title = "isVip")
    private String isVip;

    @Schema(title = "remainHoldCount")
    private String remainHoldCount;
}
