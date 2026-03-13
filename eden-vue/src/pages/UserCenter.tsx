import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { getOrders } from '../api/order';
import { Order } from '../types';
import { Package, Clock, CheckCircle } from 'lucide-react';

const UserCenter = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('orders');

  useEffect(() => {
    const fetchOrders = async () => {
      setLoading(true);
      try {
        const res = await getOrders({ pageNum: 1, pageSize: 10 });
        setOrders(res.list);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    if (activeTab === 'orders') {
      fetchOrders();
    }
  }, [activeTab]);

  return (
    <div className="flex flex-col md:flex-row gap-8">
      {/* Sidebar */}
      <aside className="w-full md:w-64 flex-shrink-0">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="p-6 border-b border-gray-100 text-center">
            <div className="h-20 w-20 rounded-full bg-emerald-100 mx-auto mb-4 flex items-center justify-center text-2xl font-bold text-emerald-600">
              {user?.username?.charAt(0).toUpperCase() || 'U'}
            </div>
            <h2 className="font-bold text-gray-900">{user?.username || '用户'}</h2>
            <p className="text-sm text-gray-500">{user?.email || 'user@example.com'}</p>
          </div>
          <nav className="p-4 space-y-1">
            <button
              onClick={() => setActiveTab('orders')}
              className={`w-full flex items-center px-4 py-2 text-sm font-medium rounded-md ${
                activeTab === 'orders' ? 'bg-emerald-50 text-emerald-700' : 'text-gray-700 hover:bg-gray-50'
              }`}
            >
              <Package className="mr-3 h-5 w-5" />
              我的订单
            </button>
            <button
              onClick={() => setActiveTab('profile')}
              className={`w-full flex items-center px-4 py-2 text-sm font-medium rounded-md ${
                activeTab === 'profile' ? 'bg-emerald-50 text-emerald-700' : 'text-gray-700 hover:bg-gray-50'
              }`}
            >
              <CheckCircle className="mr-3 h-5 w-5" />
              个人设置
            </button>
          </nav>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1">
        {activeTab === 'orders' && (
          <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900 mb-6">历史订单</h1>
            {loading ? (
              <div className="text-center py-10">加载订单中...</div>
            ) : (
              orders.map((order) => (
                <div key={order.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                  <div className="bg-gray-50 px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                    <div>
                      <p className="text-sm text-gray-500">下单时间</p>
                      <p className="font-medium text-gray-900">{new Date(order.createTime).toLocaleDateString()}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">总计</p>
                      <p className="font-medium text-gray-900">${order.totalAmount.toFixed(2)}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">订单号 #</p>
                      <p className="font-medium text-gray-900">{order.orderNo}</p>
                    </div>
                    <div className="flex items-center">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        order.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                        order.status === 'SHIPPED' ? 'bg-blue-100 text-blue-800' :
                        order.status === 'CANCELLED' ? 'bg-gray-100 text-gray-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {
                          order.status === 'COMPLETED' ? '已完成' : 
                          order.status === 'SHIPPED' ? '已发货' : 
                          order.status === 'PAID' ? '已支付' :
                          order.status === 'PENDING_PAYMENT' ? '待支付' :
                          order.status === 'CANCELLED' ? '已取消' :
                          order.status
                        }
                      </span>
                    </div>
                  </div>
                  <div className="p-6">
                    <ul className="divide-y divide-gray-200">
                      {order.items.map((item) => (
                        <li key={item.id} className="py-4 flex">
                          <div className="h-16 w-16 flex-shrink-0 overflow-hidden rounded-md border border-gray-200">
                            <img
                              src={item.productImage}
                              alt={item.productName}
                              className="h-full w-full object-cover object-center"
                              referrerPolicy="no-referrer"
                            />
                          </div>
                          <div className="ml-4 flex flex-1 flex-col justify-center">
                            <div className="flex justify-between text-base font-medium text-gray-900">
                              <h3>{item.productName}</h3>
                              <p>${item.price.toFixed(2)}</p>
                            </div>
                            <p className="mt-1 text-sm text-gray-500">数量 {item.quantity}</p>
                          </div>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {activeTab === 'profile' && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <h2 className="text-xl font-bold text-gray-900 mb-6">个人设置</h2>
            <form className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700">用户名</label>
                <input
                  type="text"
                  defaultValue={user?.username}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">电子邮箱</label>
                <input
                  type="email"
                  defaultValue={user?.email}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm"
                />
              </div>
              <button className="bg-emerald-600 text-white px-4 py-2 rounded-md hover:bg-emerald-700">
                保存更改
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserCenter;
