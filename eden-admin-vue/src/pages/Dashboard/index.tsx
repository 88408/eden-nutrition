import React from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '../../store';
import { 
  DollarSign, 
  ShoppingCart, 
  Users, 
  AlertTriangle,
  ArrowUpRight,
  ArrowDownRight
} from 'lucide-react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer 
} from 'recharts';

const salesData = [
  { name: '10-18', sales: 4000 },
  { name: '10-19', sales: 3000 },
  { name: '10-20', sales: 2000 },
  { name: '10-21', sales: 2780 },
  { name: '10-22', sales: 1890 },
  { name: '10-23', sales: 2390 },
  { name: '10-24', sales: 3490 },
];

const MetricCard = ({ title, value, icon: Icon, trend, trendValue, colorClass }: any) => (
  <div className="bg-white rounded-xl p-6 border border-gray-100 shadow-sm flex flex-col gap-4">
    <div className="flex items-center justify-between">
      <div className="text-sm font-medium text-gray-500">{title}</div>
      <div className={`p-2 rounded-lg ${colorClass} bg-opacity-10`}>
        <Icon className={`w-5 h-5 ${colorClass.replace('bg-', 'text-')}`} />
      </div>
    </div>
    <div className="text-3xl font-bold tracking-tight text-gray-900">{value}</div>
    <div className="flex items-center text-sm">
      {trend === 'up' ? (
        <ArrowUpRight className="w-4 h-4 text-emerald-500 mr-1" />
      ) : trend === 'down' ? (
        <ArrowDownRight className="w-4 h-4 text-red-500 mr-1" />
      ) : null}
      <span className={trend === 'up' ? 'text-emerald-600 font-medium' : trend === 'down' ? 'text-red-600 font-medium' : 'text-gray-500'}>
        {trendValue}
      </span>
      <span className="text-gray-400 ml-2">较昨日</span>
    </div>
  </div>
);

export default function Dashboard() {
  const products = useSelector((state: RootState) => state.products.items);
  const orders = useSelector((state: RootState) => state.orders.items);
  
  const lowStockCount = products.filter(p => p.stock < 10).length;
  const todayOrders = orders.filter(o => o.createTime.startsWith('2023-10-24'));
  const todaySales = todayOrders.reduce((sum, order) => sum + order.totalAmount, 0);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">数据看板</h1>
        <p className="text-sm text-gray-500 mt-1">欢迎回来，这是您的今日业务概览。</p>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard 
          title="今日销售额" 
          value={`¥${todaySales.toLocaleString()}`} 
          icon={DollarSign} 
          trend="up" 
          trendValue="+12.5%" 
          colorClass="bg-emerald-500" 
        />
        <MetricCard 
          title="今日订单数" 
          value={todayOrders.length} 
          icon={ShoppingCart} 
          trend="up" 
          trendValue="+5.2%" 
          colorClass="bg-blue-500" 
        />
        <MetricCard 
          title="总用户数" 
          value="1,245" 
          icon={Users} 
          trend="up" 
          trendValue="+1.2%" 
          colorClass="bg-purple-500" 
        />
        <MetricCard 
          title="低库存预警" 
          value={lowStockCount} 
          icon={AlertTriangle} 
          trend="down" 
          trendValue="-2" 
          colorClass="bg-orange-500" 
        />
      </div>

      {/* Charts and Tables */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sales Chart */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-gray-100 shadow-sm p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">销售趋势 (近7天)</h2>
            <select className="text-sm border-gray-200 rounded-md text-gray-600 focus:ring-emerald-500 focus:border-emerald-500">
              <option>本周</option>
              <option>本月</option>
              <option>全年</option>
            </select>
          </div>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={salesData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12 }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12 }} />
                <Tooltip 
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                  itemStyle={{ color: '#10b981', fontWeight: 500 }}
                />
                <Area type="monotone" dataKey="sales" stroke="#10b981" strokeWidth={3} fillOpacity={1} fill="url(#colorSales)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Latest Orders */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6 flex flex-col">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-semibold text-gray-900">最新订单</h2>
            <button className="text-sm text-emerald-600 hover:text-emerald-700 font-medium">查看全部</button>
          </div>
          <div className="flex-1 overflow-y-auto pr-2 space-y-4">
            {orders.slice(0, 5).map(order => (
              <div key={order.id} className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors border border-transparent hover:border-gray-100">
                <div>
                  <div className="text-sm font-medium text-gray-900">{order.orderNo}</div>
                  <div className="text-xs text-gray-500 mt-1">{new Date(order.createTime).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</div>
                </div>
                <div className="text-right">
                  <div className="text-sm font-bold text-gray-900">¥{order.totalAmount}</div>
                  <div className={`text-xs mt-1 font-medium px-2 py-0.5 rounded-full inline-block ${
                    order.status === 'PENDING_PAYMENT' ? 'bg-orange-100 text-orange-700' :
                    order.status === 'PAID' ? 'bg-blue-100 text-blue-700' :
                    order.status === 'SHIPPED' ? 'bg-emerald-100 text-emerald-700' :
                    order.status === 'COMPLETED' ? 'bg-gray-100 text-gray-700' :
                    'bg-red-100 text-red-700'
                  }`}>
                    {order.status === 'PENDING_PAYMENT' ? '待付款' :
                     order.status === 'PAID' ? '已付款' :
                     order.status === 'SHIPPED' ? '已发货' :
                     order.status === 'COMPLETED' ? '已完成' : '已取消'}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
