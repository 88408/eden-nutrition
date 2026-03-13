import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Order } from '../../types';
import { getOrders, shipOrder as apiShipOrder } from '../../api/order';

interface OrderState {
  items: Order[];
  status: 'idle' | 'loading' | 'succeeded' | 'failed';
  error: string | null;
  total: number;
}

const initialState: OrderState = {
  items: [],
  status: 'idle',
  error: null,
  total: 0
};

export const fetchOrders = createAsyncThunk(
  'orders/fetchOrders',
  async (params: { pageNum?: number; pageSize?: number; orderNo?: string; status?: number } | undefined) => {
    const { pageNum, pageSize, orderNo, status } = params || {};
    const response = await getOrders(pageNum, pageSize, orderNo, status);
    return response;
  }
);

export const shipOrder = createAsyncThunk(
  'orders/shipOrder',
  async (orderNo: string) => {
    await apiShipOrder(orderNo);
    return orderNo;
  }
);

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
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrders.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload.list;
        state.total = action.payload.total;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || 'Failed to fetch orders';
      })
      .addCase(shipOrder.fulfilled, (state, action) => {
         const orderIndex = state.items.findIndex(o => o.orderNo === action.payload);
         if (orderIndex !== -1) {
             state.items[orderIndex].status = 'SHIPPED';
         }
      });
  },
});

export const { updateOrderStatus } = orderSlice.actions;
export default orderSlice.reducer;
