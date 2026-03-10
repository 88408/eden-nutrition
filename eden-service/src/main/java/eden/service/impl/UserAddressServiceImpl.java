package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.UserAddressMapper;
import eden.pojo.UserAddress;
import eden.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务实现类
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    /** 最大收货地址数量 */
    private static final int MAX_ADDRESS_COUNT = 20;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> listByUserId(Long userId) {
        return userAddressMapper.selectByUserId(userId);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        return userAddressMapper.selectDefaultByUserId(userId);
    }

    @Override
    public UserAddress getById(Long id) {
        return userAddressMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, UserAddress address) {
        // 检查地址数量限制
        List<UserAddress> existingAddresses = userAddressMapper.selectByUserId(userId);
        if (existingAddresses.size() >= MAX_ADDRESS_COUNT) {
            throw new BusinessException(ResultCode.ADDRESS_LIMIT);
        }

        address.setUserId(userId);

        // 如果是第一个地址或设置为默认，处理默认地址逻辑
        if (existingAddresses.isEmpty()) {
            address.setIsDefault(1);
        } else if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            // 清除其他默认地址
            userAddressMapper.clearDefault(userId);
        } else {
            address.setIsDefault(0);
        }

        userAddressMapper.insert(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, UserAddress address) {
        // 验证地址归属
        UserAddress existing = userAddressMapper.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        // 如果设置为默认，清除其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.clearDefault(userId);
        }

        userAddressMapper.update(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long addressId) {
        // 验证地址归属
        UserAddress existing = userAddressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        userAddressMapper.deleteById(addressId);

        // 如果删除的是默认地址，将第一个地址设为默认
        if (existing.getIsDefault() == 1) {
            List<UserAddress> remaining = userAddressMapper.selectByUserId(userId);
            if (!remaining.isEmpty()) {
                userAddressMapper.setDefault(remaining.get(0).getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        // 验证地址归属
        UserAddress existing = userAddressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }

        // 清除其他默认地址
        userAddressMapper.clearDefault(userId);

        // 设置新的默认地址
        userAddressMapper.setDefault(addressId);
    }
}
