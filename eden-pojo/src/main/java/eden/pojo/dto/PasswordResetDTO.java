package eden.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 找回密码提交参数，使用手机号与一次性验证码校验用户身份后重置密码。
 */
@Data
public class PasswordResetDTO {

    /** 注册手机号，必须与验证码发送时的手机号一致。 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 服务端缓存的 6 位验证码。 */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 新密码，沿用注册密码长度规则。 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String newPassword;
}
