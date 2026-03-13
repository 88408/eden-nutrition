import React, { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Calendar, CheckCircle } from 'lucide-react';
import { getSeckillList, createSeckill, updateSeckill, publishSeckill, SeckillProduct } from '../../api/seckill';

const FlashSale = () => {
  const [seckills, setSeckills] = useState<SeckillProduct[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  // Basic form state
  const [formData, setFormData] = useState<Partial<SeckillProduct>>({});

  useEffect(() => {
    fetchSeckills();
  }, []);

  const fetchSeckills = async () => {
    try {
      const list = await getSeckillList();
      setSeckills(list);
    } catch (e) {
      console.error(e);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (formData.id) {
        await updateSeckill(formData as SeckillProduct);
      } else {
        await createSeckill(formData as SeckillProduct);
      }
      setIsModalOpen(false);
      fetchSeckills();
    } catch (e) {
      alert('Failed to save');
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await publishSeckill(id);
      fetchSeckills();
      alert('Published successfully');
    } catch (e) {
      alert('Publish failed');
    }
  };
  
  // Basic UI Implementation
  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">秒杀活动管理</h1>
        <button 
          onClick={() => { setFormData({}); setIsModalOpen(true); }}
          className="flex items-center px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700"
        >
          <Plus className="h-5 w-5 mr-2" />
          创建活动
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">商品ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">秒杀价格</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">库存</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">时间范围</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {seckills.map((item) => (
              <tr key={item.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">#{item.id}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.productId}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">¥{item.seckillPrice}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.stockCount}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(item.startTime).toLocaleString()} - <br/>
                  {new Date(item.endTime).toLocaleString()}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                   <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                     item.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                   }`}>
                     {item.status === 1 ? '进行中' : (item.status === 2 ? '已结束' : '即将开始')}
                   </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  {item.status === 0 && (
                     <button onClick={() => handlePublish(item.id!)} className="text-emerald-600 hover:text-emerald-900 mr-4">发布</button>
                  )}
                  <button onClick={() => { setFormData(item); setIsModalOpen(true); }} className="text-blue-600 hover:text-blue-900">编辑</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
                <h2 className="text-xl font-bold mb-4">{formData.id ? '编辑活动' : '创建活动'}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">商品ID</label>
                            <input type="number" required 
                                value={formData.productId || ''} 
                                onChange={e => setFormData({...formData, productId: Number(e.target.value)})}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">秒杀价格</label>
                            <input type="number" step="0.01" required 
                                value={formData.seckillPrice || ''} 
                                onChange={e => setFormData({...formData, seckillPrice: Number(e.target.value)})}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" />
                        </div>
                         <div>
                            <label className="block text-sm font-medium text-gray-700">库存数量</label>
                            <input type="number" required 
                                value={formData.stockCount || ''} 
                                onChange={e => setFormData({...formData, stockCount: Number(e.target.value)})}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">开始时间</label>
                            <input type="datetime-local" required 
                                value={formData.startTime ? new Date(formData.startTime).toISOString().slice(0, 16) : ''} 
                                onChange={e => setFormData({...formData, startTime: new Date(e.target.value + ':00Z').toISOString()})} // Simplified ISO conversion
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" />
                        </div>
                         <div>
                            <label className="block text-sm font-medium text-gray-700">结束时间</label>
                            <input type="datetime-local" required 
                                value={formData.endTime ? new Date(formData.endTime).toISOString().slice(0, 16) : ''} 
                                onChange={e => setFormData({...formData, endTime: new Date(e.target.value + ':00Z').toISOString()})}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" />
                        </div>
                    </div>
                    <div className="mt-6 flex justify-end space-x-3">
                        <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50">取消</button>
                        <button type="submit" className="px-4 py-2 bg-emerald-600 text-white rounded-md hover:bg-emerald-700">保存</button>
                    </div>
                </form>
            </div>
        </div>
      )}
    </div>
  );
};

export default FlashSale;