package eden.mapper;

import eden.pojo.UserAddress;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 收货地址 Mapper 接口
 */
public interface UserAddressMapper {

    /**
     * 根据ID查询地址
     */
    UserAddress selectById(@Param("id") Long id);

    /**
     * 查询用户的所有收货地址
     */
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     */
    UserAddress selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 插入地址
     */
    int insert(UserAddress address);

    /**
     * 更新地址
     */
    int update(UserAddress address);

    /**
     * 删除地址
     */
    int deleteById(@Param("id") Long id);

    /**
     * 设置默认地址（先清除其他默认）
     */
    int clearDefault(@Param("userId") Long userId);

    /**
     * 设置为默认地址
     */
    int setDefault(@Param("id") Long id);
}
