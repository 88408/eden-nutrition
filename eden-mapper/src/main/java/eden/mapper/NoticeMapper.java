package eden.mapper;

import eden.pojo.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内通知 Mapper，所有查询都带 userId，避免用户读取到他人消息。
 */
public interface NoticeMapper {

    /**
     * 分页查询当前用户通知。
     */
    List<Notice> selectByUserId(@Param("userId") Long userId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    /**
     * 统计当前用户通知总数。
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计当前用户未读通知数。
     */
    long countUnread(@Param("userId") Long userId);

    /**
     * 将单条通知标记为已读，仅允许更新当前用户自己的通知。
     */
    int markRead(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * 将当前用户全部未读通知标记为已读。
     */
    int markAllRead(@Param("userId") Long userId);

    /**
     * 写入通知，供订单、优惠券等业务后续复用。
     */
    int insert(Notice notice);
}
