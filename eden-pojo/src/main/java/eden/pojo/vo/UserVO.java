package eden.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息 VO（不含敏感信息）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer points;
    private String role;

    /** 后台角色编码列表，普通用户为空 */
    private List<String> roles = new ArrayList<>();

    /** 后台权限码列表，普通用户为空 */
    private List<String> permissions = new ArrayList<>();

    /** 后台菜单树，普通用户为空 */
    private List<PermissionTreeVO> menus = new ArrayList<>();
}
