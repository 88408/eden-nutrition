import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AdminLayout from './components/layout/AdminLayout';
import Dashboard from './pages/Dashboard';
import ProductManagement from './pages/ProductManagement';
import OrderManagement from './pages/OrderManagement';
import FlashSale from './pages/Marketing/FlashSale';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="products" element={<ProductManagement />} />
          <Route path="orders" element={<OrderManagement />} />
          <Route path="flash-sales" element={<FlashSale />} />
          <Route path="users" element={<div className="p-8 text-center text-gray-500">用户管理开发中...</div>} />
          <Route path="settings" element={<div className="p-8 text-center text-gray-500">系统设置开发中...</div>} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
