import os
import re

controllers_dir = r'd:\project\eden-nutrition\eden-web\src\main\java\eden\web\controller'
out_doc = r'd:\project\eden-nutrition\docs\CLIENT_API_DOCUMENTATION.md'
out_prompt = r'd:\project\eden-nutrition\docs\CLIENT_FRONTEND_GENERATION_PROMPT.md'

with open(out_doc, 'w', encoding='utf-8') as f_doc, open(out_prompt, 'w', encoding='utf-8') as f_prompt:
    f_doc.write('# C端前端交互 API 完整文档\n\n')
    
    f_prompt.write('# C端前端生成提示词 (Prompt)\n\n## 1. 背景要求\n我是基于 Spring Boot 开发的伊甸园营养品商城，这部分接口是面向 C 端普通用户。我需要一个非常现代化的 React / Vue3 前端应用（支持移动端或响应式浏览器界面）。请依据下方的完整后端 API 文档，为我生成所有的 `src/api/*.ts` 封装请求服务（建议使用 Axios）以及所有的关键交互页面组件（如：**首页推荐及商品列表、分类导航、商品详情页与加购物车、秒杀频道、购物车页面、下单结算页面、个人中心地址管理、订单支付流水追踪**）。\n\n')
    f_prompt.write('## 2. Axios 请求配置要求\n在全局请求拦截器中携带来自本地存储的 JWT Token（Storage Key 一般为 `token`），并在响应拦截器处理未登录或无权限的系统导航逻辑。\n\n')
    f_prompt.write('## 3. 后端可用完整 API 列表\n\n')

    for file in os.listdir(controllers_dir):
        if not file.endswith('Controller.java'):
            continue
            
        with open(os.path.join(controllers_dir, file), 'r', encoding='utf-8') as f:
            content = f.read()
            
        module_name = file.replace('Controller.java', '')
        
        # Get base path
        base_path = ""
        base_match = re.search(r'@RequestMapping\(\s*\"(.*?)\"\s*\)', content)
        if base_match:
            base_path = base_match.group(1)
            
        f_doc.write(f'## 模块 {module_name} (API Base: `{base_path}`)\n\n')
        f_prompt.write(f'### {module_name} 模块 (`{base_path}`)\n')
            
        # extract methods perfectly
        # signature example:  @GetMapping("/xxx") public Result<CartVO> methodName() {
        # Using a more robust parser regex
        
        # Split by public Result to find methods
        parts = content.split('public Result')
        if len(parts) <= 1:
            continue
            
        for i in range(1, len(parts)):
            pre_part = parts[i-1]
            post_part = parts[i]
            
            # Find the closest @Mapping before this 'public Result'
            # Look backwards in pre_part
            mapping_match = re.search(r'@(Get|Post|Put|Delete)Mapping([\s\S]*?)$', pre_part)
            if not mapping_match:
                continue
                
            http_method = mapping_match.group(1).upper()
            mapping_args = mapping_match.group(2)
            
            sub_path = ""
            if mapping_args:
                # check value or just string
                str_match = re.search(r'\"([^\"]*)\"', mapping_args)
                if str_match:
                    sub_path = str_match.group(1)
            
            full_path = base_path + sub_path
            
            # Extract method info from post_part
            method_sig = post_part.split('{')[0].strip()
            # method_sig looks like: <List<UserAddress>> list(@CurrentUser Long userId)
            
            # Extract javadoc comment before mapping
            doc_idx = pre_part.rfind('/**')
            end_idx = pre_part.rfind('*/')
            desc = ""
            if doc_idx != -1 and end_idx != -1 and doc_idx < end_idx:
                # if it's the right comment for this method (not class comment)
                if '@RestController' not in pre_part[end_idx:]:
                    doc_chunk = pre_part[doc_idx:end_idx].split('\n')
                    desc = "".join([d.strip().lstrip('*').strip() for d in doc_chunk if d.strip()]).replace('/**', '')
            
            f_doc.write(f'### `{http_method}` `{full_path}`\n')
            f_doc.write(f'- **说明**: {desc}\n')
            f_doc.write(f'- **签名**: `public Result{method_sig}`\n\n')
            
            f_prompt.write(f'- `{http_method}` `{full_path}` - {desc if desc else method_sig.split("(")[0]}\n')
            
    f_prompt.write('\n\n## 4. 特别说明与指引\n')
    f_prompt.write('请按照以上的后端接口，组织出完整结构的项目代码。重点包括：\n')
    f_prompt.write('1. `request.ts` 的配置方案代码。\n')
    f_prompt.write('2. 在 `src/api` 目录下将每个模块细分的文件及对应的类型接口声明 (interface/type)。\n')
    f_prompt.write('3. 具体页面的路由守卫逻辑，并写出能真实调用业务的主逻辑骨架代码。\n')
    f_prompt.write('\n\n请不要生成假数据，所有数据与操作均需和该 API 一对一贴合联动。')

print("Doc & Prompt Generator Executed Successfully.")