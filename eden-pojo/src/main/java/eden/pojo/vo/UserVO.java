package eden.pojo.vo;

import lombok.Data;

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
}
