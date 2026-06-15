package eden.web.controller;

import eden.common.result.ResultCode;
import eden.pojo.Product;
import eden.pojo.dto.ProductQueryDTO;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.ProductVO;
import eden.service.ProductService;
import eden.service.ProductSkuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import eden.common.utils.JwtUtils;
import eden.web.filter.JwtAuthenticationTokenFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class, 
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@WithMockUser
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductSkuService productSkuService;

    @SpyBean
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void getById_ExistingId_ReturnsProduct() throws Exception {
        // Arrange
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setName("Mock Product");
        mockProduct.setPrice(new BigDecimal("100.00"));
        
        given(productService.getById(productId)).willReturn(mockProduct);
        given(productSkuService.listByProductId(productId)).willReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/product/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("Mock Product"));
    }

    @Test
    void getHotProducts_ReturnsList() throws Exception {
        // Arrange
        Product hotProduct = new Product();
        hotProduct.setId(2L);
        hotProduct.setName("Hot Product");
        List<Product> hotList = Collections.singletonList(hotProduct);

        given(productService.getHotProducts(anyInt())).willReturn(hotList);

        // Act & Assert
        mockMvc.perform(get("/product/hot")
                .param("limit", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Hot Product"));
    }

    @Test
    void list_WithKeyword_ReturnsPageList() throws Exception {
        // 搜索页依赖 /product/list 返回 PageVO.list，这里锁定字段结构，避免前后端再次错位。
        Product product = new Product();
        product.setId(3L);
        product.setName("燕窝营养品");
        product.setPrice(new BigDecimal("199.00"));
        PageVO<Product> page = PageVO.of(Collections.singletonList(product), 1L, 1, 10);

        given(productService.queryProducts(any(ProductQueryDTO.class))).willReturn(page);

        mockMvc.perform(get("/product/list")
                .param("keyword", "燕窝")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].name").value("燕窝营养品"));
    }
}
