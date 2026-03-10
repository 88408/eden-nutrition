package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 */
@Data
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 优惠券ID */
    private Long id;

    /** 优惠券名称 */
    private String name;

    /** 类型：1-满减券 2-折扣券 */
    private Integer type;

    /** 优惠值（满减金额或折扣比例） */
    private BigDecimal discountValue;

    /** 面值/折扣值（兼容字段） */
    private BigDecimal value;

    /** 最低消费金额 */
    private BigDecimal minAmount;

    /** 最大优惠金额（折扣券用） */
    private BigDecimal maxDiscount;

    /** 发放总量 */
    private Integer totalCount;

    /** 剩余数量 */
    private Integer remainCount;

    /** 生效开始时间 */
    private LocalDateTime startTime;

    /** 生效结束时间 */
    private LocalDateTime endTime;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ========== 优惠券类型常量 ==========
    public static final int TYPE_FULL_REDUCTION = 1;  // 满减券
    public static final int TYPE_DISCOUNT = 2;        // 折扣券
}
