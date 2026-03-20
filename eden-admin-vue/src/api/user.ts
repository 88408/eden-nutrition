import client from './client';
import { LoginDTO, LoginVO, Result } from '../types';

/**
 * Login user
 */
export const login = async (data: LoginDTO): Promise<LoginVO> => {
  return client.post('/user/login', data);
};
