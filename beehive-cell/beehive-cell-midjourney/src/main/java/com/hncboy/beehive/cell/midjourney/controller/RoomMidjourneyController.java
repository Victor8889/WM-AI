package com.hncboy.beehive.cell.midjourney.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hncboy.beehive.base.domain.query.MjPageQuery;
import com.hncboy.beehive.base.enums.MjMsgActionEnum;
import com.hncboy.beehive.base.handler.response.R;
import com.hncboy.beehive.cell.core.annotation.CellConfigCheck;
import com.hncboy.beehive.cell.midjourney.domain.request.MjConvertRequest;
import com.hncboy.beehive.cell.midjourney.domain.request.MjDescribeRequest;
import com.hncboy.beehive.cell.midjourney.domain.request.MjImagineRequest;
import com.hncboy.beehive.cell.midjourney.domain.vo.RoomMidjourneyMsgVO;
import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author ll
 * @date 2023/5/18
 * Midjourney 房间控制器
 */
@AllArgsConstructor
@Tag(name = "Midjourney 房间相关接口")
@RequestMapping("/room/midjourney")
@RestController
public class RoomMidjourneyController {

    private final RoomMidjourneyMsgService roomMidjourneyMsgService;

    @Operation(summary = "消息列表")
    @GetMapping("/list")
    public R<List<RoomMidjourneyMsgVO>> list(@RequestParam @Parameter(description = "MJ 快慢") Integer mjType) {    //@Validated RoomMsgCursorQuery cursorQuery
        return R.data(roomMidjourneyMsgService.listMj(mjType));
    }
    @Operation(summary = "消息列表")
    @GetMapping("/page")
    public R<IPage<RoomMidjourneyMsgVO>> pageMj(@Validated MjPageQuery mjPageQuery) {
        return R.data(roomMidjourneyMsgService.pageMj(mjPageQuery));
    }

    @Operation(summary = "消息详情")
    @GetMapping("/detail")
    public R<RoomMidjourneyMsgVO> detail(@RequestParam @Parameter(description = "消息 id") Long msgId) {
        return R.data(roomMidjourneyMsgService.detail(msgId));
    }
    @Operation(summary = "消息详情")
    @GetMapping("/delete/{id}")
    public Boolean delete(@PathVariable @Parameter(description = "消息 id") Long id) {
        return roomMidjourneyMsgService.delete(id);
    }

    /**
     * 文生图
     * @param imagineRequest
     * @return 暂时不用了
     */
    @CellConfigCheck(roomId = "#imagineRequest.roomId")
    @Operation(summary = "imagine")
    @PostMapping("/imagine")
    public R<Boolean> imagine(@Validated @RequestBody MjImagineRequest imagineRequest) {
        roomMidjourneyMsgService.imagine(imagineRequest);
        return R.data(true);
    }
    /**
     * 文生图--版本2，只是添加任务到数据库
     * @param imagineRequest
     * @return
     */
    @CellConfigCheck(roomId = "#imagineRequest.roomId")
    @Operation(summary = "imagine")
    @PostMapping("/image")
    public R<RoomMidjourneyMsgVO> image(@Validated @RequestBody MjImagineRequest imagineRequest) {
        //roomMidjourneyMsgService.imagine(imagineRequest);
        return R.data(roomMidjourneyMsgService.image(imagineRequest));
    }

    /**
     * 放大
     * @param convertRequest
     * @return
     */
    @CellConfigCheck(roomId = "#convertRequest.roomId")
    @Operation(summary = "upscale")
    @PostMapping("/upscale")
    public R<Boolean> upscale(@Validated @RequestBody MjConvertRequest convertRequest) {
        roomMidjourneyMsgService.upscale(convertRequest);
        return R.data(true);
    }

    /**
     * 仿制
     * @param convertRequest
     * @return
     */
    @CellConfigCheck(roomId = "#convertRequest.roomId")
    @Operation(summary = "variation")
    @PostMapping("/variation")
    public R<Boolean> variation(@Validated @RequestBody MjConvertRequest convertRequest) {
        roomMidjourneyMsgService.variation(convertRequest);
        return R.data(true);
    }
    /**
     * 关联动作
     * @param convertRequest
     * @return
     */
    @CellConfigCheck(roomId = "#convertRequest.roomId")
    @Operation(summary = "variation")
    @PostMapping("/cpzaction")
    public R<Boolean> cpzClick(@Validated @RequestBody MjConvertRequest convertRequest) {
        if(MjMsgActionEnum.BLEND.getAction().equals(convertRequest.getAction()) || MjMsgActionEnum.FACE.getAction().equals(convertRequest.getAction()))
            roomMidjourneyMsgService.blendMj(convertRequest);
        else
            roomMidjourneyMsgService.cpzClick(convertRequest);
        return R.data(true);
    }
    /**
     * 关联动作
     * @param convertRequest
     * @return  modalImg
     */
    @CellConfigCheck(roomId = "#convertRequest.roomId")
    @Operation(summary = "variation")
    @PostMapping("/modalImg")
    public R<Boolean> modalImg(@Validated @RequestBody MjConvertRequest convertRequest) {
        roomMidjourneyMsgService.modalImg(convertRequest);
        return R.data(true);
    }
    /**
     * 图生文
     * @param describeRequest
     * @return
     */
    @CellConfigCheck(roomId = "#describeRequest.roomId")
    @Operation(summary = "describe")
    @PostMapping("/describe")
    public R<Boolean> describe(@Validated @ModelAttribute MjDescribeRequest describeRequest) {
        roomMidjourneyMsgService.describe(describeRequest);
        return R.data(true);
    }
    /**
     * 上传图片
     * @param file
     * @return
     */
    @Operation(summary = "upload")
    @PostMapping("/upload")
    public R<String> upload(@Validated @RequestParam("file") MultipartFile file,@Parameter(description = "文件类型 type") String type) {

        return R.data(roomMidjourneyMsgService.upload(file,type));
    }
}
