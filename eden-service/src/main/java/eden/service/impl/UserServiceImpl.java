package eden.service.impl;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.JwtUtils;
import eden.mapper.UserMapper;
import eden.pojo.User;
import eden.pojo.dto.LoginDTO;
import eden.pojo.dto.PasswordResetDTO;
import eden.pojo.dto.RegisterDTO;
import eden.pojo.vo.LoginVO;
import eden.pojo.vo.UserVO;
import eden.service.RbacService;
import eden.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final SecureRandom PASSWORD_RESET_RANDOM = new SecureRandom();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RbacService rbacService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        if (existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        // 检查手机号是否存在
        if (registerDTO.getPhone() != null && existsByPhone(registerDTO.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setPoints(0);
        user.setStatus(1);
        user.setRole("USER"); // 普通用户

        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        return processLogin(loginDTO, "USER");
    }

    @Override
    public LoginVO adminLogin(LoginDTO loginDTO) {
        return processLogin(loginDTO, "ADMIN");
    }

    private LoginVO processLogin(LoginDTO loginDTO, String expectedRole) {
        // 检查登录失败次数
        String failKey = RedisConstants.USER_LOGIN_FAIL + loginDTO.getUsername();
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey); 
        if (failCount != null && failCount >= 5) {
            throw new BusinessException("登录失败次数过多，请30分钟后再试");    
        }

        // 查询用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());        
        if (user == null) {
            incrementLoginFail(failKey);
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR); 
        }

        // 验证角色
        if (expectedRole != null && !expectedRole.equals(user.getRole())) {
            incrementLoginFail(failKey);
            if ("ADMIN".equals(expectedRole)) {
                throw new BusinessException("后台管理系统仅允许管理员登录");
            } else {
                throw new BusinessException("普通用户入口，禁止管理员登录");
            }        }
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            incrementLoginFail(failKey);
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 清除登录失败记录
        redisTemplate.delete(failKey);

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 缓存用户Token（使用StringRedisTemplate避免序列化问题）
        String tokenKey = RedisConstants.USER_TOKEN + user.getId();
        stringRedisTemplate.opsForValue().set(tokenKey, token, RedisConstants.EXPIRE_TOKEN, TimeUnit.SECONDS);

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setRole(user.getRole());
        if ("ADMIN".equals(user.getRole())) {
            enrichAdminAuth(loginVO, user.getId());
        }

        return loginVO;
    }

    /**
     * 为后台登录结果补充角色、权限码和菜单树，供管理端做菜单与按钮级 RBAC 控制。
     * 若管理员未分配任何权限，直接拒绝登录并给出明确提示，避免登录后所有操作返回 403。
     */
    private void enrichAdminAuth(LoginVO loginVO, Long userId) {
        loginVO.setRoles(rbacService.getUserRoles(userId).stream()
                .map(role -> role.getCode())
                .collect(Collectors.toList()));
        loginVO.setPermissions(rbacService.getPermissionCodes(userId));
        loginVO.setMenus(rbacService.getMenuTreeByUserId(userId));

        // 检查是否有权限配置，若无则拒绝登录
        if (loginVO.getPermissions() == null || loginVO.getPermissions().isEmpty()) {
            throw new BusinessException("管理员账号未配置任何权限，请联系超级管理员");
        }
    }

    private void incrementLoginFail(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, RedisConstants.EXPIRE_LOGIN_FAIL, TimeUnit.SECONDS);
        }
    }

    @Override
    public void logout(Long userId) {
        // 删除Token缓存（使用StringRedisTemplate）
        String tokenKey = RedisConstants.USER_TOKEN + userId;
        stringRedisTemplate.delete(tokenKey);

        // 删除用户信息缓存
        String userInfoKey = RedisConstants.USER_INFO + userId;
        redisTemplate.delete(userInfoKey);
    }

    @Override
    public UserVO getUserById(Long userId) {
        // 先从缓存获取
        String cacheKey = RedisConstants.USER_INFO + userId;
        UserVO cachedUser = (UserVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // 从数据库获取
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 转换为VO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        if ("ADMIN".equals(user.getRole())) {
            userVO.setRoles(rbacService.getUserRoles(userId).stream()
                    .map(role -> role.getCode())
                    .collect(Collectors.toList()));
            userVO.setPermissions(rbacService.getPermissionCodes(userId));
            userVO.setMenus(rbacService.getMenuTreeByUserId(userId));
        }

        // 缓存用户信息
        redisTemplate.opsForValue().set(cacheKey, userVO, RedisConstants.EXPIRE_USER_INFO, TimeUnit.SECONDS);

        return userVO;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserVO userVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname(userVO.getNickname());
        // 手机号用于登录找回密码等安全链路，普通资料更新不允许直接覆盖，避免绕过验证码绑定他人手机号。
        updateUser.setEmail(userVO.getEmail());
        updateUser.setAvatar(userVO.getAvatar());
        updateUser.setGender(userVO.getGender());

        userMapper.update(updateUser);

        // 清除用户信息缓存
        redisTemplate.delete(RedisConstants.USER_INFO + userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(updateUser);

        // 清除Token，强制重新登录
        logout(userId);
    }

    @Override
    public void sendPasswordResetCode(String phone) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "手机号未注册");
        }

        // 验证码先写入 Redis 并打印日志模拟短信发送，后续接入短信供应商时只需替换发送动作。
        String code = String.format("%06d", PASSWORD_RESET_RANDOM.nextInt(1_000_000));
        String key = RedisConstants.VERIFY_CODE + "password:reset:" + phone;
        stringRedisTemplate.opsForValue().set(key, code, RedisConstants.EXPIRE_VERIFY_CODE, TimeUnit.SECONDS);
        log.info("用户找回密码验证码 phone={}, code={}, expireSeconds={}", phone, code, RedisConstants.EXPIRE_VERIFY_CODE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(PasswordResetDTO resetDTO) {
        String key = RedisConstants.VERIFY_CODE + "password:reset:" + resetDTO.getPhone();
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (cachedCode == null || !cachedCode.equals(resetDTO.getCode())) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }

        User user = userMapper.selectByPhone(resetDTO.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "手机号未注册");
        }

        // 找回密码不要求旧密码，但会更新密码并清除验证码，确保验证码只能使用一次。
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        userMapper.update(updateUser);
        stringRedisTemplate.delete(key);
        logout(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePoints(Long userId, Integer points) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        int newPoints = user.getPoints() + points;
        if (newPoints < 0) {
            newPoints = 0;
        }

        userMapper.updatePoints(userId, newPoints);

        // 清除用户信息缓存
        redisTemplate.delete(RedisConstants.USER_INFO + userId);

        // 同步用户最新积分到独立的Redis Key
        String pointsKey = "eden:user:points:" + userId;
        stringRedisTemplate.opsForValue().set(pointsKey, String.valueOf(newPoints));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.selectByPhone(phone) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sign(Long userId) {
        LocalDate now = LocalDate.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        // Key设计：使用特定的前缀拼接用户ID和年月，例如：eden:user:sign:1:202605
        String key = "eden:user:sign:" + userId + ":" + keySuffix;

        // offset从0开始，所以当月号需要减1
        int offset = now.getDayOfMonth() - 1;

        // setBit 返回的是执行操作前该位的值。如果是true，说明今天已经签过到了
        Boolean isSigned = stringRedisTemplate.opsForValue().setBit(key, offset, true);
        if (Boolean.TRUE.equals(isSigned)) {
            throw new BusinessException("您今日已经签到过了~");
        }

        // 签到成功送积分奖励 (示例：送10积分)
        this.updatePoints(userId, 10);

        return true;
    }

    @Override
    public boolean checkSign(Long userId) {
        LocalDate now = LocalDate.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = "eden:user:sign:" + userId + ":" + keySuffix;
        int offset = now.getDayOfMonth() - 1;

        // 查询今天对应位是否为 1
        Boolean isSigned = stringRedisTemplate.opsForValue().getBit(key, offset);
        return Boolean.TRUE.equals(isSigned);
    }
}
