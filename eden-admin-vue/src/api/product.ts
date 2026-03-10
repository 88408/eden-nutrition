import { Product } from '../types';

const BASE_URL = 'http://localhost:8080/product';

export const getProducts = async (): Promise<Product[]> => {
  const response = await fetch(`${BASE_URL}/list`);
  if (!response.ok) throw new Error('Failed to fetch products');
  const result = await response.json();
  // Backend returns PageVO<ProductVO> in data.
  // PageVO has list field.
  return result.data.list;
};

export const getProduct = async (id: number): Promise<Product> => {
  const response = await fetch(`${BASE_URL}/${id}`);
  if (!response.ok) throw new Error('Failed to fetch product');
  const result = await response.json();
  return result.data;
};

export const createProduct = async (product: Omit<Product, 'id'>): Promise<void> => {
  // Map frontend Product (VO-like) to backend Product entity
  const payload = {
    ...product,
    mainImage: product.imageUrl,
  };

  const response = await fetch(`${BASE_URL}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  
  if (!response.ok) {
     throw new Error('Network error');
  }
  
  const result = await response.json();
  if (result.code && result.code !== 200) { // Assuming result.code exists if success=true wrapper
     throw new Error(result.message || 'Create failed');
  }
};

export const updateProduct = async (product: Product): Promise<void> => {
  const payload = {
    ...product,
    mainImage: product.imageUrl,
  };

  const response = await fetch(`${BASE_URL}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
     throw new Error('Network error');
  }

  const result = await response.json();
  if (result.code && result.code !== 200) {
     throw new Error(result.message || 'Update failed');
  }
};

export const deleteProduct = async (id: number): Promise<void> => {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  });
  
  if (!response.ok) {
     throw new Error('Network error');
  }

  const result = await response.json();
  if (result.code && result.code !== 200) {
     throw new Error(result.message || 'Delete failed');
  }
};
