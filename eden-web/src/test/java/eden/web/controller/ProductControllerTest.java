package eden.web.controller;

import eden.common.result.ResultCode;
import eden.pojo.Product;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.ProductVO;
import eden.service.ProductService;
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
}
