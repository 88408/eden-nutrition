import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Clock, Zap, ShoppingCart, ChevronRight, Flame } from 'lucide-react';
import { motion } from 'motion/react';
import { Product } from '../types';

// Extend Product type for Flash Sale specific fields
interface FlashSaleItem extends Product {
  flashPrice: number;
  totalStock: number;
  soldStock: number;
  startTime: Date;
  endTime: Date;
}

const FlashSale = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [timeLeft, setTimeLeft] = useState({ hours: 0, minutes: 0, seconds: 0 });

  // Mock time slots
  const timeSlots = [
    { id: 0, time: '10:00', status: '抢购中', label: '正在疯抢' },
    { id: 1, time: '14:00', status: '即将开始', label: '即将开始' },
    { id: 2, time: '20:00', status: '即将开始', label: '即将开始' },
    { id: 3, time: '22:00', status: '即将开始', label: '即将开始' },
  ];

  // Mock Flash Sale Data
  const flashSaleItems: FlashSaleItem[] = [
    {
      id: 101,
      name: '高性能乳清蛋白粉 (巧克力味)',
      description: '快速吸收，肌肉恢复首选。',
      price: 49.99, // Original Price
      flashPrice: 29.99,
      originalPrice: 49.99,
      stock: 20, // Current available stock
      totalStock: 100,
      soldStock: 80,
      imageUrl: 'https://picsum.photos/seed/whey-choco/400/400',
      categoryId: 1,
      rating: 4.9,
      reviewCount: 320,
      startTime: new Date(),
      endTime: new Date(new Date().getTime() + 3600000),
    },
    {
      id: 102,
      name: '强效氮泵 (蓝莓味)',
      description: '引爆训练能量，专注力提升。',
      price: 39.99,
      flashPrice: 19.99,
      originalPrice: 39.99,
      stock: 5,
      totalStock: 50,
      soldStock: 45,
      imageUrl: 'https://picsum.photos/seed/pre-workout/400/400',
      categoryId: 1,
      rating: 4.7,
      reviewCount: 150,
      startTime: new Date(),
      endTime: new Date(new Date().getTime() + 3600000),
    },
    {
      id: 103,
      name: '综合维生素矿物质片',
      description: '每日一片，全面营养支持。',
      price: 24.99,
      flashPrice: 9.99,
      originalPrice: 24.99,
      stock: 150,
      totalStock: 200,
      soldStock: 50,
      imageUrl: 'https://picsum.photos/seed/multi-vit/400/400',
      categoryId: 2,
      rating: 4.8,
      reviewCount: 500,
      startTime: new Date(),
      endTime: new Date(new Date().getTime() + 3600000),
    },
    {
      id: 104,
      name: '肌酸一水合物',
      description: '提升爆发力，增加肌肉围度。',
      price: 29.99,
      flashPrice: 14.99,
      originalPrice: 29.99,
      stock: 0, // Sold out
      totalStock: 80,
      soldStock: 80,
      imageUrl: 'https://picsum.photos/seed/creatine/400/400',
      categoryId: 1,
      rating: 4.6,
      reviewCount: 210,
      startTime: new Date(),
      endTime: new Date(new Date().getTime() + 3600000),
    },
  ];

  // Countdown Timer Logic
  useEffect(() => {
    const timer = setInterval(() => {
      const now = new Date();
      // Calculate time until next slot or end of current slot
      // For demo, just counting down to a fixed time (e.g., end of the hour)
      const target = new Date();
      target.setHours(target.getHours() + 1);
      target.setMinutes(0);
      target.setSeconds(0);
      
      const diff = target.getTime() - now.getTime();
      
      if (diff > 0) {
        const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
        const minutes = Math.floor((diff / (1000 * 60)) % 60);
        const seconds = Math.floor((diff / 1000) % 60);
        setTimeLeft({ hours, minutes, seconds });
      }
    }, 1000);

    return () => clearInterval(timer);
  }, []);

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
