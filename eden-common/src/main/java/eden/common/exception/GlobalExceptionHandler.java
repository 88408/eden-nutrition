package eden.common.exception;

import eden.common.result.Result;
import eden.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        logger.warn("参数校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常（@ModelAttribute）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        logger.warn("参数绑定失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理约束违反异常（@PathVariable, @RequestParam）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        logger.warn("约束校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.warn("缺少请求参数: {}", e.getParameterName());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理请求参数类型不匹配异常（@RequestParam / @PathVariable）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                               HttpServletRequest request) {
        logger.warn("参数类型不匹配: uri={}, query={}, name={}, value={}, requiredType={}",
                request.getRequestURI(),
                request.getQueryString(),
                e.getName(),
                e.getValue(),
                e.getRequiredType() == null ? "unknown" : e.getRequiredType().getSimpleName());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), "参数格式错误: " + e.getName());
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("请求方法不支持: {}", e.getMethod());
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("资源不存在: {}", e.getRequestURL());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        logger.error("系统异常: ", e);
        return Result.fail(ResultCode.INTERNAL_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
