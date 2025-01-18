package com.hncboy.beehive.web.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author ll
 * @date 2023-7-25
 */
@Schema(title = "聊天参数设置")
@Data
public class HaUserParamRequest {
    @NotNull(groups = {Update.class}, message = "id 不能为空")
    @Schema(title = "id，修改用")
    private Integer id;

    @Pattern(regexp = "^(sk-)?.*", groups = {Save.class, Update.class}, message = "自己的私有apikey必须以\"sk-\"开头或者为空")
    @Schema(title = "自己的私有key")
    private String apikey;

    @NotEmpty(groups = {Update.class}, message = "temperature 不能为空")
    @Size(min = 0, max = 2, groups = {Save.class, Update.class}, message = "话题精准度：数值范围[0，2]")
    @Schema(title = "参数键")
    private Float temperature;

    @NotNull(groups = {Save.class, Update.class}, message = "contextCount 不能为空")
    @Size(min = 0, max = 30, groups = {Save.class, Update.class}, message = "上下文数量：数值范围[0，30]")
    @Schema(title = "上下文数量")
    private Integer contextCount;

    @Size(min = 0, max = 2000, groups = {Save.class, Update.class}, message = "上下文数量：数值范围[0，2000]")
    @Schema(title = "上下文数量")
    private String systemMessage;

    @NotNull(groups = {Save.class, Update.class}, message = "presencePenalty 不能为空")
    @Size(min = -2, max = 2, groups = {Save.class, Update.class}, message = "话题新鲜度：数值范围[-2，2]")
    @Schema(title = "上下文数量")
    private Float presencePenalty;

    @Schema(title = "模型版本")
    private String chatModel;


    /**
     * 是否使用 上下文
     * 0使用
     * 1不使用
     */
    private Integer isContext;
    /**
     * 新增
     */
    public interface Update {

    }

    /**
     * 更新
     */
    public interface Save {

    }
}
