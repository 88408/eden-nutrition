package eden.mapper;

import eden.pojo.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper。
 * <p>维护后台角色、角色权限关系和用户角色关系。</p>
 */
public interface RoleMapper {
    List<Role> selectPage(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("limit") Integer limit);

    long countPage(@Param("keyword") String keyword);

    Role selectById(@Param("id") Long id);

    List<Role> selectByUserId(@Param("userId") Long userId);

    int insert(Role role);

    int update(Role role);

    int deleteRolePermissions(@Param("roleId") Long roleId);

    int batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    int deleteUserRoles(@Param("userId") Long userId);

    int batchInsertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
