package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.SupportMessageMapper;
import eden.mapper.SupportSessionMapper;
import eden.pojo.SupportMessage;
import eden.pojo.SupportSession;
import eden.pojo.dto.SupportMessageDTO;
import eden.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客服会话服务实现，采用留言式会话模型，后续可在此基础上扩展在线客服或 WebSocket。
 */
@Service
public class SupportServiceImpl implements SupportService {

    @Autowired
    private SupportSessionMapper supportSessionMapper;

    @Autowired
    private SupportMessageMapper supportMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupportSession getOrCreateSession(Long userId, Long productId) {
        SupportSession session = supportSessionMapper.selectActiveByUserId(userId, productId);
        if (session != null) {
            return session;
        }

        session = new SupportSession();
        session.setUserId(userId);
        session.setProductId(productId);
        session.setStatus(1);
        supportSessionMapper.insert(session);

        // 新会话写入一条系统欢迎语，让前端聊天页首次进入时不为空。
        SupportMessage welcome = new SupportMessage();
        welcome.setSessionId(session.getId());
        welcome.setSenderType("SYSTEM");
        welcome.setContent("您好，客服已收到您的咨询，请留言说明问题，我们会尽快回复。");
        welcome.setIsRead(1);
        supportMessageMapper.insert(welcome);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupportMessage sendMessage(Long userId, SupportMessageDTO messageDTO) {
        SupportSession session = requireOwnSession(userId, messageDTO.getSessionId());
        SupportMessage message = new SupportMessage();
        message.setSessionId(session.getId());
        message.setSenderType("USER");
        message.setContent(messageDTO.getContent().trim());
        message.setIsRead(0);
        supportMessageMapper.insert(message);
        supportSessionMapper.touch(session.getId());
        return message;
    }

    @Override
    public List<SupportMessage> listMessages(Long userId, Long sessionId) {
        SupportSession session = requireOwnSession(userId, sessionId);
        return supportMessageMapper.selectBySessionId(session.getId());
    }

    /**
     * 客服消息读写前统一校验会话归属，避免用户构造 sessionId 读取他人咨询内容。
     */
    private SupportSession requireOwnSession(Long userId, Long sessionId) {
        SupportSession session = supportSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客服会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return session;
    }
}
