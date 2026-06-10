package eden.service;

import eden.pojo.Notice;
import eden.pojo.vo.PageVO;

/**
 * 站内通知服务，提供消息中心列表、未读数和已读状态维护。
 */
public interface NoticeService {

    /**
     * 分页查询当前用户通知。
     */
    PageVO<Notice> list(Long userId, int pageNum, int pageSize);

    /**
     * 查询当前用户未读通知数。
     */
    long countUnread(Long userId);

    /**
     * 标记单条通知为已读，限定当前用户自己的通知。
     */
    void markRead(Long userId, Long id);

    /**
     * 标记当前用户全部通知为已读。
     */
    void markAllRead(Long userId);
}
