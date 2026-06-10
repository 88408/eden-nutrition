package eden.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户发送客服消息的请求参数，sessionId 用于校验会话归属，content 为本次留言内容。
 */
@Data
public class SupportMessageDTO {

    /** 客服会话 ID，只允许当前登录用户自己的会话。 */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /** 用户留言内容，空内容不入库，避免生成无效客服消息。 */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
