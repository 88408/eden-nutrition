package eden.mapper;

import eden.pojo.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户 Mapper 接口
 */
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int update(User user);

    /**
     * 更新用户积分
     */
    int updatePoints(@Param("id") Long id, @Param("points") Integer points);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询用户列表（分页）
     */
    List<User> selectList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户总数
     */
    long count();

    /**
     * 根据时间范围统计用户总数
     */
    long countByDate(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
