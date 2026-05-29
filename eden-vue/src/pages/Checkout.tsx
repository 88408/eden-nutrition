import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { RootState } from '../store';
import { createOrder } from '../api/order';

const Checkout = () => {
  const { items, totalAmount } = useSelector((state: RootState) => state.cart);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  
  // Mock Addresses
  const [addresses] = useState([
    { id: 1, name: 'John Doe', phone: '1234567890', address: '123 Main St, New York, NY 10001', isDefault: true },
    { id: 2, name: 'John Doe', phone: '1234567890', address: '456 Office Blvd, New York, NY 10002', isDefault: false },
  ]);
  const [selectedAddressId, setSelectedAddressId] = useState(addresses[0].id);

  const handlePlaceOrder = async () => {
    setLoading(true);
    try {
      // Create request payload matching OrderCreateDTO requires productIds
      const productIds = items.map(item => item.productId);
      const res = await createOrder({
        addressId: selectedAddressId,
        productIds: productIds
      });
      const orderNo = res.orderNo;
      alert(`订单提交成功！订单号: ${orderNo}`);
      // Clear cart locally if needed, but backend should clear it.
      // dispatch(clearCart()); 
      navigate('/user'); 
    } catch (error) {
      console.error(error);
      alert('订单提交失败:' + (error as any).message);
    } finally {
      setLoading(false);
    }
  };

  if (items.length === 0) {
    return <div className="text-center py-20">购物车为空。 <a href="/products" className="text-emerald-600">去购物</a></div>;
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {/* Left Column: Shipping & Payment */}
      <div className="space-y-8">
        {/* Shipping Address */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">收货地址</h2>
          <div className="space-y-4">
            {addresses.map((addr) => (
              <div
                key={addr.id}
                onClick={() => setSelectedAddressId(addr.id)}
                className={`p-4 rounded-lg border cursor-pointer transition-colors ${
                  selectedAddressId === addr.id
                    ? 'border-emerald-500 bg-emerald-50'
                    : 'border-gray-200 hover:border-emerald-200'
                }`}
              >
                <div className="flex justify-between">
                  <span className="font-medium text-gray-900">{addr.name}</span>
                  <span className="text-gray-500">{addr.phone}</span>
                </div>
                <p className="text-sm text-gray-600 mt-1">{addr.address}</p>
              </div>
            ))}
            <button className="text-emerald-600 text-sm font-medium hover:text-emerald-700">
              + 添加新地址
            </button>
          </div>
        </div>

        {/* Payment Method */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">支付方式</h2>
          <div className="space-y-4">
            <label className="flex items-center p-4 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50">
              <input type="radio" name="payment" className="h-4 w-4 text-emerald-600 focus:ring-emerald-500" defaultChecked />
              <span className="ml-3 font-medium text-gray-900">信用卡</span>
            </label>
            <label className="flex items-center p-4 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50">
              <input type="radio" name="payment" className="h-4 w-4 text-emerald-600 focus:ring-emerald-500" />
              <span className="ml-3 font-medium text-gray-900">PayPal / 支付宝</span>
            </label>
          </div>
        </div>
      </div>

      {/* Right Column: Order Summary */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 h-fit sticky top-24">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">订单摘要</h2>
        <div className="flow-root mb-6">
          <ul className="-my-4 divide-y divide-gray-200">
            {items.map((item) => (
              <li key={item.productId} className="flex py-4">
                <div className="h-16 w-16 flex-shrink-0 overflow-hidden rounded-md border border-gray-200">
                  <img
                    src={item.imageUrl}
                    alt={item.productName}
                    className="h-full w-full object-cover object-center"
                    referrerPolicy="no-referrer"
                  />
                </div>
                <div className="ml-4 flex flex-1 flex-col">
                  <div>
                    <div className="flex justify-between text-base font-medium text-gray-900">
                      <h3>{item.productName}</h3>
                      <p>${(item.price * item.quantity).toFixed(2)}</p>
                    </div>
                    <p className="mt-1 text-sm text-gray-500">数量 {item.quantity}</p>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className="border-t border-gray-200 pt-4 space-y-2">
          <div className="flex justify-between text-sm text-gray-600">
            <p>小计</p>
            <p>${totalAmount.toFixed(2)}</p>
          </div>
          <div className="flex justify-between text-sm text-gray-600">
            <p>运费</p>
            <p>$5.00</p>
          </div>
          <div className="flex justify-between text-sm text-gray-600">
            <p>税费</p>
            <p>${(totalAmount * 0.08).toFixed(2)}</p>
          </div>
          <div className="flex justify-between text-base font-medium text-gray-900 pt-2 border-t border-gray-200 mt-2">
            <p>总计</p>
            <p>${(totalAmount + 5 + totalAmount * 0.08).toFixed(2)}</p>
          </div>
        </div>

        <button
          onClick={handlePlaceOrder}
          disabled={loading}
          className="mt-6 w-full bg-emerald-600 text-white py-3 px-4 rounded-md font-bold hover:bg-emerald-700 transition-colors disabled:opacity-70 disabled:cursor-not-allowed"
        >
          {loading ? '处理中...' : '提交订单'}
        </button>
      </div>
    </div>
  );
};

export default Checkout;
