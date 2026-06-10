package eden.mapper;

import eden.pojo.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper。
 * <p>提供菜单树、权限码校验和角色授权所需的基础查询。</p>
 */
public interface PermissionMapper {
    List<Permission> selectAllEnabled();

    List<Permission> selectByUserId(@Param("userId") Long userId);

    List<String> selectCodesByUserId(@Param("userId") Long userId);

    List<Permission> selectByRoleId(@Param("roleId") Long roleId);
}
