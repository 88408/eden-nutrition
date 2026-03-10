import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { ShoppingCart, User, Search, Menu, X } from 'lucide-react';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../store';
import { logout } from '../store/authSlice';

const MainLayout = () => {
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
  const { totalQuantity } = useSelector((state: RootState) => state.cart);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex-shrink-0 flex items-center">
              <Link to="/" className="text-2xl font-bold text-emerald-600">
                Eden Nutrition
              </Link>
            </div>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex space-x-8">
              <Link to="/" className="text-gray-700 hover:text-emerald-600 px-3 py-2 rounded-md text-sm font-medium">
                首页
              </Link>
              <Link to="/products" className="text-gray-700 hover:text-emerald-600 px-3 py-2 rounded-md text-sm font-medium">
                商品列表
              </Link>
              <Link to="/flash-sale" className="text-red-600 hover:text-red-700 px-3 py-2 rounded-md text-sm font-medium flex items-center">
                <span className="mr-1">⚡</span> 秒杀专区
              </Link>
              <Link to="/about" className="text-gray-700 hover:text-emerald-600 px-3 py-2 rounded-md text-sm font-medium">
                关于我们
              </Link>
            </nav>

            {/* Search & Actions */}
            <div className="hidden md:flex items-center space-x-4">
              <div className="relative">
                <input
                  type="text"
                  placeholder="搜索商品..."
                  className="bg-gray-100 text-gray-800 rounded-full pl-10 pr-4 py-2 focus:outline-none focus:ring-2 focus:ring-emerald-500 w-64"
                />
                <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
              </div>

              <Link to="/cart" className="relative p-2 text-gray-600 hover:text-emerald-600">
                <ShoppingCart className="h-6 w-6" />
                {totalQuantity > 0 && (
                  <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-500 rounded-full">
                    {totalQuantity}
                  </span>
                )}
              </Link>

              {isAuthenticated ? (
                <div className="relative group">
                  <button className="flex items-center space-x-2 text-gray-700 hover:text-emerald-600 focus:outline-none">
                    <User className="h-6 w-6" />
                    <span className="text-sm font-medium">{user?.username}</span>
                  </button>
                  {/* Dropdown */}
                  <div className="absolute right-0 w-48 mt-2 origin-top-right bg-white border border-gray-200 divide-y divide-gray-100 rounded-md shadow-lg outline-none hidden group-hover:block">
                    <div className="py-1">
                      <Link to="/user" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                        个人中心
                      </Link>
                      <Link to="/orders" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                        我的订单
                      </Link>
                      <button
                        onClick={handleLogout}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        退出登录
                      </button>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="flex space-x-2">
                  <Link to="/login" className="text-gray-700 hover:text-emerald-600 px-3 py-2 rounded-md text-sm font-medium">
                    登录
                  </Link>
                  <Link
                    to="/register"
                    className="bg-emerald-600 text-white hover:bg-emerald-700 px-4 py-2 rounded-md text-sm font-medium"
                  >
                    注册
                  </Link>
                </div>
              )}
            </div>

            {/* Mobile menu button */}
            <div className="md:hidden flex items-center">
              <button
                onClick={() => setIsMenuOpen(!isMenuOpen)}
                className="text-gray-700 hover:text-emerald-600 focus:outline-none"
              >
                {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMenuOpen && (
          <div className="md:hidden bg-white border-t border-gray-200">
            <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
              <Link to="/" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50">
                首页
              </Link>
              <Link to="/products" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50">
                商品列表
              </Link>
              <Link to="/flash-sale" className="block px-3 py-2 rounded-md text-base font-medium text-red-600 hover:text-red-700 hover:bg-red-50">
                ⚡ 秒杀专区
              </Link>
              <Link to="/cart" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50">
                购物车 ({totalQuantity})
              </Link>
              {isAuthenticated ? (
                <>
                  <Link to="/user" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50">
                    个人中心
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50"
                  >
                    退出登录
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50">
                    登录
                  </Link>
                  <Link to="/register" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-emerald-600 hover:bg-gray-50">
                    注册
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </header>

      {/* Main Content */}
      <main className="flex-grow max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Eden Nutrition</h3>
              <p className="text-gray-500 text-sm">
                提供高品质的营养补充剂，助您拥有更健康的生活。
              </p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-gray-900 tracking-wider uppercase mb-4">购物</h3>
              <ul className="space-y-2">
                <li><Link to="/products" className="text-gray-500 hover:text-emerald-600 text-sm">所有商品</Link></li>
                <li><Link to="/category/vitamins" className="text-gray-500 hover:text-emerald-600 text-sm">维生素</Link></li>
                <li><Link to="/category/protein" className="text-gray-500 hover:text-emerald-600 text-sm">蛋白粉</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-gray-900 tracking-wider uppercase mb-4">支持</h3>
              <ul className="space-y-2">
                <li><Link to="/contact" className="text-gray-500 hover:text-emerald-600 text-sm">联系我们</Link></li>
                <li><Link to="/faq" className="text-gray-500 hover:text-emerald-600 text-sm">常见问题</Link></li>
                <li><Link to="/shipping" className="text-gray-500 hover:text-emerald-600 text-sm">配送信息</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-gray-900 tracking-wider uppercase mb-4">订阅</h3>
              <p className="text-gray-500 text-sm mb-4">订阅我们的通讯，获取特别优惠和最新资讯。</p>
              <div className="flex">
                <input
                  type="email"
                  placeholder="输入您的邮箱"
                  className="flex-1 min-w-0 px-4 py-2 border border-gray-300 rounded-l-md focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm"
                />
                <button className="bg-emerald-600 text-white px-4 py-2 rounded-r-md hover:bg-emerald-700 text-sm font-medium">
                  订阅
                </button>
              </div>
            </div>
          </div>
          <div className="mt-8 border-t border-gray-200 pt-8 text-center">
            <p className="text-gray-400 text-sm">&copy; 2024 Eden Nutrition. 保留所有权利。</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
