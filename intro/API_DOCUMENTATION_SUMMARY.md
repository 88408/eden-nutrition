# Eden Nutrition Backend API Documentation Summary

This document summarizes the available backend API endpoints based on the `eden-web` module controllers.

**Base URL**: `/api` (See `application.yml` `server.servlet.context-path`)

## 1. User Management (`/user`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| POST | `/user/register` | 用户注册 | No |
| POST | `/user/login` | 用户登录 | No |
| POST | `/user/logout` | 用户登出 | Yes |
| GET | `/user/info` | 获取当前用户信息 | Yes |
| PUT | `/user/info` | 更新用户信息 | Yes |
| PUT | `/user/password` | 修改密码 | Yes |
| GET | `/user/check/username` | 检查用户名是否存在 | No |
| GET | `/user/check/phone` | 检查手机号是否存在 | No |

## 2. Product Management (`/product`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/product/{id}` | 获取商品详情 | No |
| GET | `/product/list` | 商品列表查询 (支持分页、筛选、排序) | No |
| GET | `/product/hot` | 获取热门商品 | No |
| GET | `/product/recommend` | 获取推荐商品 | No |
| GET | `/product/new` | 获取新品列表 | No |

## 3. Product Category (`/category`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/category/tree` | 获取分类树 | No |
| GET | `/category/first` | 获取一级分类 | No |
| GET | `/category/children/{parentId}` | 获取子分类 | No |
| GET | `/category/{id}` | 获取分类详情 | No |
| POST | `/category/add` | 新增分类 | Yes (Admin) |

## 4. Shopping Cart (`/cart`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/cart` | 获取购物车信息 | Yes |
| POST | `/cart/add` | 添加商品到购物车 | Yes |
| PUT | `/cart/quantity` | 更新购物车商品数量 | Yes |
| DELETE | `/cart/{productId}` | 删除购物车商品 | Yes |
| DELETE | `/cart/clear` | 清空购物车 | Yes |
| GET | `/cart/count` | 获取购物车商品数量 | Yes |
| PUT | `/cart/select` | 选中/取消选中商品 | Yes |
| PUT | `/cart/selectAll` | 全选/取消全选 | Yes |

## 5. Order Management (`/order`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| POST | `/order/create` | 创建订单 | Yes |
| GET | `/order/list` | 获取用户订单列表 | Yes |
| GET | `/order/{orderNo}` | 获取订单详情 | Yes |
| POST | `/order/cancel/{orderNo}` | 取消订单 | Yes |
| POST | `/order/pay/{orderNo}` | 支付订单 | Yes |
| POST | `/order/confirm/{orderNo}` | 确认收货 | Yes |
| DELETE | `/order/{orderNo}` | 删除订单 | Yes |
| GET | `/order/admin/list` | 管理员查询订单列表 | Yes (Admin) |

## 6. Address Management (`/address`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/address/list` | 获取收货地址列表 | Yes |
| GET | `/address/default` | 获取默认收货地址 | Yes |
| GET | `/address/{id}` | 获取地址详情 | Yes |
| POST | `/address` | 添加收货地址 | Yes |
| PUT | `/address` | 更新收货地址 | Yes |
| DELETE | `/address/{id}` | 删除收货地址 | Yes |
| PUT | `/address/default/{id}` | 设置默认地址 | Yes |

## 7. Coupon Management (`/coupon`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/coupon/available` | 获取可领取的优惠券列表 | No |
| POST | `/coupon/receive/{couponId}` | 领取优惠券 | Yes |
| GET | `/coupon/my` | 获取我的优惠券列表 | Yes |
| GET | `/coupon/usable` | 获取下单时可用的优惠券 | Yes |

## 8. Seckill (Flash Sale) (`/seckill`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/seckill/sessions` | 获取秒杀场次列表 | No |
| GET | `/seckill/list` | 获取所有秒杀活动 | No |
| GET | `/seckill/ongoing` | 获取进行中的秒杀活动 | No |
| GET | `/seckill/upcoming` | 获取即将开始的秒杀活动 | No |
| GET | `/seckill/{seckillId}` | 获取秒杀商品详情 | No |
| POST | `/seckill/do` | 执行秒杀 (下单) | Yes |
| GET | `/seckill/check/{seckillId}` | 检查是否已参与秒杀 | Yes |
| POST | `/seckill/create` | 创建秒杀活动 | Yes (Admin) |
| PUT | `/seckill/update` | 更新秒杀活动 | Yes (Admin) |
| POST | `/seckill/publish/{id}` | 发布/上架秒杀活动 | Yes (Admin) |

## 9. Product Reviews (`/review`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| GET | `/review/product/{productId}` | 获取商品评价列表 | No |
| GET | `/review/product/{productId}/stats` | 获取商品评价统计 | No |
| POST | `/review` | 添加评价 | Yes |
| DELETE | `/review/{reviewId}` | 删除评价 | Yes |
