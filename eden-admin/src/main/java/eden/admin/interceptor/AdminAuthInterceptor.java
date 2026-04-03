package eden.admin.interceptor;

import eden.admin.annotation.RequireAdminLogin;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查是否有 RequireAdminLogin 注解
        boolean hasAnnotation = handlerMethod.getBeanType().isAnnotationPresent(RequireAdminLogin.class)
                || handlerMethod.getMethod().isAnnotationPresent(RequireAdminLogin.class);

        if (!hasAnnotation) {
            return true;
        }

        // 获取 token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            // 如果 Header 没有则尝试从参数中获取
            token = request.getParameter("token");
        }
        
        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        try {
            // 解析 token
            Claims claims = jwtUtils.parseToken(token);
            if (claims == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            Long userId = jwtUtils.getUserId(token);
            
            // 验证 token 是否在 redis 中存在，防止已退出登录的 token 继续使用
            String cacheToken = stringRedisTemplate.opsForValue().get(RedisConstants.USER_TOKEN + userId);
            if (!token.equals(cacheToken)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }

            // 将用户ID存入request
            request.setAttribute("adminId", userId);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }
}
