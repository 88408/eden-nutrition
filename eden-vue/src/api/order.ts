import client from './client';
import { ApiResponse, Order, PageResult } from '../types';

export const createOrder = (data: { addressId: number; items: any[] }) => {
  return client.post<any, ApiResponse<{ orderNo: string }>>('/orders', data);
};

export const getOrders = (params: any) => {
  return client.get<any, ApiResponse<PageResult<Order>>>('/orders', { params });
};

export const getOrderDetail = (id: number) => {
  return client.get<any, ApiResponse<Order>>(`/orders/${id}`);
};

export const payOrder = (orderNo: string) => {
  return client.post<any, ApiResponse<any>>(`/orders/${orderNo}/pay`);
};
