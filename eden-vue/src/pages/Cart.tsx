import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Trash2, Plus, Minus, ArrowRight } from 'lucide-react';
import { getCart, updateCartItem, removeCartItem } from '../api/cart';
import { setCartItems } from '../store/cartSlice';
import { RootState } from '../store';
import { CartItem } from '../types';

const Cart = () => {
  const { items, totalAmount, totalQuantity } = useSelector((state: RootState) => state.cart);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCart = async () => {
      setLoading(true);
      try {
        const res = await getCart();
        dispatch(setCartItems(res));
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, [dispatch]);

  const handleUpdateQuantity = async (productId: number, newQuantity: number) => {
    if (newQuantity < 1) return;
    try {
      await updateCartItem({ productId, quantity: newQuantity });
      // Refresh cart to ensure consistency
      const res = await getCart();
      dispatch(setCartItems(res));
    } catch (error) {
      console.error(error);
    }
  };

  const handleRemoveItem = async (productId: number) => {
    try {
      await removeCartItem(productId);
      const res = await getCart();
      dispatch(setCartItems(res));
    } catch (error) {
      console.error(error);
    }
  };

  if (loading) return <div className="text-center py-20">加载购物车...</div>;

  if (items.length === 0) {
    return (
      <div className="text-center py-20">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">您的购物车是空的</h2>
        <p className="text-gray-500 mb-8">您还没有添加任何商品。</p>
        <Link to="/products" className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-emerald-600 hover:bg-emerald-700">
          开始购物
        </Link>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="p-6 md:p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-8">购物车 ({totalQuantity} 件商品)</h1>

        <div className="flow-root">
          <ul className="-my-6 divide-y divide-gray-200">
            {items.map((item) => (
              <li key={item.productId} className="flex py-6">
                <div className="h-24 w-24 flex-shrink-0 overflow-hidden rounded-md border border-gray-200">
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
                      <h3>
                        <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                      </h3>
                      <p className="ml-4">${(item.price * item.quantity).toFixed(2)}</p>
                    </div>
                    <p className="mt-1 text-sm text-gray-500">${item.price.toFixed(2)} / 单价</p>
                  </div>
                  <div className="flex flex-1 items-end justify-between text-sm">
                    <div className="flex items-center border border-gray-300 rounded-md">
                      <button
                        onClick={() => handleUpdateQuantity(item.productId, item.quantity - 1)}
                        className="p-1 hover:bg-gray-50 text-gray-600"
                      >
                        <Minus className="h-4 w-4" />
                      </button>
                      <span className="px-2 py-1 text-gray-900 font-medium">{item.quantity}</span>
                      <button
                        onClick={() => handleUpdateQuantity(item.productId, item.quantity + 1)}
                        className="p-1 hover:bg-gray-50 text-gray-600"
                      >
                        <Plus className="h-4 w-4" />
                      </button>
                    </div>

                    <div className="flex">
                      <button
                        type="button"
                        onClick={() => handleRemoveItem(item.productId)}
                        className="font-medium text-red-600 hover:text-red-500 flex items-center"
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        移除
                      </button>
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="border-t border-gray-200 p-6 md:p-8 bg-gray-50">
        <div className="flex justify-between text-base font-medium text-gray-900 mb-4">
          <p>小计</p>
          <p>${totalAmount.toFixed(2)}</p>
        </div>
        <p className="mt-0.5 text-sm text-gray-500 mb-6">运费和税费将在结算时计算。</p>
        <div className="mt-6">
          <Link
            to="/checkout"
            className="flex items-center justify-center rounded-md border border-transparent bg-emerald-600 px-6 py-3 text-base font-medium text-white shadow-sm hover:bg-emerald-700 w-full"
          >
            去结算 <ArrowRight className="ml-2 h-5 w-5" />
          </Link>
        </div>
        <div className="mt-6 flex justify-center text-center text-sm text-gray-500">
          <p>
            或者{' '}
            <Link to="/products" className="font-medium text-emerald-600 hover:text-emerald-500">
              继续购物
              <span aria-hidden="true"> &rarr;</span>
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Cart;
