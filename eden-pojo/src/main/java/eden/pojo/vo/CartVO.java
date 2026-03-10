package eden.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车 VO
 */
@Data
public class CartVO {

    /** 购物车项列表 */
    private List<CartItemVO> items;

    /** 选中商品数量 */
    private Integer selectedCount;

    /** 选中商品总价 */
    private BigDecimal selectedAmount;

    /** 购物车商品总数 */
    private Integer totalCount;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 总数量 */
    private Integer totalQuantity;

    /** 是否全选 */
    private Boolean allSelected;
}
