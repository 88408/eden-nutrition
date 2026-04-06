import os
import re

controllers_dir = r'd:\project\eden-nutrition\eden-web\src\main\java\eden\web\controller'
output_file = r'd:\project\eden-nutrition\docs\CLIENT_API_DOCUMENTATION.md'
prompt_file = r'd:\project\eden-nutrition\docs\CLIENT_FRONTEND_GENERATION_PROMPT.md'

with open(output_file, 'w', encoding='utf-8') as out_f, open(prompt_file, 'w', encoding='utf-8') as prompt_f:
    out_f.write('# 客户端 API 完整文档\n\n')
    prompt_f.write('# 客户端前端代码生成 Prompt\n\n')
    
    prompt_f.write('## 背景描述\n我需要构建一个伊甸园营养品商城的 C 端（用户端），可以是一个移动端 H5 网页或小程序，也可以是 PC 端前台页面。请使用 React (推荐 Next.js/Vite) 或 Vue3 构建，并基于下述的完整无遗漏的所有后台 API 进行状态管理对接（如使用 Zustand、Pinia、Zustand 或 Redux）以及 API 集成（Axios 或 fetch）。\n\n')
    prompt_f.write('## 核心功能模块与接口清单\n')

    for root, dirs, files in os.walk(controllers_dir):
        for file in files:
            if file.endswith('Controller.java'):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()

                # Find Controller description
                class_desc = ""
                desc_match = re.search(r'\/\*\*\s*\n([\s\S]*?)\*\/\s*@RestController', content)
                if desc_match:
                    class_desc = re.sub(r'^\s*\*\s*', '', desc_match.group(1), flags=re.MULTILINE).strip()
                
                base_path = ""
                path_match = re.search(r'@RequestMapping\(\"(.*?)\"\)', content)
                if path_match:
                    base_path = path_match.group(1)

                out_f.write(f'## 模块: {file.replace("Controller.java", "")} ({base_path})\n')
                if class_desc:
                    out_f.write(f'> {class_desc}\n\n')
                    
                prompt_f.write(f'### {file.replace("Controller.java", "")} 控制器 ({base_path})\n')

                # Regex to match methods
                methods = re.findall(r'(\/\*\*([\s\S]*?)\*\/)?\s*@(Get|Post|Put|Delete)Mapping(?:\(\"(.*?)\"\))?[\s\S]*?public\s+(.*?)\s+(\w+)\(([\s\S]*?)\)', content)
                
                for m in methods:
                    comment = m[1] if m[1] else ''
                    desc = re.sub(r'^\s*\*\s*', '', comment, flags=re.MULTILINE).strip()
                    http_method = m[2].upper()
                    path = m[3] if m[3] else ''
                    full_path = base_path + path
                    return_type = m[4].strip()
                    method_name = m[5].strip()
                    params = m[6].strip().replace('\n', ' ')

                    out_f.write(f'### {http_method} {full_path}\n')
                    if desc:
                        out_f.write(f'- **功能说明**：{desc}\n')
                    out_f.write(f'- **方法名**：{method_name}\n')
                    out_f.write(f'- **返回值**：{return_type}\n')
                    out_f.write(f'- **参数**：{params}\n\n')
                    
                    prompt_f.write(f'- {http_method} {full_path} : {desc.split(chr(10))[0] if desc else method_name}\n')
                
                out_f.write('---\n\n')
                
    prompt_f.write('\n## 你的任务说明\n')
    prompt_f.write('1. 帮我生成基础的请求封装 equest.ts (基于 Axios)，需要在请求头携带 Authorization token。\n')
    prompt_f.write('2. 帮我生成各模块相关的 API 文件如 pi/user.ts, pi/product.ts, pi/order.ts, pi/seckill.ts 等。\n')
    prompt_f.write('3. 提供客户端关键页面的 React/Vue 组件代码，例如：**首页**、**商品详情页**、**秒杀专区**、**购物车**、**结算页面**、**个人中心**和**订单列表**。\n')
    prompt_f.write('4. 确保在页面组件中涵盖网络请求与错误处理逻辑（如未登录时重定向）。\n')
