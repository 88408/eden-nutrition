package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服消息实体，senderType 区分用户、客服和系统欢迎语。
 */
@Data
public class SupportMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 消息ID。 */
    private Long id;

    /** 所属客服会话ID。 */
    private Long sessionId;

    /** 发送人类型：USER-用户，STAFF-客服，SYSTEM-系统。 */
    private String senderType;

    /** 消息正文。 */
    private String content;

    /** 是否已读：0-未读，1-已读。 */
    private Integer isRead;

    /** 消息创建时间。 */
    private LocalDateTime createTime;
}
