// Placeholder component if it was missing or I need to create it for migration
import React, { useEffect, useState } from 'react';
import { getProducts } from '../api/product';
import { Product } from '../types';
import { Link } from 'react-router-dom';
import { Star, ShoppingCart } from 'lucide-react';
import { addToCart } from '../api/cart';

const FeaturedProducts = () => {
    const [products, setProducts] = useState<Product[]>([]);
    
    useEffect(() => {
        getProducts({ pageSize: 4 }).then(res => {
            // Assuming res.list in migration plan logic
             if (res && (res as any).list) {
                 setProducts((res as any).list);
             } else if (Array.isArray(res)) {
                 setProducts(res);
             }
        }).catch(console.error);
    }, []);

    const handleAddToCart = async (product: Product) => {
        try {
            await addToCart({ productId: product.id, quantity: 1 });
            alert('Added to cart!');
        } catch (e) {
            console.error(e);
        }
    };
    
    if (products.length === 0) return null;

    return (
        <section className="py-12 bg-white">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <h2 className="text-3xl font-bold text-gray-900 mb-8">Featured Products</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                    {products.map(product => (
                        <div key={product.id} className="group relative border rounded-lg p-4 hover:shadow-lg transition-shadow">
                            <Link to={`/products/${product.id}`} className="block">
                                <div className="aspect-w-1 aspect-h-1 w-full overflow-hidden rounded-md bg-gray-200 lg:aspect-none group-hover:opacity-75 lg:h-80">
                                    <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover object-center lg:h-full lg:w-full" />
                                </div>
                                <div className="mt-4 flex justify-between">
                                    <div>
                                        <h3 className="text-sm text-gray-700">
                                            <span aria-hidden="true" className="absolute inset-0" />
                                            {product.name}
                                        </h3>
                                        <p className="mt-1 text-sm text-gray-500">{product.rating} <Star className="inline w-3 h-3 text-yellow-500 fill-current" /></p>
                                    </div>
                                    <p className="text-sm font-medium text-gray-900">${product.price}</p>
                                </div>
                            </Link>
                             <button 
                                onClick={(e) => { e.preventDefault(); handleAddToCart(product); }}
                                className="mt-4 w-full bg-emerald-600 text-white py-2 rounded-md hover:bg-emerald-700 z-10 relative"
                            >
                                Add to Cart
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default FeaturedProducts;
