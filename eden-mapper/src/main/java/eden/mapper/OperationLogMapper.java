package eden.mapper;

import eden.pojo.OperationLog;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 操作日志 Mapper 接口
 */
public interface OperationLogMapper {

    /**
     * 插入日志
     */
    int insert(OperationLog log);

    /**
     * 查询日志列表
     */
    List<OperationLog> selectList(@Param("userId") Long userId,
                                  @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计总数
     */
    long count(@Param("userId") Long userId);

    /**
     * 删除指定日期之前的日志
     */
    int deleteBeforeDate(@Param("date") String date);
}
