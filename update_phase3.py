import sys

path = r"d:\project\eden-nutrition\docs\SECKILL_MANAGEMENT_PHASE1_REPORT.md"

with open(path, "r", encoding="utf-8") as f:
    text = f.read()

# Replace the "Upcoming Next Phase (Phase 3)" section
old_text = "### Upcoming Next Phase (Phase 3)\n- Constructing and modifying the exposed `SeckillController` for B-End interaction (Defining API mappings locally mapping to Service).\n- Swagger Annotations / API Documentation updating.\n- Potential basic unit test confirmations enforcing correctness on edge cases (e.g. overlap validations)."

new_text = """### Phase 3 Plan: Controller Exposure & API Documentation (Proposed)

In the upcoming Phase 3, we will expose the completed Service layer methods to the front-end through a dedicated Admin Controller. Based on existing modular conventions, we will introduce `AdminSeckillController` inside the `eden-admin` module.

#### 1. RESTful API Endpoints Mapping

We will construct `eden.admin.controller.AdminSeckillController` equipped with standardized REST APIs:

- **`GET /admin/seckill/page`**: Maps to `getAdminPage()`. Handles paginated query string parameters (`AdminSeckillQueryDTO`) returning `Result<PageVO<AdminSeckillVO>>`.
- **`GET /admin/seckill/{id}`**: Maps to `getAdminDetail()`. Returns detailed info of a campaign.
- **`POST /admin/seckill`**: Maps to `addAdminSeckill()`. Accepts `@RequestBody AdminSeckillSaveDTO`.
- **`PUT /admin/seckill`**: Maps to `updateAdminSeckill()`. Accepts `@RequestBody AdminSeckillSaveDTO`.
- **`DELETE /admin/seckill/{id}`**: Maps to `deleteAdminSeckill()`. Standard logical delete for an activity. 
- **`PUT /admin/seckill/finish/{id}`**: Maps to `finishAdminSeckill()`. A dedicated pathway to forcefully and immediately terminate an ongoing campaign and flush Redis limits.

#### 2. Documentation & Permissions
Although specific Swagger annotations (`@Api`, `@ApiOperation`) and security filters might vary globally across the system, we will inject core annotations directly into the Controller layout to ensure auto-generation of API docs:
- Class-level: `@RestController`, `@RequestMapping("/admin/seckill")`.
- Integration formatting following `Result.success()` wrapping standard.

#### 3. Post-Phase Integration
Once this Controller is validated locally via testing tools (like Postman or Swagger UI), it perfectly clears the way for Phase 4: Implementation of the Vue3/Element-Plus frontend interface to consume these exact standardized endpoints."""

if old_text in text:
    text = text.replace(old_text, new_text)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)
    print("Report updated")
else:
    print("Text not found")
