package eden.mapper;

import eden.pojo.SupportMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客服消息 Mapper，负责会话消息的顺序读取和写入。
 */
public interface SupportMessageMapper {

    /**
     * 按会话查询消息，升序返回便于前端按聊天流展示。
     */
    List<SupportMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 写入客服消息。
     */
    int insert(SupportMessage message);
}
