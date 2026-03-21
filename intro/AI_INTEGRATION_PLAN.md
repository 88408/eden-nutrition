# Eden Nutrition - AI Integration Proposal (AI 功能集成方案)

本文档旨在为 Eden Nutrition 电商平台提供人工智能（AI）功能集成的技术方案与路线图。通过引入 AI 技术，提升用户体验（UX）、运营效率以及销售转化率。

## 1. 核心应用场景 (Core Scenarios)

### A. 智能客服助手 (Smart Customer Support Chatbot)
**目标**: 24/7 解答用户疑问，处理订单查询，提供营养建议。
- **功能**:
  - 自然语言问答（基于产品库和营养知识库）。
  - 订单状态查询（对接 Order Service）。
  - 个性化推荐交互。
- **技术栈**:
  - **LLM**: OpenAI GPT-4o / Azure OpenAI / Google Gemini via API.
  - **RAG (检索增强生成)**: 将产品手册、FAQ 向量化存入向量数据库。
  - **框架**: LangChain4j (Java) 或 Sidecar Python Service (FastAPI + LangChain).

### B. 智能搜索与推荐 (AI-Powered Search & Recommendations)
**目标**: 提升商品查找准确率，增加客单价。
- **功能**:
  - **语义搜索**: 支持 "适合老人的高钙奶粉" 这样的自然语言搜索，而不仅仅是关键词匹配。
  - **相似商品推荐**: 在详情页推荐 "猜你喜欢"。
  - **个性化首页**: 基于用户历史行为重排首页商品。
- **技术栈**:
  - **Search Engine**: 
    - 方案一 (轻量): 升级 Elasticsearch 至 8.x 利用其内置的 kNN 向量搜索。
    - 方案二 (专业): 引入专有向量数据库 (Milvus / Qdrant) 配合现有 MySQL。
  - **Embedding Model**: text-embedding-3-small 或开源模型 (HuggingFace).

### C. 运营内容生成 (Admin Content Generation)
**目标**: 减少运营人员编写商品详情、营销文案的时间。
- **功能**:
  - **商品文案生成**: 输入关键词（如 "无糖、高蛋白、草莓味"），自动生成 SEO 友好的商品标题和详情页 HTML。
  - **营销邮件/短信生成**: 自动生成促销活动文案。
  - **多语言翻译**: 该平台如有出海需求，自动翻译商品信息。
- **技术栈**:
  - 直接集成 LLM API 到 `eden-admin` 模块。

## 2. 推荐实施路线 (Implementation Roadmap)

### Phase 1: 基础设施与 MVP (快速赢取)
1.  **LLM 接入**: 在 `eden-common` 中封装统一的 LLM Client (OpenAI/Azure SDK)。
2.  **Admin 文案生成**: 在后台商品发布页增加 "AI 一键生成描述" 按钮。
    - 成本低，风险低，立竿见影提升运营效率。

### Phase 2: 直面用户 (提升转化)
1.  **RAG 知识库搭建**: 
    - 提取 `product` 表和 `docs/*.md` 资料。
    - 生成 Vector Embeddings 并存入 Elasticsearch (这是最顺滑的路径，因为项目中已有 ES 依赖)。
2.  **智能导购 Bot**: 
    - 在前端 (`eden-vue`) 右下角悬浮窗接入 Chat Widget。
    - 后端开发 `eden-ai-service` 处理对话上下文和检索。

### Phase 3: 深度集成 (数据驱动)
1.  **用户画像分析**: 利用 Python 数据分析服务分析 `user` 和 `order` 数据，打标签。
2.  **全链路推荐**: 首页、购物车、支付成功页的动态推荐位。

## 3. 技术架构调整建议 (Architecture Changes)

### 方案 A: Java Native (推荐 - 维护成本低)
由于现有架构是 Spring Boot，推荐使用 **Spring AI** 或 **LangChain4j**。
- **优点**: 无需引入 python 异构技术栈，运维简单。
- **缺点**: Java 生态在深度学习模型训练方面稍弱，但调用 API 足够强大。

### 方案 B: Python Sidecar (适合复杂 AI 需求)
新增一个 `eden-ai-python` 服务 (FastAPI/Flask)。
- **优点**: 拥有最丰富的 AI 库 (PyTorch, TensorFlow, LangChain)。
- **缺点**: 增加运维复杂度 (Docker 容器增多，服务间通信)。

## 4. 具体代码落地 (Concrete Implementation)

鉴于项目当前基于 **Spring Boot 2.7.18** 和 **JDK 17**，我们推荐使用兼容性极佳的 **LangChain4j** 框架。它能无缝集成到现有 Spring Boot 2.x 环境中，且 API 设计优雅，支持声明式服务调用。

### 4.1 引入依赖 (Maven)

在根目录或 `eden-common` 模块的 `pom.xml` 中添加 `langchain4j` 依赖：

```xml
<properties>
    <langchain4j.version>0.31.0</langchain4j.version>
</properties>

<dependencies>
    <!-- LangChain4j Core: 集成 OpenAI (或其他模型提供商) -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-open-ai</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
    <!-- Spring Boot Starter: 简化自动配置 -->
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-spring-boot-starter</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
</dependencies>
```

### 4.2 配置文件 (Configuration)

在 `eden-admin/src/main/resources/application.yml` (或 Nacos 配置中心) 中添加模型配置：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${OPENAI_API_KEY} # 建议从环境变量读取
      base-url: https://api.openai.com/v1 # 如果使用代理或国内中转及 Azure，需修改此处
      model-name: gpt-3.5-turbo  # 生产环境推荐 gpt-4o
      temperature: 0.7
      log-requests: true
      log-responses: true
```

### 4.3 核心服务实现 (Service Layer)

在 `eden-service` 模块中，利用 LangChain4j 的 `AiServices` 创建声明式 AI 服务。

**1. 定义 AI 服务接口 `ProductCopywriter.java`:**

```java
package com.eden.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI 商品文案生成器接口
 */
public interface ProductCopywriter {

    @SystemMessage("你是一个专业的健康食品电商文案策划大师。你的目标是根据商品信息撰写吸引人的营销文案。\n" +
                   "要求：\n" +
                   "1. 输出格式为 HTML (包含 <h3>, <p>, <ul> 等标签)。\n" +
                   "2. 语气专业、亲切、健康向。\n" +
                   "3. 重点突出营养价值。")
    @UserMessage("请为一款名为 \"{{name}}\" 的商品撰写详情页文案。\n" +
                 "核心关键词：{{keywords}}。\n" +
                 "目标人群：{{targetAudience}}。\n" +
                 "文案风格：{{style}}。")
    String generateDescription(@V("name") String name, 
                               @V("keywords") String keywords, 
                               @V("targetAudience") String targetAudience,
                               @V("style") String style);
}
```

**2. 配置 Bean (`AiConfig.java`):**

在 `eden-config` 或 `eden-service` 的配置包中：

```java
package com.eden.config;

import com.eden.service.ai.ProductCopywriter;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ProductCopywriter productCopywriter(ChatLanguageModel chatLanguageModel) {
        // LangChain4j 会自动注入配置好的 ChatLanguageModel (基于 yaml 配置)
        return AiServices.builder(ProductCopywriter.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}
```

### 4.4 Web 层接入 (Controller)

在 `eden-admin` 模块中暴露接口供前端使用：

```java
@RestController
@RequestMapping("/admin/ai")
@Tag(name = "AI 辅助工具")
public class AiController {

    @Autowired
    private ProductCopywriter productCopywriter;

    @PostMapping("/generate/product-desc")
    public CommonResult<String> generateProductDesc(@RequestBody AiGenerateRequest req) {
        // 校验参数...
        String content = productCopywriter.generateDescription(
            req.getName(), 
            req.getKeywords(), 
            req.getTargetAudience(), 
            req.getStyle()
        );
        return CommonResult.success(content);
    }
}

// REQUEST DTO
@Data
class AiGenerateRequest {
    private String name;
    private String keywords;
    private String targetAudience; // 如：老人、健身人群
    private String style;          // 如：专业严谨、轻松活泼
}
```

### 4.5 数据库与 RAG (进阶 Phase 2)

为了实现**智能问答 (Chatbot)**，后续需引入向量存储。考虑到项目已有 Elasticsearch，可以使用其向量插件。

**代码预览 (RAG Ingest)**:
```java
// 当商品更新时，将描述转化为向量存入 ES
@Component
public class ProductVectorSyncer {
    
    @Autowired EmbeddingModel embeddingModel; // 同样由 LangChain4j 提供
    @Autowired VectorStore vectorStore;       // LangChain4j ElasticsearchStore

    @EventListener
    public void onProductUpdate(ProductUpdateEvent event) {
        Product p = event.getProduct();
        TextSegment segment = TextSegment.from(
            p.getName() + ": " + p.getDescription(), 
            Metadata.from("productId", p.getId())
        );
        Embedding embedding = embeddingModel.embed(segment).content();
        vectorStore.add(embedding, segment);
    }
}
```

## 5. 资源需求估算
- **API 成本**: 预估 $20-$50/月 (基于 OpenAI API，视流量而定)。
- **服务器**: 
  - Elasticsearch 需增加内存以支持向量索引。
  - 推荐增加一台 4C8G 服务器专门运行 AI 相关辅助服务（如果自建模型）。
