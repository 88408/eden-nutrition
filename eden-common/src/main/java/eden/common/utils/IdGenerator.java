package eden.common.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 生成工具类
 */
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static final int MAX_SEQUENCE = 9999;

    /**
     * 生成订单号
     * 格式：yyyyMMddHHmmss + 4位随机数 + 4位序列号
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = String.format("%04d", RANDOM.nextInt(10000));
        int seq = SEQUENCE.incrementAndGet();
        if (seq > MAX_SEQUENCE) {
            SEQUENCE.set(0);
            seq = SEQUENCE.incrementAndGet();
        }
        String sequence = String.format("%04d", seq);
        return timestamp + random + sequence;
    }

    /**
     * 生成UUID（去掉横线）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成短UUID（16位）
     */
    public static String generateShortUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 生成数字验证码
     */
    public static String generateVerifyCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成字母数字混合验证码
     */
    public static String generateMixedCode(int length) {
        String chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成Token（用于重置密码等场景）
     */
    public static String generateToken() {
        return generateUUID() + System.currentTimeMillis();
    }

    /**
     * 生成业务流水号
     * 格式：前缀 + yyyyMMddHHmmss + 6位随机数
     */
    public static String generateBusinessNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = String.format("%06d", RANDOM.nextInt(1000000));
        return prefix + timestamp + random;
    }

    /**
     * 生成唯一ID（雪花算法简化版）
     * 用于生成数据库主键ID
     */
    public static long nextId() {
        // 使用时间戳 + 随机数 + 序列号生成唯一ID
        long timestamp = System.currentTimeMillis();
        int random = RANDOM.nextInt(1000);
        int seq = SEQUENCE.incrementAndGet() % 1000;
        return timestamp * 1000000 + random * 1000 + seq;
    }
}
