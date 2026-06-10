package eden.service.impl;

import eden.mapper.NoticeMapper;
import eden.pojo.Notice;
import eden.pojo.vo.PageVO;
import eden.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内通知服务实现，所有操作均按当前用户过滤，保证消息中心数据隔离。
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public PageVO<Notice> list(Long userId, int pageNum, int pageSize) {
        // 通知分页使用服务层换算 offset，Controller 只保留业务参数。
        int offset = (pageNum - 1) * pageSize;
        List<Notice> notices = noticeMapper.selectByUserId(userId, offset, pageSize);
        long total = noticeMapper.countByUserId(userId);
        return PageVO.of(notices, total, pageNum, pageSize);
    }

    @Override
    public long countUnread(Long userId) {
        return noticeMapper.countUnread(userId);
    }

    @Override
    public void markRead(Long userId, Long id) {
        // Mapper 条件包含 userId，即使传入他人通知ID也不会更新。
        noticeMapper.markRead(userId, id);
    }

    @Override
    public void markAllRead(Long userId) {
        noticeMapper.markAllRead(userId);
    }
}
