import client from './client';
import { ApiResponse, User } from '../types';

export const login = (data: any) => {
  return client.post<any, ApiResponse<{ token: string; user: User }>>('/auth/login', data);
};

export const register = (data: any) => {
  return client.post<any, ApiResponse<any>>('/auth/register', data);
};

export const getUserInfo = () => {
  return client.get<any, ApiResponse<User>>('/user/info');
};

export const updateUserInfo = (data: Partial<User>) => {
  return client.put<any, ApiResponse<User>>('/user/info', data);
};
