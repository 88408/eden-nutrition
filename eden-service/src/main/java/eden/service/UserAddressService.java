package eden.service;

import eden.pojo.UserAddress;
import java.util.List;

/**
 * 收货地址服务接口
 */
public interface UserAddressService {

    /**
     * 获取用户的所有收货地址
     */
    List<UserAddress> listByUserId(Long userId);

    /**
     * 获取用户的默认地址
     */
    UserAddress getDefaultAddress(Long userId);

    /**
     * 根据ID获取地址
     */
    UserAddress getById(Long id);

    /**
     * 添加收货地址
     */
    void add(Long userId, UserAddress address);

    /**
     * 更新收货地址
     */
    void update(Long userId, UserAddress address);

    /**
     * 删除收货地址
     */
    void delete(Long userId, Long addressId);

    /**
     * 设置默认地址
     */
    void setDefault(Long userId, Long addressId);
}
