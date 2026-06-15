package eden.web.config;

import eden.web.filter.JwtAuthenticationTokenFilter;
import eden.web.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;
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
                // 商城端使用 JWT 鉴权，不依赖服务端 Session。
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/user/login", "/user/register").permitAll()
                .antMatchers("/admin/user/login", "/admin/user/register").permitAll()
                // 订阅入口是公开表单，兼容有无 /api context-path 的访问路径。
                .antMatchers("/subscribe", "/api/subscribe", "/**/subscribe").permitAll()
                // 支付宝回调和调试桥接页没有用户登录态，必须通过支付宝签名或短期 token 保护。
                .antMatchers(
                        "/order/pay/alipay/notify",
                        "/order/pay/alipay/return",
                        "/order/pay/alipay/bridge",
                        "/order/pay/alipay/weapp-debug-return",
                        "/api/order/pay/alipay/notify",
                        "/api/order/pay/alipay/return",
                        "/api/order/pay/alipay/bridge",
                        "/api/order/pay/alipay/weapp-debug-return").permitAll()
                // Swagger / Knife4j 文档入口。
                .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs").permitAll()
                // 用户端公开浏览能力只按 GET 精确放行，避免收藏、购物车、下单等写接口被匿名访问。
                .antMatchers(HttpMethod.GET,
                        "/product/list",
                        "/api/product/list",
                        "/product/*",
                        "/api/product/*",
                        "/product/hot",
                        "/api/product/hot",
                        "/product/recommend",
                        "/api/product/recommend",
                        "/product/new",
                        "/api/product/new",
                        "/product/category/*",
                        "/api/product/category/*",
                        "/category/tree",
                        "/api/category/tree",
                        "/category/first",
                        "/api/category/first",
                        "/category/children/*",
                        "/api/category/children/*",
                        "/category/*",
                        "/api/category/*",
                        "/seckill/sessions",
                        "/api/seckill/sessions",
                        "/seckill/list",
                        "/api/seckill/list",
                        "/seckill/ongoing",
                        "/api/seckill/ongoing",
                        "/seckill/upcoming",
                        "/api/seckill/upcoming").permitAll()
                // 商品图片由后端静态目录统一托管，用户端和管理端都需要未登录直接渲染。
                .antMatchers("/images/**", "/api/images/**", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.webp").permitAll()
                // 前端静态资源。
                .antMatchers("/", "/*.html", "/**/*.css", "/**/*.js", "/favicon.ico").permitAll()
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 仅支付宝调试桥接页允许被微信开发者工具 web-view 承载，其他接口保留默认安全响应头。
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
     * context-path 为 /api 时 servletPath 不含 /api；同时保留 /api 判断用于兼容代理或测试环境直传完整路径。
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
     * 拦截默认 HeaderWriter 写入的 X-Frame-Options，避免微信 web-view 承载调试桥接页时白屏。
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
