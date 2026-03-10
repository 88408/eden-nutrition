package eden.web.annotation;

import java.lang.annotation.*;

/**
 * 标注需要登录才能访问的接口
 * 可以标注在类或方法上
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
