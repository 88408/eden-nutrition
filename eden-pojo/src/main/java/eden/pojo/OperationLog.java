package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
public class OperationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作描述 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求参数 */
    private String params;

    /** IP地址 */
    private String ip;

    /** 执行时长（毫秒） */
    private Long duration;

    /** 创建时间 */
    private LocalDateTime createTime;
}
