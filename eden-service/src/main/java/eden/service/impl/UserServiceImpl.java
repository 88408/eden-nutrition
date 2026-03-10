package eden.service.impl;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.JwtUtils;
import eden.mapper.UserMapper;
import eden.pojo.User;
import eden.pojo.dto.LoginDTO;
import eden.pojo.dto.RegisterDTO;
import eden.pojo.vo.LoginVO;
import eden.pojo.vo.UserVO;
import eden.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

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

        return loginVO;
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
        updateUser.setPhone(userVO.getPhone());
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
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.selectByPhone(phone) != null;
    }
}
