import client from './client';
import { Product } from '../types';

export const getProducts = async (params?: { pageNum?: number; pageSize?: number; keyword?: string; categoryId?: number; status?: number }): Promise<{ list: Product[], total: number }> => {
  return await client.get('/admin/product/list', { params });
};

export const getProduct = async (id: number): Promise<Product> => {
  return client.get(`/admin/product/${id}`);
};

export const createProduct = async (product: any): Promise<void> => {
  await client.post('/admin/product', product);
};

export const updateProduct = async (product: any): Promise<void> => {
  await client.put('/admin/product', product);
};

export const updateProductStatus = async (id: number, status: number): Promise<void> => {
  await client.put(`/admin/product/status/${id}/${status}`);
};

export const deleteProduct = async (id: number): Promise<void> => {
  await client.delete(`/admin/product/${id}`);
};