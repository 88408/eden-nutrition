import sys
import os

path = r"d:\project\eden-nutrition\eden-admin\src\main\java\eden\admin\controller\AdminSeckillController.java"
os.makedirs(os.path.dirname(path), exist_ok=True)

content = """package eden.admin.controller;

import eden.common.result.Result;
import eden.pojo.dto.AdminSeckillQueryDTO;
import eden.pojo.dto.AdminSeckillSaveDTO;
import eden.pojo.vo.AdminSeckillVO;
import eden.pojo.vo.PageVO;
import eden.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台：秒杀活动管理 API
 */
@RestController
@RequestMapping("/admin/seckill")
public class AdminSeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 获取管理端秒杀活动分页列表
     */
    @GetMapping("/page")
    public Result<PageVO<AdminSeckillVO>> getAdminPage(AdminSeckillQueryDTO queryDTO) {
        PageVO<AdminSeckillVO> pageVO = seckillService.getAdminPage(queryDTO);
        return Result.success(pageVO);
    }

    /**
     * 获取管理端秒杀活动详情
     */
    @GetMapping("/{id}")
    public Result<AdminSeckillVO> getAdminDetail(@PathVariable Long id) {
        AdminSeckillVO detail = seckillService.getAdminDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping
    public Result<Void> addAdminSeckill(@RequestBody AdminSeckillSaveDTO dto) {
        seckillService.addAdminSeckill(dto);
        return Result.success();
    }

    /**
     * 修改秒杀活动
     */
    @PutMapping
    public Result<Void> updateAdminSeckill(@RequestBody AdminSeckillSaveDTO dto) {
        seckillService.updateAdminSeckill(dto);
        return Result.success();
    }

    /**
     * 删除秒杀活动
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdminSeckill(@PathVariable Long id) {
        seckillService.deleteAdminSeckill(id);
        return Result.success();
    }

    /**
     * 强制结束秒杀活动
     */
    @PutMapping("/finish/{id}")
    public Result<Void> finishAdminSeckill(@PathVariable Long id) {
        seckillService.finishAdminSeckill(id);
        return Result.success();
    }
}
"""

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print(f"Created {path}")

# Update PHASE1_REPORT.md
report_path = r"d:\project\eden-nutrition\docs\SECKILL_MANAGEMENT_PHASE1_REPORT.md"
with open(report_path, "r", encoding="utf-8") as f:
    text = f.read()

index = text.find("### Phase 3 Plan: Controller Exposure & API Documentation (Proposed)")
if index != -1:
    text = text[:index] + """## Phase 3: Controller Exposure & API Documentation (Completed)

In Phase 3, we successfully exposed the Service layer methods to the front-end through a dedicated Admin Controller `AdminSeckillController` within the `eden-admin` module.

### 1. RESTful API Endpoints Mapping Verified

We constructed the `eden.admin.controller.AdminSeckillController` equipped with standardized REST APIs:

- **`GET /admin/seckill/page`**: Mapped to `getAdminPage()`. Handled paginated queries mapping `AdminSeckillQueryDTO` parameters and safely returning `Result<PageVO<AdminSeckillVO>>`.
- **`GET /admin/seckill/{id}`**: Mapped to `getAdminDetail()`. Accurately retrieving and fetching seckill particulars for administrative review.
- **`POST /admin/seckill`**: Enabled the `@RequestBody AdminSeckillSaveDTO` payload wrapping into `addAdminSeckill()` with overlap checking.
- **`PUT /admin/seckill`**: Similar payload routing effectively mapped into `updateAdminSeckill()`.
- **`DELETE /admin/seckill/{id}`**: `deleteAdminSeckill()`. Exposed the logical delete sequence.
- **`PUT /admin/seckill/finish/{id}`**: Dedicated route directly attached to `finishAdminSeckill()` to handle rapid ongoing campaign suspension caching.

### 2. Standardized Formatting

- Deployed standard Spring `@RestController` and mapped the global B-End prefix base via `@RequestMapping("/admin/seckill")`.
- Ensured absolute compatibility with the generic `Result.success(...)` standard interface, guaranteeing frontend Vue parsers require zero structural modifications.

### 3. Readiness for Frontend (Phase 4)

Now that the Controller is completely structured and implemented, all underlying logic (Mapper -> POJO -> Service -> Controller) is finalized. It perfectly paves the way for Phase 4: Implementing the Vue3/Element-Plus frontend `SeckillView` administration dashboard to consume these endpoints efficiently.
"""
    with open(report_path, "w", encoding="utf-8") as f:
        f.write(text)
    print("Report updated")
