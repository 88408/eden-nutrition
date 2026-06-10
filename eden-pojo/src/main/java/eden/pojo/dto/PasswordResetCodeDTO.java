package eden.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 找回密码验证码请求参数，当前仅支持手机号找回，后续接入短信服务时保持接口入参不变。
 */
@Data
public class PasswordResetCodeDTO {

    /** 接收验证码的注册手机号。 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
