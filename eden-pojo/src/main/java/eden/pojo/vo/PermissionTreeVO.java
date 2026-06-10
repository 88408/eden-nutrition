package eden.pojo.vo;

import eden.pojo.Permission;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点。
 * <p>后台登录和权限配置共用该结构，前端可按 children 直接渲染菜单树。</p>
 */
@Data
public class PermissionTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String path;
    private String component;
    private String icon;
    private Integer type;
    private Integer sortOrder;
    private List<PermissionTreeVO> children = new ArrayList<>();

    public static PermissionTreeVO from(Permission permission) {
        PermissionTreeVO vo = new PermissionTreeVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }
}
