import client from './client';
import { ApiResponse, CartItem } from '../types';

export const getCart = () => {
  return client.get<any, ApiResponse<CartItem[]>>('/cart');
};

export const addToCart = (data: { productId: number; quantity: number }) => {
  return client.post<any, ApiResponse<any>>('/cart', data);
};

export const updateCartItem = (data: { productId: number; quantity: number }) => {
  return client.put<any, ApiResponse<any>>('/cart', data);
};

export const removeCartItem = (productId: number) => {
  return client.delete<any, ApiResponse<any>>(`/cart/${productId}`);
};

export const clearCart = () => {
  return client.delete<any, ApiResponse<any>>('/cart');
};
