import client from './client';
import { Order } from '../types';

const mapStatus = (status: number): string => {
    switch (status) {
        case 0: return 'PENDING_PAYMENT';
        case 1: return 'PAID';
        case 2: return 'SHIPPED';
        case 3: return 'COMPLETED';
        case 4: return 'CANCELLED';
        case 5: return 'REFUNDED';
        default: return 'UNKNOWN';
    }
};

export const getOrders = async (pageNum = 1, pageSize = 10, orderNo?: string, status?: number): Promise<{ list: Order[], total: number }> => {
    const params: any = { pageNum, pageSize };
    if (orderNo) params.orderNo = orderNo;
    if (status !== undefined) params.status = status;

    // Backend returns PageResult<OrderVO>
    const res = await client.get('/order/admin/list', { params });
    
    // Check if the structure matches what we expect. Assuming res is the data payload from client.ts
    // res should be { list: [...], total: ... }
    const mappedList = res.list.map((item: any) => ({
        id: item.id,
        orderNo: item.orderNo,
        userId: item.userId,
        // The VO might be order.payAmount or similar. Adjust based on backend API.
        totalAmount: item.payAmount || item.totalAmount, 
        status: mapStatus(item.status),
        createTime: item.createTime,
        items: item.orderItems ? item.orderItems.map((oi: any) => ({
             productId: oi.productId,
             productName: oi.productName,
             price: oi.price,
             quantity: oi.quantity
        })) : []
    }));
    return { list: mappedList, total: res.total };
};

export const shipOrder = async (orderNo: string): Promise<void> => {
    await client.post(`/order/admin/ship/${orderNo}`);
};
