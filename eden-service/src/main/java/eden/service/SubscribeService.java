package eden.service;

/**
 * 订阅服务接口
 */
public interface SubscribeService {

    /**
     * 订阅指定邮箱
     * @param email 邮箱字符串
     */
    void subscribe(String email);
}

