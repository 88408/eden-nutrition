package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服会话实体，记录用户与客服之间的一条长期留言会话。
 */
@Data
public class SupportSession implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 会话ID。 */
    private Long id;

    /** 会话所属用户ID。 */
    private Long userId;

    /** 来源商品ID，商品详情页发起咨询时记录，个人中心发起时可为空。 */
    private Long productId;

    /** 会话状态：0-关闭，1-进行中。 */
    private Integer status;

    /** 会话创建时间。 */
    private LocalDateTime createTime;

    /** 会话更新时间，发送新消息时同步刷新。 */
    private LocalDateTime updateTime;
}
