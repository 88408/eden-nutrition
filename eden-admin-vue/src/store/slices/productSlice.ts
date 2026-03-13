import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Product } from '../../types';
import * as productApi from '../../api/product';

interface ProductState {
  items: Product[];
  status: 'idle' | 'loading' | 'succeeded' | 'failed';
  error: string | null;
}

const initialState: ProductState = {
  items: [],
  status: 'idle',
  error: null,
};

export const fetchProducts = createAsyncThunk('products/fetchProducts', async (params?: { pageNum?: number; pageSize?: number }) => {
  // Default to fetching a large number for admin list view without proper pagination UI yet
  const effectiveParams = { pageSize: 100, ...params };
  return await productApi.getProducts(effectiveParams);
});

export const addNewProduct = createAsyncThunk('products/addNewProduct', async (initialProduct: Omit<Product, 'id'>) => {
  // Backend returns void, so we fetch all products again
  await productApi.createProduct(initialProduct);
  return await productApi.getProducts({ pageSize: 100 });
});

export const updateExistingProduct = createAsyncThunk('products/updateProduct', async (product: Product) => {
  await productApi.updateProduct(product);
  return product;
});

export const removeProduct = createAsyncThunk('products/deleteProduct', async (id: number) => {
  await productApi.deleteProduct(id);
  return id;
});

const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {
    // Keep internal reducers if needed, but async thunks handle most logic now
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => {
        state.status = 'loading';
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || 'Something went wrong';
      })
      .addCase(addNewProduct.fulfilled, (state, action) => {
        // Since we refetch on add
        state.items = action.payload;
      })
      .addCase(updateExistingProduct.fulfilled, (state, action) => {
        const index = state.items.findIndex(product => product.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
      })
      .addCase(removeProduct.fulfilled, (state, action) => {
        state.items = state.items.filter(product => product.id !== action.payload);
      });
  },
});

export default productSlice.reducer;

