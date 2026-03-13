import client from './client';
import { Category } from '../types';

export const getCategories = async (): Promise<Category[]> => {
  return client.get('/category/first');
};
