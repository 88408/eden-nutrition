package eden.service;

import eden.pojo.SupportMessage;
import eden.pojo.SupportSession;
import eden.pojo.dto.SupportMessageDTO;

import java.util.List;

/**
 * 客服会话服务，负责用户侧留言会话创建、消息发送与会话归属校验。
 */
public interface SupportService {

    /**
     * 获取或创建当前用户的客服会话，productId 为空时表示个人中心通用客服入口。
     */
    SupportSession getOrCreateSession(Long userId, Long productId);

    /**
     * 发送用户消息，写入前必须校验 sessionId 属于当前用户。
     */
    SupportMessage sendMessage(Long userId, SupportMessageDTO messageDTO);

    /**
     * 查询会话消息列表，读取前必须校验 sessionId 属于当前用户。
     */
    List<SupportMessage> listMessages(Long userId, Long sessionId);
}
