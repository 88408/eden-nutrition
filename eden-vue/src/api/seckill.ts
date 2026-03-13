import client from './client';
import { ApiResponse, SeckillSession } from '../types';

// Assuming SeckillSession is defined in types or I need to define it locally if types file is not available
// The response from backend is List<SeckillSessionDTO> which contains product list.

export const getSeckillSessions = () => {
    // Backend returns list of sessions with products
    return client.get<any, ApiResponse<SeckillSession[]>>('/seckill/sessions');
};
