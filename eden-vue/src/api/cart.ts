import client from './client';
import { ApiResponse, CartItem } from '../types';

export const getCart = () => {
  return client.get<any, ApiResponse<CartItem[]>>('/cart/list');
};

export const addToCart = (data: { productId: number; quantity: number }) => {
  return client.post<any, ApiResponse<any>>('/cart/add', data);
};

export const updateCartItem = (data: { productId: number; quantity: number }) => {
  return client.put<any, ApiResponse<any>>('/cart/update', data);
};

export const removeCartItem = (productId: number) => {
  return client.delete<any, ApiResponse<any>>(`/cart/delete?productId=${productId}`);
  // Or /cart/delete/${productId} depending on backend.
  // Plan says DELETE /cart/delete. Usually implies /cart/delete?productId=... or similar or body.
  // Standard Spring Boot might use @DeleteMapping("/delete") public void delete(@RequestParam Long productId).
};

export const clearCart = () => {
  return client.delete<any, ApiResponse<any>>('/cart/clear'); // Not in plan but common
};
