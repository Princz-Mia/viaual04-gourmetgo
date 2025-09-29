import React, { useContext, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import ProductModal from '../components/ProductModal';
import {
  fetchRestaurant,
  updateRestaurant
} from '../api/restaurantService';
import {
  fetchProductsByRestaurantId,
  createProduct,
  updateProduct,
  deleteProduct,
} from '../api/productService';
import { fetchRestaurantOrders, updateOrderStatus } from '../api/orderService';
import { OrderStatuses, StatusLabels, StatusClasses } from '../utils/statusUtils';
import { AuthContext } from '../contexts/AuthContext';

const daysOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const RestaurantManagement = () => {
  const { user, login } = useContext(AuthContext);
  const { restaurantId } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [view, setView] = useState('products');
  const [selectedCategories, setSelectedCategories] = useState([]);

  // Product CRUD state
  const [showCreate, setShowCreate] = useState(false);
  const [showEdit, setShowEdit] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [currentProduct, setCurrentProduct] = useState(null);

  useEffect(() => {
    const loadAll = async () => {
      setLoading(true);
      try {
        const rest = await fetchRestaurant(restaurantId);
        setRestaurant(rest);
        const prods = await fetchProductsByRestaurantId(restaurantId);
        setProducts(prods);
        const ords = await fetchRestaurantOrders(restaurantId);
        setOrders(ords);
      } catch (err) {
        toast.error(err.message || 'Load failed');
      } finally {
        setLoading(false);
      }
    };
    loadAll();
  }, [restaurantId]);

  // Handle order status updates
  const handleOrderStatusChange = async (orderId, newStatus) => {
    try {
      await updateOrderStatus(orderId, newStatus);
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
      toast.success('Order status updated');
    } catch (err) {
      toast.error(err.message || 'Status update failed');
    }
  };

  // Restaurant info change
  const handleInfoChange = e => {
    const { name, value } = e.target;
    setRestaurant(prev => ({ ...prev, [name]: value }));
  };

  const handleDeliveryFeeChange = e => {
    const fee = parseFloat(e.target.value);
    if (!isNaN(fee) && fee >= 0) {
      setRestaurant(prev => ({ ...prev, deliveryFee: fee }));
    }
  };

  const handleCategoryNameChange = (idx, newName) => {
    setRestaurant(prev => {
      const cats = prev.categories.map((c, i) => i === idx ? { ...c, name: newName } : c);
      return { ...prev, categories: cats };
    });
  };

  const removeCategory = idx => {
    setRestaurant(prev => {
      const cats = prev.categories.filter((_, i) => i !== idx);
      return { ...prev, categories: cats };
    });
  };

  const addCategory = () => {
    setRestaurant(prev => ({
      ...prev,
      categories: [...(prev.categories || []), { id: null, name: '' }]
    }));
  };

  const handleOpeningTimeChange = (day, field, val) => {
    setRestaurant(prev => ({
      ...prev,
      openingHours: {
        ...prev.openingHours,
        [day]: {
          ...prev.openingHours[day],
          [field]: val
        }
      }
    }));
  };

  const saveRestaurantInfo = async () => {
    setLoading(true);
    try {
      const updated = await updateRestaurant(restaurantId, restaurant);
      setRestaurant(updated);
      toast.success('Restaurant info saved!');
      if (updated !== null) {
        window.location.reload();
      }
    } catch (err) {
      toast.error(err.message || 'Save failed');
    } finally {
      setLoading(false);
    }
  };

  // Product CRUD
  const allCategories = [...new Set(products?.map(p => p.category?.name))];
  const filteredProducts = selectedCategories?.length
    ? products?.filter(p => selectedCategories?.includes(p.category?.name))
    : products;

  const toggleCategory = cat => setSelectedCategories(prev =>
    prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
  );

  const openCreate = () => { setCurrentProduct({ name: '', description: '', price: 0, inventory: 0, category: { name: '' } }); setShowCreate(true); };
  const openEdit = p => { setCurrentProduct({ ...p, category: p.category || { name: '' } }); setShowEdit(true); };
  const openDelete = p => { setCurrentProduct(p); setShowDelete(true); };

  const handleProdChange = e => {
    const { name, value } = e.target;
    setCurrentProduct(prev => ({
      ...prev,
      [name]: name === 'price' || name === 'inventory' ? parseFloat(value) : value
    }));
  };

  const saveProduct = async () => {
    setLoading(true);
    try {
      let saved;
      if (currentProduct.id) {
        saved = await updateProduct(currentProduct.id, currentProduct);
        setProducts(ps => ps.map(p => p.id === saved.id ? saved : p));
        toast.success('Product updated');
      } else {
        saved = await createProduct({ ...currentProduct, restaurant: { id: restaurantId } });
        setProducts(ps => [saved, ...ps]);
        toast.success('Product created');
      }
      setShowCreate(false); setShowEdit(false);
    } catch (err) {
      toast.error(err.message || 'Save failed');
    } finally { setLoading(false); }
  };

  const confirmDelete = async () => {
    setLoading(true);
    try {
      await deleteProduct(currentProduct.id);
      setProducts(ps => ps.filter(p => p.id !== currentProduct.id));
      toast.success('Product deleted');
      setShowDelete(false);
    } catch (err) {
      toast.error(err.message || 'Delete failed');
    } finally { setLoading(false); }
  };

  if (loading || !restaurant) return <p className="text-center py-8">Loading...</p>;

  return (
    <div className="container mx-auto p-6 space-y-8">
      <ToastContainer />
      {/* Restaurant Info Section */}
      <section className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-2xl font-bold mb-4">Edit Restaurant Info</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          <input name="name" value={restaurant.name} onChange={handleInfoChange}
            className="input input-bordered" placeholder="Name" />
          <input name="phoneNumber" value={restaurant.phoneNumber} onChange={handleInfoChange}
            className="input input-bordered" placeholder="Phone Number" />
          <input name="ownerName" value={restaurant.ownerName} onChange={handleInfoChange}
            className="input input-bordered" placeholder="Owner Name" />
          <input name="deliveryFee" type="number" step="0.01" min="0"
            value={restaurant.deliveryFee || 0.00}
            onChange={handleDeliveryFeeChange}
            className="input input-bordered" placeholder="Delivery Fee" />
        </div>
        <div className="mb-6">
          <h3 className="font-semibold mb-2">Categories</h3>
          <div className="space-y-2">
            {restaurant.categories?.map((cat, idx) => (
              <div key={idx} className="flex items-center gap-2">
                <input value={cat.name} onChange={e => handleCategoryNameChange(idx, e.target.value)}
                  className="input input-bordered flex-1" placeholder="Category Name" />
                <button className="btn btn-sm btn-error" onClick={() => removeCategory(idx)}>Remove</button>
              </div>
            ))}
            <button className="btn btn-secondary btn-sm mt-2" onClick={addCategory}>Add Category</button>
          </div>
        </div>
        <div className="mb-6">
          <h3 className="font-semibold mb-2">Opening Hours</h3>
          <div className="space-y-2">
            {daysOrder.map(day => {
              const hrs = restaurant.openingHours?.[day] || {};
              return (
                <div key={day} className="flex items-center gap-4">
                  <span className="w-24 font-medium">{day}</span>
                  <input type="time" value={hrs.openingTime || ''}
                    onChange={e => handleOpeningTimeChange(day, 'openingTime', e.target.value)}
                    className="input input-bordered w-32" />
                  <span>to</span>
                  <input type="time" value={hrs.closingTime || ''}
                    onChange={e => handleOpeningTimeChange(day, 'closingTime', e.target.value)}
                    className="input input-bordered w-32" />
                </div>
              );
            })}
          </div>
        </div>
        <button className="btn btn-primary" onClick={saveRestaurantInfo} disabled={loading}>
          {loading ? 'Saving…' : 'Save Info'}
        </button>
      </section>

      {/* Tab Buttons */}
      <div className="flex space-x-2">
        <button className={`btn ${view === 'products' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setView('products')}>Products</button>
        <button className={`btn ${view === 'orders' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setView('orders')}>Orders</button>
      </div>

      {/* Products View */}
      {view === 'products' && (
        <section className="bg-white p-6 rounded-lg shadow space-y-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold">Manage Products</h2>
            <button className="btn btn-primary" onClick={openCreate}>New Product</button>
          </div>
          <div className="flex gap-6">
            <aside className="w-1/4 p-2 bg-base-100 rounded-lg">
              <h4 className="font-semibold mb-2">Filter by Type</h4>
              {allCategories.map(cat => (
                <label key={cat} className="flex items-center">
                  <input type="checkbox" checked={selectedCategories?.includes(cat)}
                    onChange={() => toggleCategory(cat)} className="mr-2" />
                  {cat}
                </label>
              ))}
            </aside>
            <main className="flex-1 space-y-4">
              {filteredProducts?.map(p => (
                <div key={p.id} className="flex items-center bg-white p-4 rounded shadow">
                  <div className="flex-1">
                    <h5 className="font-semibold">{p.name}</h5>
                    <p className="text-sm">${p.price.toFixed(2)}</p>
                    <p className="text-xs text-gray-500">{p.category?.name}</p>
                  </div>
                  <div className="flex flex-col gap-2">
                    <button className="btn btn-sm" onClick={() => openEdit(p)}>Edit</button>
                    <button className="btn btn-error btn-sm" onClick={() => openDelete(p)}>Delete</button>
                  </div>
                </div>
              ))}
            </main>
          </div>
        </section>
      )}

      {/* Orders View */}
      {view === 'orders' && (
        <section className="bg-white p-6 rounded-lg shadow space-y-4">
          <h2 className="text-2xl font-bold">Incoming Orders</h2>
          <div className="space-y-4">
            {orders.map(o => (
              <div key={o.id} className="bg-base-100 p-4 rounded-lg shadow flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                  <p className="font-semibold">Order #{o.id}</p>
                  <p className="text-sm text-gray-500">Date: {new Date(o.orderDate).toLocaleDateString()}</p>
                </div>
                <div className="space-y-1">
                  {o.orderItems.map((it, i) => (<p key={i} className="text-sm">{it.product.name} x {it.quantity}</p>))}
                </div>
                <select value={o.status} onChange={e => handleOrderStatusChange(o.id, e.target.value)} className="select select-bordered select-sm w-40">
                  {OrderStatuses.map(s => (
                    <option key={s} value={s}>
                      {StatusLabels[s]}
                    </option>
                  ))}
                </select>
              </div>
            ))}
            {orders.length === 0 && <p className="text-gray-500">No incoming orders.</p>}
          </div>
        </section>
      )}

      {/* Create/Edit Product Modal */}
      {(showCreate || showEdit) && (
        <div className="modal modal-open">
          <div className="modal-box max-w-lg">
            <h3 className="font-bold text-lg mb-4">{showCreate ? 'New' : 'Edit'} Product</h3>
            <div className="space-y-3">
              <input name="name" value={currentProduct.name} onChange={handleProdChange}
                className="input w-full" placeholder="Name" />
              <textarea name="description" value={currentProduct.description} onChange={handleProdChange}
                className="textarea w-full" placeholder="Description" rows={3} />
              <input name="price" type="number" step="0.01" value={currentProduct.price} onChange={handleProdChange}
                className="input w-full" placeholder="Price" />
              <input name="inventory" type="number" step="1" value={currentProduct.inventory} onChange={handleProdChange}
                className="input w-full" placeholder="Inventory" />
              <input name="category.name" value={currentProduct.category.name} onChange={e => setCurrentProduct(prev => ({ ...prev, category: { name: e.target.value } }))}
                className="input w-full" placeholder="Category" />
            </div>
            <div className="modal-action">
              <button className="btn" onClick={() => { setShowCreate(false); setShowEdit(false); }}>Cancel</button>
              <button className="btn btn-primary" onClick={saveProduct}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {showDelete && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold">Delete {currentProduct.name}?</h3>
            <p className="mb-4">This action cannot be undone.</p>
            <div className="modal-action">
              <button className="btn" onClick={() => setShowDelete(false)}>Cancel</button>
              <button className="btn btn-error" onClick={confirmDelete}>Delete</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RestaurantManagement;
