package eden.mapper;

import eden.pojo.SupportSession;
import org.apache.ibatis.annotations.Param;

/**
 * 客服会话 Mapper，负责按用户维度查询和维护会话归属。
 */
public interface SupportSessionMapper {

    /**
     * 查询用户当前可复用的进行中会话，避免个人中心反复创建空会话。
     */
    SupportSession selectActiveByUserId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据 ID 查询会话，用于消息读写前的用户归属校验。
     */
    SupportSession selectById(@Param("id") Long id);

    /**
     * 创建客服会话。
     */
    int insert(SupportSession session);

    /**
     * 刷新会话更新时间，保证最近咨询可以排在前面。
     */
    int touch(@Param("id") Long id);
}
