import client from './client';
// Need SeckillProduct type? define it or use any for now
import { Product } from '../types';

export interface SeckillProduct {
  id?: number;
  productId: number;
  productName?: string; // transient
  productImage?: string; // transient
  seckillPrice: number;
  stockCount: number; // seckill stock
  startTime: string;
  endTime: string;
  status?: number; // 0,1,2
}

export const getSeckillList = async (): Promise<SeckillProduct[]> => {
  return client.get('/seckill/list');
};

export const createSeckill = async (data: SeckillProduct): Promise<void> => {
  await client.post('/seckill/create', data);
};

export const updateSeckill = async (data: SeckillProduct): Promise<void> => {
  await client.put('/seckill/update', data);
};

export const publishSeckill = async (id: number): Promise<void> => {
  await client.post(`/seckill/publish/${id}`);
};
