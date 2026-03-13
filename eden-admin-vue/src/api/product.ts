import client from './client';
import { Product } from '../types';

export const getProducts = async (params?: { pageNum?: number; pageSize?: number }): Promise<Product[]> => {
  const res = await client.get('/product/list', { params });
  return res.list;
};

export const getProduct = async (id: number): Promise<Product> => {
  return client.get(`/product/${id}`);
};

export const createProduct = async (product: Omit<Product, 'id'>): Promise<void> => {
  const payload = {
    ...product,
    mainImage: product.imageUrl,
  };
  await client.post('/product/create', payload);
};

export const updateProduct = async (product: Product): Promise<void> => {
  const payload = {
    ...product,
    mainImage: product.imageUrl,
  };
  // Using update endpoint, assuming standard update by ID or similar
  await client.put('/product/update', payload);
};

export const deleteProduct = async (id: number): Promise<void> => {
  await client.delete(`/product/delete/${id}`);
};
