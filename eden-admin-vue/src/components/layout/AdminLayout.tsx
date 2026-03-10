import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Package, 
  ShoppingCart, 
  Zap, 
  Users, 
  Settings, 
  LogOut,
  Bell,
  Search
} from 'lucide-react';
import { Toaster } from 'react-hot-toast';

const Sidebar = () => {
  const navItems = [
    { path: '/admin/dashboard', icon: LayoutDashboard, label: '数据看板' },
    { path: '/admin/products', icon: Package, label: '商品管理' },
    { path: '/admin/orders', icon: ShoppingCart, label: '订单管理' },
    { path: '/admin/flash-sales', icon: Zap, label: '秒杀活动' },
    { path: '/admin/users', icon: Users, label: '用户管理' },
    { path: '/admin/settings', icon: Settings, label: '系统设置' },
  ];

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col h-full">
      <div className="h-16 flex items-center px-6 border-b border-gray-200">
        <div className="flex items-center gap-2 text-emerald-600 font-bold text-xl">
          <Zap className="w-6 h-6 fill-current" />
          <span>Eden Admin</span>
        </div>
      </div>
      
      <div className="flex-1 py-6 px-4 space-y-1 overflow-y-auto">
        <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4 px-2">
          主菜单
        </div>
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                isActive 
                  ? 'bg-emerald-50 text-emerald-700 font-medium' 
                  : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
              }`
            }
          >
            <item.icon className="w-5 h-5" />
            {item.label}
          </NavLink>
        ))}
      </div>
      
      <div className="p-4 border-t border-gray-200">
        <button className="flex items-center gap-3 px-3 py-2.5 w-full rounded-lg text-gray-600 hover:bg-red-50 hover:text-red-600 transition-colors">
          <LogOut className="w-5 h-5" />
          <span>退出登录</span>
        </button>
      </div>
    </aside>
  );
};

const Header = () => {
  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-8 sticky top-0 z-10">
      <div className="flex items-center bg-gray-100 rounded-lg px-3 py-2 w-96">
        <Search className="w-4 h-4 text-gray-400 mr-2" />
        <input 
          type="text" 
          placeholder="搜索订单、商品或用户..." 
          className="bg-transparent border-none outline-none text-sm w-full text-gray-700 placeholder-gray-400"
        />
      </div>
      
      <div className="flex items-center gap-6">
        <button className="relative text-gray-500 hover:text-gray-700 transition-colors">
          <Bell className="w-5 h-5" />
          <span className="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"></span>
        </button>
        
        <div className="flex items-center gap-3 border-l border-gray-200 pl-6">
          <div className="text-right hidden md:block">
            <div className="text-sm font-medium text-gray-900">管理员</div>
            <div className="text-xs text-gray-500">admin@eden.com</div>
          </div>
          <img 
            src="https://picsum.photos/seed/admin/100/100" 
            alt="Admin Avatar" 
            className="w-9 h-9 rounded-full border border-gray-200 object-cover"
          />
        </div>
      </div>
    </header>
  );
};

export default function AdminLayout() {
  return (
    <div className="flex h-screen bg-gray-50 font-sans text-gray-900 overflow-hidden">
      <Sidebar />
      <div className="flex-1 flex flex-col h-full overflow-hidden">
        <Header />
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
      <Toaster position="top-right" />
    </div>
  );
}
