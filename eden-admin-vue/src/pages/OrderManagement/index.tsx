import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../../store';
import { updateOrderStatus } from '../../store/slices/orderSlice';
import { Order } from '../../types';
import { 
  Search, 
  Filter, 
  Eye, 
  Truck, 
  X,
  Package
} from 'lucide-react';
import toast from 'react-hot-toast';

const statusMap = {
  'ALL': { label: '全部订单', color: 'bg-gray-100 text-gray-800' },
  'PENDING_PAYMENT': { label: '待付款', color: 'bg-orange-100 text-orange-700' },
  'PAID': { label: '已付款', color: 'bg-blue-100 text-blue-700' },
  'SHIPPED': { label: '已发货', color: 'bg-emerald-100 text-emerald-700' },
  'COMPLETED': { label: '已完成', color: 'bg-gray-100 text-gray-700' },
  'CANCELLED': { label: '已取消', color: 'bg-red-100 text-red-700' },
};

export default function OrderManagement() {
  const dispatch = useDispatch();
  const orders = useSelector((state: RootState) => state.orders.items);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

  const filteredOrders = orders.filter(o => {
    const matchesSearch = o.orderNo.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = selectedStatus === 'ALL' || o.status === selectedStatus;
    return matchesSearch && matchesStatus;
  });

  const handleShipOrder = (id: number) => {
    if (window.confirm('确认该订单已发货吗？')) {
      dispatch(updateOrderStatus({ id, status: 'SHIPPED' }));
      toast.success('订单已标记为发货');
    }
  };

  const handleViewDetails = (order: Order) => {
    setSelectedOrder(order);
    setIsDetailModalOpen(true);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">订单管理</h1>
        <p className="text-sm text-gray-500 mt-1">处理客户订单，跟踪发货状态。</p>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input 
            type="text" 
            placeholder="搜索订单号..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none transition-all"
          />
        </div>
        <div className="flex bg-gray-100 p-1 rounded-lg overflow-x-auto">
          {Object.entries(statusMap).map(([key, { label }]) => (
            <button
              key={key}
              onClick={() => setSelectedStatus(key)}
              className={`px-4 py-1.5 text-sm font-medium rounded-md whitespace-nowrap transition-colors ${
                selectedStatus === key 
                  ? 'bg-white text-emerald-700 shadow-sm' 
                  : 'text-gray-600 hover:text-gray-900 hover:bg-gray-200'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200 text-gray-500 text-sm uppercase tracking-wider">
                <th className="px-6 py-4 font-medium">订单号/时间</th>
                <th className="px-6 py-4 font-medium">用户ID</th>
                <th className="px-6 py-4 font-medium">金额</th>
                <th className="px-6 py-4 font-medium">状态</th>
                <th className="px-6 py-4 font-medium text-right">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredOrders.map(order => (
                <tr key={order.id} className="hover:bg-gray-50 transition-colors group">
                  <td className="px-6 py-4">
                    <div className="font-medium text-gray-900">{order.orderNo}</div>
                    <div className="text-xs text-gray-500 mt-1">
                      {new Date(order.createTime).toLocaleString('zh-CN')}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-gray-600">
                    #{order.userId}
                  </td>
                  <td className="px-6 py-4">
                    <div className="font-bold text-gray-900">¥{order.totalAmount.toFixed(2)}</div>
                    <div className="text-xs text-gray-500 mt-1">共 {order.items.reduce((sum, item) => sum + item.quantity, 0)} 件商品</div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${statusMap[order.status as keyof typeof statusMap].color}`}>
                      {statusMap[order.status as keyof typeof statusMap].label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button 
                        onClick={() => handleViewDetails(order)}
                        className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="查看详情"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      {order.status === 'PAID' && (
                        <button 
                          onClick={() => handleShipOrder(order.id)}
                          className="p-2 text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors flex items-center gap-1 text-sm font-medium"
                          title="发货"
                        >
                          <Truck className="w-4 h-4" />
                          <span className="hidden sm:inline">发货</span>
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filteredOrders.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                    没有找到匹配的订单
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Detail Modal */}
      {isDetailModalOpen && selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50">
              <div className="flex items-center gap-3">
                <h3 className="text-lg font-bold text-gray-900">订单详情</h3>
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusMap[selectedOrder.status as keyof typeof statusMap].color}`}>
                  {statusMap[selectedOrder.status as keyof typeof statusMap].label}
                </span>
              </div>
              <button 
                onClick={() => setIsDetailModalOpen(false)}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200 rounded-full transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="p-6 overflow-y-auto flex-1 space-y-6">
              <div className="grid grid-cols-2 gap-6 p-4 bg-gray-50 rounded-xl border border-gray-100">
                <div>
                  <div className="text-xs text-gray-500 mb-1 uppercase tracking-wider">订单编号</div>
                  <div className="font-medium text-gray-900">{selectedOrder.orderNo}</div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1 uppercase tracking-wider">下单时间</div>
                  <div className="font-medium text-gray-900">{new Date(selectedOrder.createTime).toLocaleString('zh-CN')}</div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1 uppercase tracking-wider">用户 ID</div>
                  <div className="font-medium text-gray-900">#{selectedOrder.userId}</div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1 uppercase tracking-wider">订单总额</div>
                  <div className="font-bold text-emerald-600 text-lg">¥{selectedOrder.totalAmount.toFixed(2)}</div>
                </div>
              </div>

              <div>
                <h4 className="text-sm font-bold text-gray-900 mb-4 flex items-center gap-2">
                  <Package className="w-4 h-4 text-gray-400" />
                  商品清单
                </h4>
                <div className="border border-gray-200 rounded-xl overflow-hidden">
                  <table className="w-full text-left text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                      <tr>
                        <th className="px-4 py-3 font-medium text-gray-500">商品名称</th>
                        <th className="px-4 py-3 font-medium text-gray-500 text-right">单价</th>
                        <th className="px-4 py-3 font-medium text-gray-500 text-right">数量</th>
                        <th className="px-4 py-3 font-medium text-gray-500 text-right">小计</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {selectedOrder.items.map((item, idx) => (
                        <tr key={idx} className="hover:bg-gray-50">
                          <td className="px-4 py-3 text-gray-900">{item.productName}</td>
                          <td className="px-4 py-3 text-right text-gray-600">¥{item.price.toFixed(2)}</td>
                          <td className="px-4 py-3 text-right text-gray-600">x{item.quantity}</td>
                          <td className="px-4 py-3 text-right font-medium text-gray-900">¥{(item.price * item.quantity).toFixed(2)}</td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot className="bg-gray-50 border-t border-gray-200">
                      <tr>
                        <td colSpan={3} className="px-4 py-3 text-right font-medium text-gray-500">合计：</td>
                        <td className="px-4 py-3 text-right font-bold text-gray-900">¥{selectedOrder.totalAmount.toFixed(2)}</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
            
            <div className="px-6 py-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
              <button 
                onClick={() => setIsDetailModalOpen(false)}
                className="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg font-medium hover:bg-gray-50 transition-colors"
              >
                关闭
              </button>
              {selectedOrder.status === 'PAID' && (
                <button 
                  onClick={() => {
                    handleShipOrder(selectedOrder.id);
                    setIsDetailModalOpen(false);
                  }}
                  className="px-4 py-2 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 transition-colors shadow-sm flex items-center gap-2"
                >
                  <Truck className="w-4 h-4" />
                  确认发货
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
