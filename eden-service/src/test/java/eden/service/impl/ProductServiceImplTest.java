package eden.service.impl;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.ProductMapper;
import eden.pojo.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        // Lenient mock for opsForValue to avoid unnecessary stubbing exceptions in tests that don't use it
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getById_CacheHit_ReturnsCachedProduct() {
        // Arrange
        Long productId = 1L;
        Product cachedProduct = new Product();
        cachedProduct.setId(productId);
        cachedProduct.setName("Cached Product");

        when(valueOperations.get(RedisConstants.PRODUCT_DETAIL + productId)).thenReturn(cachedProduct);

        // Act
        Product result = productService.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Cached Product", result.getName());
        
        // Verify DB was NOT called
        verify(productMapper, never()).selectById(anyLong());
    }

    @Test
    void getById_CacheMiss_DbHit_StatusNormal_ReturnsProductAndCaches() {
        // Arrange
        Long productId = 2L;
        Product dbProduct = new Product();
        dbProduct.setId(productId);
        dbProduct.setName("DB Product");
        dbProduct.setStatus(1); // Normal status

        when(valueOperations.get(RedisConstants.PRODUCT_DETAIL + productId)).thenReturn(null);
        when(productMapper.selectById(productId)).thenReturn(dbProduct);

        // Act
        Product result = productService.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals("DB Product", result.getName());

        // Verify DB was called
        verify(productMapper, times(1)).selectById(productId);
        
        // Verify result was cached
        verify(valueOperations, times(1)).set(
                eq(RedisConstants.PRODUCT_DETAIL + productId), 
                eq(dbProduct), 
                anyLong(), 
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void getById_CacheMiss_DbHit_StatusAbnormal_ThrowsException() {
        // Arrange
        Long productId = 3L;
        Product dbProduct = new Product();
        dbProduct.setId(productId);
        dbProduct.setStatus(0); // Off shelf

        when(valueOperations.get(RedisConstants.PRODUCT_DETAIL + productId)).thenReturn(null);
        when(productMapper.selectById(productId)).thenReturn(dbProduct);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getById(productId);
        });

        assertEquals(ResultCode.PRODUCT_OFF_SHELF.getMessage(), exception.getMessage());
        
        // Verify NOTHING was cached
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void getById_CacheMiss_DbMiss_ThrowsException() {
        // Arrange
        Long productId = 4L;
        when(valueOperations.get(RedisConstants.PRODUCT_DETAIL + productId)).thenReturn(null);
        when(productMapper.selectById(productId)).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.getById(productId);
        });

        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getMessage(), exception.getMessage());
    }
}
