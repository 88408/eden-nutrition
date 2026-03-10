import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Order } from '../../types';

interface OrderState {
  items: Order[];
}

const initialOrders: Order[] = [
  {
    id: 1001,
    orderNo: 'ORD20231024001',
    userId: 501,
    totalAmount: 299,
    status: 'PENDING_PAYMENT',
    createTime: '2023-10-24T10:30:00Z',
    items: [
      { productId: 1, productName: '乳清蛋白粉 (香草味) 2磅', price: 299, quantity: 1 }
    ]
  },
  {
    id: 1002,
    orderNo: 'ORD20231024002',
    userId: 502,
    totalAmount: 458,
    status: 'PAID',
    createTime: '2023-10-24T11:15:00Z',
    items: [
      { productId: 1, productName: '乳清蛋白粉 (香草味) 2磅', price: 299, quantity: 1 },
      { productId: 3, productName: '深海鱼油软胶囊 90粒', price: 159, quantity: 1 }
    ]
  },
  {
    id: 1003,
    orderNo: 'ORD20231024003',
    userId: 503,
    totalAmount: 129,
    status: 'SHIPPED',
    createTime: '2023-10-24T09:00:00Z',
    items: [
      { productId: 2, productName: '复合维生素片 120粒', price: 129, quantity: 1 }
    ]
  },
  {
    id: 1004,
    orderNo: 'ORD20231023004',
    userId: 504,
    totalAmount: 178,
    status: 'COMPLETED',
    createTime: '2023-10-23T15:45:00Z',
    items: [
      { productId: 5, productName: '褪黑素睡眠软糖 60粒', price: 89, quantity: 2 }
    ]
  },
  {
    id: 1005,
    orderNo: 'ORD20231023005',
    userId: 505,
    totalAmount: 189,
    status: 'CANCELLED',
    createTime: '2023-10-23T18:20:00Z',
    items: [
      { productId: 4, productName: 'BCAA支链氨基酸 (西瓜味)', price: 189, quantity: 1 }
    ]
  }
];

const initialState: OrderState = {
  items: initialOrders,
};

const orderSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    updateOrderStatus: (state, action: PayloadAction<{ id: number; status: Order['status'] }>) => {
      const order = state.items.find(o => o.id === action.payload.id);
      if (order) {
        order.status = action.payload.status;
      }
    }
  },
});

export const { updateOrderStatus } = orderSlice.actions;
export default orderSlice.reducer;
