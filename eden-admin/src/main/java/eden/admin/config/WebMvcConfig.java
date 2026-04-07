package eden.admin.config;

import eden.admin.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("adminWebMvcConfig")
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**") // 拦截所有管理后台接口
                .excludePathPatterns(
                        "/admin/user/login", // 排除登录接口
                        "/admin/dashboard/stats", // 暂时放行仪表盘概况
                        "/admin/dashboard/sales"  // 暂时放行销售额接口
                );
    }
}
