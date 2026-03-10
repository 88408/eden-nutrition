package eden.web.annotation;

import java.lang.annotation.*;

/**
 * 标注在Controller方法参数上，自动注入当前登录用户的ID
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
