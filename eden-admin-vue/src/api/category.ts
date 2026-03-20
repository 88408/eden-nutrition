import client from './client';
import { Category } from '../types';

export const getCategories = async (): Promise<Category[]> => {
  return client.get('/category/first');
};

export const addCategory = async (data: Partial<Category>): Promise<void> => {
  return client.post('/category/add', data);
};
