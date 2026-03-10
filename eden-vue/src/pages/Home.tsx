import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Star } from 'lucide-react';
import { motion } from 'motion/react';
import { getProducts } from '../api/product';
import { Product } from '../types';

const Home = () => {
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        // In a real app, we might have a specific endpoint for featured products
        const response = await getProducts({ pageNum: 1, pageSize: 4 });
        if (response.data && response.data.list) {
          setFeaturedProducts(response.data.list);
        } else {
          // Fallback mock data if API fails or returns empty
          setFeaturedProducts([
            { id: 1, name: '有机乳清蛋白粉', description: '草饲乳清蛋白分离物。', price: 49.99, imageUrl: 'https://picsum.photos/seed/whey/400/400', categoryId: 1, rating: 4.8, reviewCount: 120, stock: 100 },
            { id: 2, name: '复合维生素', description: '每日必需的维生素和矿物质。', price: 29.99, imageUrl: 'https://picsum.photos/seed/vitamin/400/400', categoryId: 2, rating: 4.5, reviewCount: 85, stock: 50 },
            { id: 3, name: 'Omega-3 深海鱼油', description: '高含量 EPA & DHA。', price: 24.99, imageUrl: 'https://picsum.photos/seed/omega/400/400', categoryId: 2, rating: 4.7, reviewCount: 200, stock: 80 },
            { id: 4, name: '训练前能量补剂', description: '提升您的能量和专注力。', price: 39.99, imageUrl: 'https://picsum.photos/seed/energy/400/400', categoryId: 1, rating: 4.6, reviewCount: 150, stock: 60 },
          ]);
        }
      } catch (error) {
        console.error('Failed to fetch products', error);
         // Fallback mock data
         setFeaturedProducts([
            { id: 1, name: '有机乳清蛋白粉', description: '草饲乳清蛋白分离物。', price: 49.99, imageUrl: 'https://picsum.photos/seed/whey/400/400', categoryId: 1, rating: 4.8, reviewCount: 120, stock: 100 },
            { id: 2, name: '复合维生素', description: '每日必需的维生素和矿物质。', price: 29.99, imageUrl: 'https://picsum.photos/seed/vitamin/400/400', categoryId: 2, rating: 4.5, reviewCount: 85, stock: 50 },
            { id: 3, name: 'Omega-3 深海鱼油', description: '高含量 EPA & DHA。', price: 24.99, imageUrl: 'https://picsum.photos/seed/omega/400/400', categoryId: 2, rating: 4.7, reviewCount: 200, stock: 80 },
            { id: 4, name: '训练前能量补剂', description: '提升您的能量和专注力。', price: 39.99, imageUrl: 'https://picsum.photos/seed/energy/400/400', categoryId: 1, rating: 4.6, reviewCount: 150, stock: 60 },
          ]);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  return (
    <div className="space-y-16">
      {/* Hero Section */}
      <section className="relative bg-emerald-900 text-white rounded-3xl overflow-hidden">
        <div className="absolute inset-0">
          <img
            src="https://picsum.photos/seed/fitness/1920/1080?blur=2"
            alt="Fitness Background"
            className="w-full h-full object-cover opacity-40"
            referrerPolicy="no-referrer"
          />
        </div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32 flex flex-col items-center text-center">
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="text-4xl md:text-6xl font-extrabold tracking-tight mb-6"
          >
            强健体魄， <br className="hidden md:block" />
            <span className="text-emerald-400">享受生活</span>
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="text-lg md:text-xl text-gray-200 max-w-2xl mb-10"
          >
            探索为您量身定制的高端营养补充剂，助您达到最佳状态。
          </motion.p>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
          >
            <Link
              to="/products"
              className="inline-flex items-center px-8 py-3 border border-transparent text-base font-medium rounded-full text-emerald-900 bg-white hover:bg-gray-100 md:text-lg transition-colors"
            >
              立即购买
              <ArrowRight className="ml-2 h-5 w-5" />
            </Link>
          </motion.div>
        </div>
      </section>

      {/* Featured Products */}
      <section>
        <div className="flex justify-between items-end mb-8">
          <div>
            <h2 className="text-3xl font-bold text-gray-900">精选商品</h2>
            <p className="mt-2 text-gray-500">为您精心挑选。</p>
          </div>
          <Link to="/products" className="text-emerald-600 hover:text-emerald-700 font-medium flex items-center">
            查看全部 <ArrowRight className="ml-1 h-4 w-4" />
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="bg-white rounded-2xl shadow-sm p-4 animate-pulse">
                <div className="h-48 bg-gray-200 rounded-xl mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {featuredProducts.map((product) => (
              <Link key={product.id} to={`/products/${product.id}`} className="group">
                <div className="bg-white rounded-2xl shadow-sm hover:shadow-md transition-shadow overflow-hidden border border-gray-100">
                  <div className="relative aspect-square bg-gray-100">
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      referrerPolicy="no-referrer"
                    />
                  </div>
                  <div className="p-4">
                    <div className="flex items-center mb-2">
                      <Star className="h-4 w-4 text-yellow-400 fill-current" />
                      <span className="ml-1 text-sm text-gray-500">{product.rating} ({product.reviewCount})</span>
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-1 truncate">{product.name}</h3>
                    <p className="text-sm text-gray-500 mb-3 line-clamp-2">{product.description}</p>
                    <div className="flex items-center justify-between">
                      <span className="text-lg font-bold text-emerald-600">${product.price.toFixed(2)}</span>
                      <button className="p-2 rounded-full bg-emerald-50 text-emerald-600 hover:bg-emerald-100 transition-colors">
                        <ArrowRight className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* Categories Preview */}
      <section className="bg-gray-50 rounded-3xl p-8 md:p-12">
        <div className="text-center mb-12">
          <h2 className="text-3xl font-bold text-gray-900">按分类浏览</h2>
          <p className="mt-2 text-gray-500">找到适合您目标的营养品。</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { name: '维生素与健康', image: 'https://picsum.photos/seed/vitamins/400/300' },
            { name: '蛋白粉与健身', image: 'https://picsum.photos/seed/protein/400/300' },
            { name: '体重管理', image: 'https://picsum.photos/seed/weight/400/300' },
          ].map((category, idx) => (
            <div key={idx} className="relative group rounded-2xl overflow-hidden cursor-pointer h-64">
              <img
                src={category.image}
                alt={category.name}
                className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                referrerPolicy="no-referrer"
              />
              <div className="absolute inset-0 bg-black/40 group-hover:bg-black/50 transition-colors flex items-center justify-center">
                <h3 className="text-2xl font-bold text-white">{category.name}</h3>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default Home;
