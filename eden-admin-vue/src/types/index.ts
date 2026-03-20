export interface Result<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface LoginDTO {
  username?: string;
  password?: string;
  phone?: string;
  code?: string;
}

export interface LoginVO {
  token: string;
  userInfo: UserInfo;
}

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  phone: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface Product {
  id: number;
  name: string;
  subtitle: string;
  detail: string;
  price: number;
  originalPrice?: number;
  stock: number;
  imageUrl: string;
  subImages?: string;
  categoryId: number;
  rating: number;
  reviewCount: number;
  status: number; // 0-下架 1-上架
  isHot: number; // 0-否 1-是
  isNew: number; // 0-否 1-是
  sales?: number;
  createTime?: string;
  updateTime?: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  price: number;
  quantity: number;
}

export interface Order {
  id: number;
  orderNo: string;
  userId: number;
  totalAmount: number;
  status: 'PENDING_PAYMENT' | 'PAID' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED';
  createTime: string;
  items: OrderItem[];
}

export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface Category {
    id: number;
    parentId: number;
    name: string;
    level: number;
    sortOrder: number;
    status: number;
}

