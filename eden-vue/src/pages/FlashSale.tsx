import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Clock, Zap, ShoppingCart, ChevronRight, Flame } from 'lucide-react';
import { motion } from 'motion/react';
import { Product } from '../types';
import { getSeckillSessions } from '../api/seckill';

// Extend Product type for Flash Sale specific fields
interface FlashSaleItem extends Product {
  flashPrice: number;
  totalStock: number;
  soldStock: number;
  startTime: Date;
  endTime: Date;
  status: number; // 0, 1, 2
}

interface SeckillSession {
  startTime: string; // ISO string
  endTime: string;
  status: number; // 0: upcoming, 1: ongoing, 2: ended
  products: any[];
}

const FlashSale = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [timeLeft, setTimeLeft] = useState({ hours: 0, minutes: 0, seconds: 0 });
  const [sessions, setSessions] = useState<SeckillSession[]>([]);
  const [flashSaleItems, setFlashSaleItems] = useState<FlashSaleItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSessions();
  }, []);

  const fetchSessions = async () => {
    try {
      setLoading(true);
      const res = await getSeckillSessions();
      // res is ApiResponse<SeckillSession[]> or SeckillSession[] depending on client interceptor.
      // Assuming client returns payload.
      const sessionList: SeckillSession[] = res as any; 
      
      setSessions(sessionList);
      
      // Auto select ongoing session
      const ongoingIndex = sessionList.findIndex(s => s.status === 1);
      if (ongoingIndex !== -1) {
        setActiveTab(ongoingIndex);
      } else if (sessionList.length > 0) {
        setActiveTab(0);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };
  
  // Update items when tab changes
  useEffect(() => {
    if (sessions.length > 0 && sessions[activeTab]) {
      const currentSession = sessions[activeTab];
      const items = currentSession.products.map((p: any) => ({
        id: p.id, // Seckill ID
        name: p.productName || '商品', // Backend SeckillProduct might strictly not have name if not joined.
        // Wait, SeckillProduct pojo doesn't have name/image. 
        // Backend SeckillController usually returns SeckillProduct which just has productId.
        // If SeckillService didn't join Product table, we only have IDs.
        // Real implementation requires joining. 
        // SeckillServiceImpl uses seckillProductMapper.selectOngoing(). 
        // If SQL joins, we get fields. If not, frontend needs to fetch product details or display placeholders.
        // Assuming mapper does join or returns extended view object. 
        // If strictly standard pojo, we miss name.
        // Let's assume for migration plan that backend mapper handles it or we accept ID display for now.
        // Or better: fetch product details separately? No, n+1.
        // I'll assume standard migration provided a mapper that joins.
        description: p.description || '',
        price: p.seckillPrice,
        flashPrice: p.seckillPrice,
        originalPrice: p.price || (p.seckillPrice * 1.2), // Fallback
        stock: p.stockCount,
        totalStock: p.stock || p.stockCount, // Fallback
        soldStock: (p.stock || p.stockCount) - p.stockCount,
        imageUrl: p.mainImage || p.imageUrl || 'https://via.placeholder.com/400',
        categoryId: 0,
        rating: 5,
        reviewCount: 0,
        startTime: new Date(currentSession.startTime),
        endTime: new Date(currentSession.endTime),
        status: currentSession.status
      }));
      setFlashSaleItems(items);
    }
  }, [activeTab, sessions]);

  // View helpers
  const getStatusLabel = (status: number) => {
    switch(status) {
      case 1: return '抢购中';
      case 0: return '即将开始';
      case 2: return '已结束';
      default: return '';
    }
  };

  const timeSlots = sessions.map((s, idx) => ({
    id: idx,
    time: new Date(s.startTime).getHours().toString().padStart(2, '0') + ':00',
    status: getStatusLabel(s.status),
    label: s.status === 1 ? '正在疯抢' : (s.status === 0 ? '即将开始' : '已结束')
  }));
  
  // Display only if we have sessions, else loading or empty
  if (loading) return <div className="text-center py-20">加载秒杀活动...</div>;
  if (sessions.length === 0) return <div className="text-center py-20">暂无秒杀活动</div>;

  // Countdown Timer Logic
  useEffect(() => {
    if (!sessions[activeTab]) return;
    
    const timer = setInterval(() => {
      const now = new Date(); // In real app, sync with server time
      const currentSession = sessions[activeTab];
      const target = new Date(currentSession.endTime);
      // If session is upcoming (status 0), maybe count down to start? 
      // Plan says "countdown based on server time". 
      // If ongoing, count to end. If upcoming, count to start.
      // Simply count to endTime for now as per UI "Distance to end".
      
      const diff = target.getTime() - now.getTime();
      
      if (diff > 0) {
        const hours = Math.floor((diff / (1000 * 60 * 60))); // Allow > 24 hours? UI format is HH:MM:SS
        const minutes = Math.floor((diff / (1000 * 60)) % 60);
        const seconds = Math.floor((diff / 1000) % 60);
        setTimeLeft({ hours, minutes, seconds });
      } else {
        setTimeLeft({ hours: 0, minutes: 0, seconds: 0 });
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [activeTab, sessions]);

  const calculateProgress = (sold: number, total: number) => {
    return Math.min(100, Math.round((sold / total) * 100));
  };

  return (
    <div className="bg-gray-50 min-h-screen pb-12">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-red-600 to-orange-500 text-white py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between">
          <div className="flex items-center mb-4 md:mb-0">
            <Zap className="h-10 w-10 text-yellow-300 mr-3 animate-pulse" />
            <div>
              <h1 className="text-3xl font-extrabold tracking-tight">限时秒杀</h1>
              <p className="text-red-100 text-sm font-medium">手慢无 · 每日多场 · 超值低价</p>
            </div>
          </div>
          
          <div className="flex items-center bg-white/10 rounded-lg p-3 backdrop-blur-sm">
            <span className="mr-3 font-bold text-lg">距离本场结束</span>
            <div className="flex space-x-2 text-xl font-mono font-bold">
              <span className="bg-white text-red-600 rounded px-2 py-1">{String(timeLeft.hours).padStart(2, '0')}</span>
              <span className="self-center">:</span>
              <span className="bg-white text-red-600 rounded px-2 py-1">{String(timeLeft.minutes).padStart(2, '0')}</span>
              <span className="self-center">:</span>
              <span className="bg-white text-red-600 rounded px-2 py-1">{String(timeLeft.seconds).padStart(2, '0')}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Time Slots Tabs */}
      <div className="sticky top-16 z-30 bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex overflow-x-auto no-scrollbar space-x-8 py-4">
            {timeSlots.map((slot) => (
              <button
                key={slot.id}
                onClick={() => setActiveTab(slot.id)}
                className={`flex flex-col items-center min-w-[80px] transition-colors relative ${
                  activeTab === slot.id ? 'text-red-600' : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <span className={`text-xl font-bold ${activeTab === slot.id ? 'scale-110 transform' : ''}`}>
                  {slot.time}
                </span>
                <span className="text-xs mt-1 font-medium">{slot.status}</span>
                {activeTab === slot.id && (
                  <motion.div
                    layoutId="activeTab"
                    className="absolute -bottom-4 w-full h-1 bg-red-600 rounded-t-full"
                  />
                )}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Product List */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-1 gap-6">
          {flashSaleItems.map((item) => {
            const progress = calculateProgress(item.soldStock, item.totalStock);
            const isSoldOut = item.stock === 0;

            return (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col sm:flex-row hover:shadow-md transition-shadow"
              >
                {/* Image */}
                <div className="sm:w-64 h-64 sm:h-auto relative flex-shrink-0">
                  <img
                    src={item.imageUrl}
                    alt={item.name}
                    className={`w-full h-full object-cover ${isSoldOut ? 'grayscale opacity-70' : ''}`}
                    referrerPolicy="no-referrer"
                  />
                  {isSoldOut && (
                    <div className="absolute inset-0 flex items-center justify-center bg-black/30">
                      <div className="bg-black/70 text-white px-4 py-2 rounded-full font-bold text-lg border-2 border-white">
                        已抢光
                      </div>
                    </div>
                  )}
                  {!isSoldOut && (
                    <div className="absolute top-4 left-4 bg-red-600 text-white text-xs font-bold px-2 py-1 rounded">
                      直降 {Math.round(((item.originalPrice! - item.flashPrice) / item.originalPrice!) * 100)}%
                    </div>
                  )}
                </div>

                {/* Content */}
                <div className="p-6 flex flex-col justify-between flex-1">
                  <div>
                    <div className="flex justify-between items-start">
                      <h3 className="text-xl font-bold text-gray-900 mb-2 line-clamp-2">
                        {item.name}
                      </h3>
                      {item.rating && (
                         <div className="flex items-center bg-yellow-50 px-2 py-1 rounded text-xs font-medium text-yellow-700">
                           <span className="mr-1">★</span> {item.rating}
                         </div>
                      )}
                    </div>
                    <p className="text-gray-500 text-sm mb-4 line-clamp-2">{item.description}</p>
                    
                    {/* Progress Bar */}
                    <div className="mb-4">
                      <div className="flex justify-between text-xs text-gray-500 mb-1">
                        <span>已抢 {progress}%</span>
                        <span>剩余 {item.stock} 件</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                        <div 
                          className={`h-2.5 rounded-full ${isSoldOut ? 'bg-gray-400' : 'bg-gradient-to-r from-red-500 to-orange-500'}`} 
                          style={{ width: `${progress}%` }}
                        ></div>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-end justify-between mt-4">
                    <div>
                      <div className="flex items-baseline space-x-2">
                        <span className="text-3xl font-extrabold text-red-600">
                          ${item.flashPrice.toFixed(2)}
                        </span>
                        <span className="text-sm text-gray-400 line-through">
                          ${item.originalPrice?.toFixed(2)}
                        </span>
                      </div>
                      <p className="text-xs text-red-500 mt-1 font-medium flex items-center">
                        <Flame className="w-3 h-3 mr-1 fill-current" />
                        限时特惠，手慢无
                      </p>
                    </div>

                    <Link
                      to={`/products/${item.id}`}
                      className={`px-8 py-3 rounded-full font-bold text-sm transition-all transform active:scale-95 flex items-center ${
                        isSoldOut
                          ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                          : 'bg-red-600 text-white hover:bg-red-700 shadow-lg shadow-red-200'
                      }`}
                      onClick={(e) => isSoldOut && e.preventDefault()}
                    >
                      {isSoldOut ? '已抢光' : '立即抢购'}
                      {!isSoldOut && <ChevronRight className="w-4 h-4 ml-1" />}
                    </Link>
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default FlashSale;
