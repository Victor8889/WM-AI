package com.hncboy.beehive.cell.openai.controller;

import com.hncboy.beehive.base.domain.query.RoomQrQuery;
import com.hncboy.beehive.base.handler.response.R;
import com.hncboy.beehive.web.domain.vo.RoomHaQrInfoVO;
import com.hncboy.beehive.web.domain.vo.RoomShowHaQrInfoVO;
import com.hncboy.beehive.web.service.RoomQrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ll
 * @date 2023/7/31
 * OpenAi 对话房间控制器
 */
@AllArgsConstructor
@Tag(name = "OpenAi 对话房间相关接口")
@RequestMapping("/room/qr")
@RestController
public class RoomQrController {

    private final RoomQrService roomQrService;

    @Operation(summary = "消息列表")
    @GetMapping("/list")
    public R<List<RoomShowHaQrInfoVO>> list(@Validated RoomQrQuery cursorQuery) {
        return R.data(roomQrService.list(cursorQuery));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/add")
    public R<Boolean> send(@RequestBody RoomHaQrInfoVO sendRequest) {
        if(roomQrService.send(sendRequest))
            return R.success("更新成功");
        else
        return  R.fail("更新失败");
    }
    @Operation(summary = "删除消息")
    @DeleteMapping("/delete")
    public R<Boolean>  delete(@RequestParam String id) {

        return R.data(roomQrService.delete(id));
    }
}
