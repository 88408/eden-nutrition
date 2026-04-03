# Eden Nutrition - B端秒杀管理模块开发方案 (FEATURE_SECKILL_MANAGEMENT_PLAN)

## 1. 业务背景与目标
当前Eden Nutrition系统的C端已经实现了完整的秒杀链路（商品秒杀、Redis预扣库存、MQ异步下单落库）。但在B端，管理员依然缺乏一个可以统一配置、管理和创建“秒杀活动场次”的基础页面后端支撑。
本方案旨在落地**B端秒杀管理 (Admin Seckill Management)**的功能，允许运营人员在管理后台轻松地新增秒杀商品、设定库存与价格，并管理秒杀档期。

## 2. 现状分析

现有的表结构和实体对象：
1. **`SeckillProduct` (POJO)**：表示具体的秒杀活动与商品映射。
   - `id`: 秒杀ID
   - `productId`: 关联商品ID
   - `seckillPrice`: 秒杀价格
   - `stockCount/stock`: 秒杀库存
   - `startTime/endTime`: 开始时间/结束时间
   - `status`: 状态 (0-未开始, 1-进行中, 2-已结束)
2. **`SeckillMapper`**:
   - `selectById`, `selectOngoing`, `selectUpcoming`, `selectByTimeRange`, `insert`, `update`, `deductStock`, `updateEndedSeckills`

## 3. 功能需求规划 (B端)

对于管理后台端，我们需要以下的接口来管理上述实体：

### 3.1 查询与浏览
* **秒杀活动分页列表**：
  * 支持条件查询：根据状态 (`status`)、商品ID (`productId`) 进行查询。
  * 列表数据除了返回 `SeckillProduct` 信息外，还需带出关联商品的基础信息（名称 `name`、主图 `mainImage`）。

### 3.2 配置与运维
* **创建秒杀活动**：
  * 选择商品，设置秒杀库存（`stock`）、秒杀价格（`seckillPrice`）、时间档期（`startTime` 与 `endTime`）。
  * ⚠️ *防重校验*：同一商品在同一时间段内不能有重复的、正在进行的秒杀活动。
  * ⚠️ *缓存同步*：创建或修改完毕后，如马上即将开始，需要将活动信息同步至Redis（供高并发读取）。
* **修改秒杀活动**：
  * 支持在 **"0-未开始"** 状态下修改信息。一旦进行中或已结束，则限制无法修改核心数据。
* **删除秒杀活动**：
  * 支持在 **"0-未开始"** 或者 **"2-已结束"** 状态下进行逻辑或物理删除，正在进行的秒杀禁止删除。
* **强制下线/结束**：
  * 对于因为突发情况必须终止的秒杀活动，支持人为手动将状态修改为“2-已结束”。

## 4. 后端接口设计 (Controller Layer)

属于模块：`eden-admin`，创建 `AdminSeckillController.java`

| 接口名称 | Method | 路由 | 描述 |
| :--- | :--- | :--- | :--- |
| **分页查询** | `GET` | `/admin/seckill/page` | 分页获取所有秒杀活动，按状态筛选 |
| **详情查询** | `GET` | `/admin/seckill/{id}` | 查询单个秒杀配置详情 |
| **新增秒杀** | `POST` | `/admin/seckill` | 新增秒杀配置 (需在请求体传入商品ID与时间配置) |
| **修改秒杀** | `PUT` | `/admin/seckill` | 修改未开始的秒杀配置 |
| **删除秒杀** | `DELETE` | `/admin/seckill/{id}` | 删除未开始或已结束的秒杀 |
| **结束活动** | `PUT` | `/admin/seckill/{id}/finish` | 强制中止正在进行的秒杀 |

### 4.1 数据传输对象 (DTO/VO)

在 `eden-pojo` 模块新增：

**`AdminSeckillQueryDTO.java`**
```java
public class AdminSeckillQueryDTO extends PageDTO {
    private Long productId;
    private Integer status; // 0-未开始, 1-进行中, 2-已结束
}
```

**`AdminSeckillSaveDTO.java`**
```java
public class AdminSeckillSaveDTO {
    private Long id; // 修改时传入
    private Long productId;
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

**`AdminSeckillVO.java`**
需要封装前端展示的对象：
```java
public class AdminSeckillVO extends SeckillProduct {
    private String productName;
    private String productMainImage;
    private BigDecimal originalPrice;
}
```

## 5. 服务层演进方案 (Service & Mapper)

### 5.1 Mapper改造 (`SeckillMapper.xml`)
补充对于 B 端的分页查询支持，包含外连接 `product` 表带出名字等冗余数据。
```xml
<select id="selectAdminPage" resultType="eden.pojo.vo.AdminSeckillVO">
    SELECT s.*, p.name as productName, p.main_image as productMainImage, p.price as originalPrice
    FROM seckill_product s
    LEFT JOIN product p ON s.product_id = p.id
    <where>
        <if test="productId != null"> AND s.product_id = #{productId} </if>
        <if test="status != null"> AND s.status = #{status} </if>
    </where>
    ORDER BY s.create_time DESC
    LIMIT #{offset}, #{pageSize}
</select>
```

### 5.2 Service增强 (`SeckillService.java` & `SeckillServiceImpl.java`)
- `PageVO<AdminSeckillVO> getAdminPage(AdminSeckillQueryDTO queryDTO);`
- `void addSeckill(AdminSeckillSaveDTO dto);`
  - 核心逻辑：校验 `startTime < endTime`，且当前时间没有针对同一个商品重叠的活动。
- `void updateSeckill(AdminSeckillSaveDTO dto);`
  - 核心逻辑：拦截校验 `status == 0`，同步更新最新时间落库。
- `void deleteSeckill(Long id);`
  - 核心逻辑：确保活动不是进行中，并清理可能存在的Redis缓存预热数据。
- `void finishSeckill(Long id);`
  - 更新状态为2，主动清退该秒杀商品的Redis余量信息（Redis Key: `seckill:stock:{id}`）。

## 6. 实施步骤规划

1. **Phase 1: DTO/VO定义与Mapper扩建** (预计 1小时)
   - 编写以上约定的 `DTO` 与 `VO`。
   - 于 `SeckillMapper.java` 以及 `.xml` 新增由于联表分页导致的 `selectAdminPage`、`countAdminPage` 查询手段。
2. **Phase 2: B端 Service层能力补充** (预计 2小时)
   - 在 `SeckillServiceImpl` 扩展对应的方法（增删改查、强行结束）。
   - 实现缓存清除的串联（防止停用后C端还在卖）。
3. **Phase 3: Controller编写** (预计 0.5小时)
   - 在 `eden-admin` 下增添 `AdminSeckillController` 并适配 `@PreAuthorize` 鉴权与参数校验。
4. **Phase 4: 前端联调映射** (视前端进度而定)
   - 暴露接口协议给前端 `eden-admin-vue/src/api/seckill.ts` 中发起 Axios 替换。

## 7. 注意事项

- **状态自动流转**：依赖系统原先就有的 `Cron` 定时任务（负责将已过期的秒杀改为状态2），在Admin端新增或修改活动时间时，必须要保证活动时效正确。
- **缓存的一致性**：创建活动时可能会写入Redis进行预热。一旦在管理系统手动修改或停止，必须立刻执行 `redisTemplate.delete(RedisConstants.SECKILL_STOCK_PREFIX + seckillId)` ，防止出现超卖或者不应该被售卖的秒杀品流出。 
