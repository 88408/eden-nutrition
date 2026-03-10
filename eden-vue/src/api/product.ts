import client from './client';
import { ApiResponse, PageResult, Product } from '../types';

export const getProducts = (params: any) => {
  return client.get<any, ApiResponse<PageResult<Product>>>('/product/list', { params });
};

export const getProductDetail = (id: number) => {
  return client.get<any, ApiResponse<Product>>(`/product/${id}`);
};

export const getCategories = () => {
  return client.get<any, ApiResponse<any[]>>('/categories');
};
