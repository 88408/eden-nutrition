# Eden Nutrition - AI Implementation Guide (快速落地指南)

本文档针对 **Admin Content Generation (运营内容生成)** 场景提供具体的实施步骤。这是一个低风险、高感知的切入点。

## 目标 (Goal)
在 `eden-admin-vue` 后台管理系统中，添加一个 "AI 生成描述" 按钮。点击后调用后端接口，自动填写商品的 `description` 和 `detail_html` 字段。

## 后端实现 (Backend - Spring Boot)

### 1. 引入依赖
在 `eden-common` 或 `eden-service` 的 `pom.xml` 中引入 `spring-ai` 或简单的 `okhttp` 客户端。为了兼容性，推荐使用简单的 HTTP Client 调用 OpenAI 兼容接口。

```xml
<!-- 示例: 使用 OkHttp -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.10.0</version>
</dependency>
```

### 2. 配置 API Key
在 `application.yml` 或 `nacos` 配置中心添加：
```yaml
ai:
  openai:
    api-key: sk-your-api-key-here
    base-url: https://api.openai.com/v1  # 或国内代理地址
    model: gpt-3.5-turbo
```

### 3. 创建 AI Service
在 `eden-service` 模块创建 `AiGenerationService`。

```java
@Service
@Slf4j
public class AiGenerationService {

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateProductCopy(String productName, String category, String keywords) {
        String prompt = String.format(
            "你是一个专业的电商运营专家。请为一款名为“%s”的商品撰写吸引人的营销文案。\n" +
            "分类：%s\n关键词：%s\n" +
            "要求：\n1. 包含核心卖点提炼。\n2. 语气亲切，富有感染力。\n3. 使用HTML格式输出，包含<h3>和<p>标签。",
            productName, category, keywords
        );

        // 构建请求体 (简化的JSON结构)
        ObjectNode json = mapper.createObjectNode();
        json.put("model", "gpt-3.5-turbo");
        json.putArray("messages")
            .addObject()
            .put("role", "user")
            .put("content", prompt);

        // 发起请求... (省略具体 HTTP 调用代码)
        return extractContent fromResponse;
    }
}
```

### 4. 新增 Controller 接口
在 `eden-admin` (或 `eden-web`) 中暴露接口给前端调用。

```java
@RestController
@RequestMapping("/admin/ai")
public class AdminAiController {

    @Autowired
    private AiGenerationService aiService;

    @PostMapping("/generate-copy")
    public CommonResult<String> generateCopy(@RequestBody AiGenerateRequest req) {
        String content = aiService.generateProductCopy(req.getName(), req.getCategory(), req.getKeywords());
        return CommonResult.success(content);
    }
}
```

## 前端实现 (Frontend - Vue/React)

### 1. 界面修改 (Product Edit Page)
在商品编辑表单的 "详情描述" 输入框旁，添加一个 ✨ 按钮。

### 2. 调用逻辑
当点击按钮时：
1.  获取当前表单中的 "商品名称" 和 "分类"。
2.  弹出 Modal 让用户输入额外关键词（可选）。
3.  调用 `/admin/ai/generate-copy` 接口，显示 Loading 状态。
4.  成功后，将返回的 HTML 填充到富文本编辑器中。

## 下一步 (Next Steps)
- **多图生成**: 集成 DALL-E 3 生成商品展示图。
- **SEO 优化**: 让 AI 自动生成 `meta_title` 和 `meta_description`。
