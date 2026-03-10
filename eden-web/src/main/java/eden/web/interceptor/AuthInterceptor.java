package eden.web.interceptor;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.JwtUtils;
import eden.web.annotation.RequireLogin;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /** ThreadLocal 存储当前登录用户ID */
    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 不是方法处理器，放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查是否需要登录
        boolean requireLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class) ||
                handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);

        // 获取Token
        String token = extractToken(request);

        if (token != null && !token.isEmpty()) {
            try {
                // 验证Token
                if (jwtUtils.validateToken(token)) {
                    Long userId = jwtUtils.getUserId(token);
                    
                    // 验证Redis中的Token是否一致（单点登录）
                    String cachedToken = stringRedisTemplate.opsForValue()
                            .get(RedisConstants.USER_TOKEN + userId);
                    
                    if (token.equals(cachedToken)) {
                        // Token有效，存储用户ID
                        CURRENT_USER.set(userId);
                    } else if (requireLogin) {
                        // Token已失效（被踢出或已在其他地方登录）
                        throw new BusinessException(ResultCode.UNAUTHORIZED, "登录已失效，请重新登录");
                    }
                }
            } catch (JwtException e) {
                if (requireLogin) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED);
                }
            }
        }

        // 需要登录但未登录
        if (requireLogin && CURRENT_USER.get() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 清除ThreadLocal，防止内存泄漏
        CURRENT_USER.remove();
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtUtils.TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(JwtUtils.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtUtils.TOKEN_PREFIX.length());
        }
        // 也支持从参数中获取
        return request.getParameter("token");
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return CURRENT_USER.get();
    }
}
