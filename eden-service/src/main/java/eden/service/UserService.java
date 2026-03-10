package eden.service;

import eden.pojo.User;
import eden.pojo.dto.LoginDTO;
import eden.pojo.dto.RegisterDTO;
import eden.pojo.vo.LoginVO;
import eden.pojo.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出
     */
    void logout(Long userId);

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    User getByUsername(String username);

    /**
     * 更新用户信息
     */
    void updateUser(Long userId, UserVO userVO);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新用户积分
     */
    void updatePoints(Long userId, Integer points);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);
}
