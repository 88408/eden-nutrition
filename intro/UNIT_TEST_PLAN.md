# Eden Nutrition 单元测试方案

## 1. 引言 (Introduction)
### 1.1 目的
本方案旨在规范 Eden Nutrition 项目的单元测试流程，确保核心业务逻辑的正确性，提高代码质量，减少回归 Bug，并为后续的 CI/CD 流程打下基础。

### 1.2 测试范围
*   **高优先级**：
    *   **Service 层**：核心业务逻辑集中地，必须进行覆盖全面的测试（正常路径、异常路径、边界条件）。
    *   **Common/Utils 层**：公共工具类，被广泛引用，必须保证绝对正确。
*   **中优先级**：
    *   **Controller 层**：验证 API 接口参数校验、响应格式及 HTTP 状态码。
*   **低优先级**：
    *   **Mapper/DAO 层**：通常由 MyBatis 框架保证，除非有复杂的自定义 SQL，否则可不写单元测试，主要靠集成测试覆盖。
    *   **Pojo/DTO/VO**：纯数据载体，除自定义校验逻辑外，一般不测试 Java Bean 方法。

## 2. 测试环境与工具 (Environment & Tools)
| 工具 | 版本 | 用途 |
| :--- | :--- | :--- |
| **JUnit 5** | 5.8+ | 单元测试基础框架 |
| **Mockito** | 4.0+ | 模拟依赖（Mocking Framework），隔离被测对象 |
| **Spring Boot Test** | 2.7.x | 提供 Spring 上下文测试支持 (MockMvc 等) |
| **H2 Database** | (可选) | 内存数据库，用于 Repository 层集成测试 |
| **Jacoco** | 0.8+ | 代码覆盖率统计报告 |

## 3. 测试策略 (Test Strategy)

### 3.1 Service 层测试 (Pure Unit Test)
*   **原则**：**只测试业务逻辑，不启动 Spring 容器**。
*   **方法**：使用 `@ExtendWith(MockitoExtension.class)`。
*   **Mock**：使用 `@Mock` 模拟 Mapper 或其他 Service 依赖。
*   **Inject**：使用 `@InjectMocks` 将 Mock 对象注入到被测 Service 中。
*   **验证**：
    *   `Assertions` 验证返回值是否符合预期。
    *   `verify()` 验证依赖方法是否被正确调用（次数、参数）。
    *   `assertThrows()` 验证异常情况是否正确抛出 BusinessException。

### 3.2 Controller 层测试 (Slice Test)
*   **原则**：测试 HTTP 请求处理，参数校验，以及 Service 调用的连接。
*   **方法**：使用 `@WebMvcTest(TargetController.class)`，只加载通过 Controller 相关的 Bean。
*   **Mock**：使用 `@MockBean` 模拟 Service 层依赖。
*   **工具**：使用 `MockMvc` 发起模拟 HTTP 请求。
*   **验证**：验证 HTTP Status, Response Body (JSON Path), Service 方法调用。

### 3.3 工具类测试 (Standard Test)
*   **原则**：输入输出测试，覆盖各种边界输入。
*   **方法**：直接实例化工具类或调用静态方法进行 Assertion 验证。

## 4. 命名与规范 (Conventions)
*   **测试类位置**：`src/test/java` 目录下，包路径与 `src/main/java` 保持一致。
*   **测试类命名**：`TargetClass` + `Test` (例如: `ProductServiceImplTest`)。
*   **测试方法命名**：`methodName_Condition_ExpectedResult`。
    *   例如：`add_ValidProduct_Success` (添加有效商品_成功)
    *   例如：`add_NullName_ThrowsException` (添加空名商品_抛出异常)

## 5. 测试覆盖率目标
*   **Service 层**：行覆盖率 > 80%，分支覆盖率 > 70%
*   **Utils 层**：行覆盖率 > 90%
*   **Controller 层**：行覆盖率 > 60%

## 6. 测试用例设计示例 (Templates)

### 6.1 Service 层测试示例 (ProductService)
```java
package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.mapper.ProductMapper;
import eden.pojo.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void add_ValidProduct_ShouldSave() {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        
        when(productMapper.insert(any(Product.class))).thenReturn(1);

        // Act
        productService.add(product);

        // Assert
        verify(productMapper, times(1)).insert(product);
    }

    @Test
    void add_ProductWithNullName_ShouldThrowException() {
        // Arrange
        Product product = new Product();
        product.setPrice(new BigDecimal("100.00")); 
        // Name is null

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.add(product);
        });
        
        // 假设 Service 层有对 name 的非空校验
        assertEquals("商品名称不能为空", exception.getMessage());
        verify(productMapper, never()).insert(any());
    }
}
```

### 6.2 Controller 层测试示例 (ProductController)
```java
package eden.web.controller;

import eden.pojo.Product;
import eden.pojo.vo.ProductVO;
import eden.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void getById_ExistingId_ReturnsProduct() throws Exception {
        // Arrange
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setName("Mock Product");
        
        given(productService.getById(productId)).willReturn(mockProduct);

        // Act & Assert
        mockMvc.perform(get("/product/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("Mock Product"));
    }
}
```

### 6.3 工具类测试示例 (JwtUtils)
```java
package eden.common.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    @Test
    void generateToken_ShouldReturnNonEmptyString() {
        // Arrange
        JwtUtils jwtUtils = new JwtUtils("secretKey12345678901234567890", 3600L);
        
        // Act
        String token = jwtUtils.generateToken("testUser");

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }
}
```
