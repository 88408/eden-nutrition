package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.mapper.PermissionMapper;
import eden.mapper.RoleMapper;
import eden.pojo.Permission;
import eden.pojo.Role;
import eden.pojo.dto.RoleSaveDTO;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.PermissionTreeVO;
import eden.service.RbacService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台 RBAC 服务实现。
 * <p>权限树构建保留数据库排序，确保菜单展示和权限配置页面顺序稳定。</p>
 */
@Service
public class RbacServiceImpl implements RbacService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        return buildTree(permissionMapper.selectAllEnabled());
    }

    @Override
    public List<PermissionTreeVO> getMenuTreeByUserId(Long userId) {
        List<Permission> permissions = permissionMapper.selectByUserId(userId).stream()
                .filter(permission -> permission.getType() != null && permission.getType() == 1)
                .collect(Collectors.toList());
        return buildTree(permissions);
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        List<String> codes = permissionMapper.selectCodesByUserId(userId);
        return codes == null ? Collections.emptyList() : codes;
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return roles == null ? Collections.emptyList() : roles;
    }

    @Override
    public PageVO<Role> getRolePage(String keyword, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNum - 1) * safePageSize;
        List<Role> roles = roleMapper.selectPage(keyword, offset, safePageSize);
        long total = roleMapper.countPage(keyword);
        return PageVO.of(roles, total, safePageNum, safePageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(RoleSaveDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank() || dto.getCode() == null || dto.getCode().isBlank()) {
            throw new BusinessException("角色名称和编码不能为空");
        }
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getId() == null) {
            roleMapper.insert(role);
        } else {
            roleMapper.update(role);
        }
        if (dto.getPermissionIds() != null) {
            assignRolePermissions(role.getId(), dto.getPermissionIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        roleMapper.deleteRolePermissions(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            roleMapper.batchInsertRolePermissions(roleId, permissionIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        roleMapper.deleteUserRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            roleMapper.batchInsertUserRoles(userId, roleIds);
        }
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        return permissions == null ? Collections.emptyList() : permissions;
    }

    /**
     * 将扁平权限列表组装成树；找不到父节点时作为根节点返回，避免脏数据导致菜单丢失。
     */
    private List<PermissionTreeVO> buildTree(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, PermissionTreeVO> nodeMap = new LinkedHashMap<>();
        for (Permission permission : permissions) {
            nodeMap.put(permission.getId(), PermissionTreeVO.from(permission));
        }
        List<PermissionTreeVO> roots = new ArrayList<>();
        for (PermissionTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }
}
