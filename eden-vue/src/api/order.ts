import client from './client';
import { ApiResponse, Order, PageResult } from '../types';

export const createOrder = (data: { addressId: number; productIds: number[]; remark?: string; userCouponId?: number }) => {
  return client.post<any, ApiResponse<{ orderNo: string }>>('/order/create', data);
};

export const getOrders = (params: any) => {
  return client.get<any, ApiResponse<PageResult<Order>>>('/order/list', { params });
};

export const getOrderDetail = (orderNo: string) => {
  return client.get<any, ApiResponse<Order>>(`/order/${orderNo}`);
};

export const payOrder = (orderNo: string) => {
  return client.post<any, ApiResponse<any>>(`/order/pay/${orderNo}`);
};

// ================= 商品收藏相关 =================
export const toggleFavorite = (productId: number) => {
  return client.post<any, ApiResponse<boolean>>(`/product/favorite/${productId}`);
};

export const checkFavorite = (productId: number) => {
  return client.get<any, ApiResponse<boolean>>(`/product/favorite/check/${productId}`);
};
