package eden.service.impl;

import eden.service.SubscribeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 订阅服务实现
 */
@Service
public class SubscribeServiceImpl implements SubscribeService {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeServiceImpl.class);

    @Override
    public void subscribe(String email) {
        if (email == null) {
            logger.warn("尝试订阅时邮箱为空");
            return;
        }
        // 打印到控制台/日志
        String msg = String.format("(%s)已订阅！", email);
        // 同时使用 System.out 以确保控制台可见
        System.out.println(msg);
        logger.info(msg);
    }
}

