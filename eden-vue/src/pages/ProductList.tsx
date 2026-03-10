import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Filter, Star } from 'lucide-react';
import { getProducts } from '../api/product';
import { Product } from '../types';

const ProductList = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    category: '',
    minPrice: '',
    maxPrice: '',
  });

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        // Mock API call
        // const res = await getProducts({ ...filters });
        // setProducts(res.data.list);
        
        // Mock data
        await new Promise(resolve => setTimeout(resolve, 500));
        setProducts([
          { id: 1, name: '有机乳清蛋白粉', description: '草饲乳清蛋白分离物。', price: 49.99, imageUrl: 'https://picsum.photos/seed/whey/400/400', categoryId: 1, rating: 4.8, reviewCount: 120, stock: 100 },
          { id: 2, name: '复合维生素', description: '每日必需的维生素和矿物质。', price: 29.99, imageUrl: 'https://picsum.photos/seed/vitamin/400/400', categoryId: 2, rating: 4.5, reviewCount: 85, stock: 50 },
          { id: 3, name: 'Omega-3 深海鱼油', description: '高含量 EPA & DHA。', price: 24.99, imageUrl: 'https://picsum.photos/seed/omega/400/400', categoryId: 2, rating: 4.7, reviewCount: 200, stock: 80 },
          { id: 4, name: '训练前能量补剂', description: '提升您的能量和专注力。', price: 39.99, imageUrl: 'https://picsum.photos/seed/energy/400/400', categoryId: 1, rating: 4.6, reviewCount: 150, stock: 60 },
          { id: 5, name: 'BCAA 支链氨基酸', description: '加速恢复，减少肌肉流失。', price: 34.99, imageUrl: 'https://picsum.photos/seed/bcaa/400/400', categoryId: 1, rating: 4.4, reviewCount: 90, stock: 75 },
          { id: 6, name: '植物蛋白粉', description: '纯植物来源蛋白混合。', price: 44.99, imageUrl: 'https://picsum.photos/seed/vegan/400/400', categoryId: 1, rating: 4.2, reviewCount: 60, stock: 40 },
        ]);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [filters]);

  return (
    <div className="flex flex-col md:flex-row gap-8">
      {/* Sidebar Filters */}
      <aside className="w-full md:w-64 flex-shrink-0">
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 sticky top-24">
          <div className="flex items-center mb-4">
            <Filter className="h-5 w-5 text-gray-500 mr-2" />
            <h2 className="text-lg font-semibold text-gray-900">筛选</h2>
          </div>
          
          <div className="space-y-6">
            <div>
              <h3 className="text-sm font-medium text-gray-900 mb-2">分类</h3>
              <div className="space-y-2">
                {['全部', '蛋白粉', '维生素', '运动表现', '减脂'].map((cat) => (
                  <label key={cat} className="flex items-center">
                    <input type="checkbox" className="rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
                    <span className="ml-2 text-sm text-gray-600">{cat}</span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <h3 className="text-sm font-medium text-gray-900 mb-2">价格区间</h3>
              <div className="flex items-center space-x-2">
                <input
                  type="number"
                  placeholder="最低价"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-emerald-500 focus:border-emerald-500"
                />
                <span className="text-gray-500">-</span>
                <input
                  type="number"
                  placeholder="最高价"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-emerald-500 focus:border-emerald-500"
                />
              </div>
            </div>
          </div>
        </div>
      </aside>

      {/* Product Grid */}
      <div className="flex-1">
        <div className="mb-6 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">所有商品</h1>
          <select className="border-gray-300 rounded-md text-sm focus:ring-emerald-500 focus:border-emerald-500">
            <option>排序: 精选</option>
            <option>价格: 从低到高</option>
            <option>价格: 从高到低</option>
            <option>最新上架</option>
          </select>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="bg-white rounded-lg shadow-sm p-4 animate-pulse h-80"></div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {products.map((product) => (
              <Link key={product.id} to={`/products/${product.id}`} className="group">
                <div className="bg-white rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-gray-100 overflow-hidden h-full flex flex-col">
                  <div className="relative aspect-square bg-gray-100">
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      referrerPolicy="no-referrer"
                    />
                  </div>
                  <div className="p-4 flex-1 flex flex-col">
                    <div className="flex items-center mb-1">
                      <Star className="h-4 w-4 text-yellow-400 fill-current" />
                      <span className="ml-1 text-xs text-gray-500">{product.rating}</span>
                    </div>
                    <h3 className="text-base font-medium text-gray-900 mb-1 group-hover:text-emerald-600 transition-colors">{product.name}</h3>
                    <p className="text-sm text-gray-500 mb-4 line-clamp-2 flex-1">{product.description}</p>
                    <div className="flex items-center justify-between mt-auto">
                      <span className="text-lg font-bold text-gray-900">${product.price.toFixed(2)}</span>
                      <span className="text-xs text-emerald-600 font-medium bg-emerald-50 px-2 py-1 rounded-full">现货</span>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductList;
