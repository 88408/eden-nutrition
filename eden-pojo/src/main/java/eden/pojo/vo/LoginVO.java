package eden.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录响应 VO
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    /** Token 类型 */
    private String tokenType = "Bearer";

    /** 过期时间（秒） */
    private Long expiresIn;

    /** 用户信息 */
    private UserVO user;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 角色 */
    private String role;

    /** 后台角色编码列表，用于前端展示当前管理员身份 */
    private List<String> roles = new ArrayList<>();

    /** 后台权限码列表，用于菜单和按钮级 RBAC 控制 */
    private List<String> permissions = new ArrayList<>();

    /** 后台菜单树，仅管理员登录时返回 */
    private List<PermissionTreeVO> menus = new ArrayList<>();
}
