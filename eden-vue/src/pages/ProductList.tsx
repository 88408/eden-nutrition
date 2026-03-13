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
        const res = await getProducts({
          categoryId: filters.category === '全部' || !filters.category ? undefined : filters.category, // Assuming category needs mapping or ID
          minPrice: filters.minPrice,
          maxPrice: filters.maxPrice
        });
        // Backend returns PageResult. Assuming res.data.list? Or client returns data directly?
        // client.ts returns res.data which is PageResult<Product>
        // So res.list should be correct if getProducts returns Promise<PageResult<Product>>
        // But wait, getProducts types says it returns whatever client.get returns.
        // Let's assume client.ts unwraps ApiResponse and returns data payload.
        setProducts(res.list || []);
      } catch (error) {
        console.error(error);
        // Fallback for demo if API fails? No, migration plan says remove mock.
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
