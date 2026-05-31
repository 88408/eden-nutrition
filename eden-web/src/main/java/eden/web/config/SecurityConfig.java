package eden.web.config;

import eden.web.filter.JwtAuthenticationTokenFilter;
import eden.web.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            // 禁用session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 允许匿名访问的接口
            .antMatchers("/user/login", "/user/register").permitAll()
                .antMatchers("/admin/user/login", "/admin/user/register").permitAll()
            // 允许匿名访问订阅接口（前端订阅不需要登录）
            // 使用更通用的匹配，兼容有无 context-path 的情况
            .antMatchers("/subscribe", "/api/subscribe", "/**/subscribe").permitAll()
            // 支付宝异步通知和同步返回没有用户登录态，必须通过签名验签保护
            .antMatchers(
                    "/order/pay/alipay/notify",
                    "/order/pay/alipay/return",
                    "/order/pay/alipay/bridge",
                    "/order/pay/alipay/weapp-debug-return",
                    "/api/order/pay/alipay/notify",
                    "/api/order/pay/alipay/return",
                    "/api/order/pay/alipay/bridge",
                    "/api/order/pay/alipay/weapp-debug-return").permitAll()
            // Swagger / Knife4j 相关
            .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll()
            // 静态资源
            .antMatchers("/", "/*.html", "/**/*.css", "/**/*.js", "/favicon.ico").permitAll()
            // 其他所有请求需要认证
            .anyRequest().authenticated();
        
        // 添加JWT filter
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 支付宝调试桥接页需要被微信开发者工具 web-view 承载，只对这些 HTML 页面屏蔽 X-Frame-Options。
        http.addFilterBefore(alipayWebViewFrameHeaderFilter(), HeaderWriterFilter.class);
        
        return http.build();
    }

    /**
     * 仅支付宝沙箱调试 HTML 允许被 web-view 嵌入，其他接口继续保留 Spring Security 默认响应头。
     */
    @Bean
    public OncePerRequestFilter alipayWebViewFrameHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                if (isAlipayWebViewDebugPage(request)) {
                    filterChain.doFilter(request, new SuppressFrameOptionsResponse(response));
                    return;
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * context-path 为 /api 时 servletPath 不含 /api；保留 /api 判断用于兼容代理或测试环境直传完整路径。
     */
    private boolean isAlipayWebViewDebugPage(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String requestUri = request.getRequestURI();
        return isAlipayWebViewDebugPath(servletPath) || isAlipayWebViewDebugPath(requestUri);
    }

    private boolean isAlipayWebViewDebugPath(String path) {
        return "/order/pay/alipay/bridge".equals(path)
                || "/order/pay/alipay/weapp-debug-return".equals(path)
                || "/api/order/pay/alipay/bridge".equals(path)
                || "/api/order/pay/alipay/weapp-debug-return".equals(path);
    }

    /**
     * 拦截默认 HeaderWriter 写入的 X-Frame-Options，避免微信 web-view 直接白屏。
     */
    private static class SuppressFrameOptionsResponse extends HttpServletResponseWrapper {
        private static final String X_FRAME_OPTIONS = "X-Frame-Options";

        SuppressFrameOptionsResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            if (!X_FRAME_OPTIONS.equalsIgnoreCase(name)) {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if (!X_FRAME_OPTIONS.equalsIgnoreCase(name)) {
                super.addHeader(name, value);
            }
        }
    }
}
