import React, { useContext, useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import ProductModal from '../components/ProductModal';
import ImageUpload from '../components/ImageUpload';
import ProductImageManager from '../components/ProductImageManager';
import LoadingSpinner from '../components/LoadingSpinner';
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
import { uploadRestaurantImage, updateImage, getImageUrl, uploadProductImage } from '../api/imageService';
import { OrderStatuses, StatusLabels, StatusClasses } from '../utils/statusUtils';
import { AuthContext } from '../contexts/AuthContext';

const daysOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const dayLabels = {
  'MONDAY': 'Monday',
  'TUESDAY': 'Tuesday', 
  'WEDNESDAY': 'Wednesday',
  'THURSDAY': 'Thursday',
  'FRIDAY': 'Friday',
  'SATURDAY': 'Saturday',
  'SUNDAY': 'Sunday'
};

const RestaurantManagement = () => {
  const { user, login } = useContext(AuthContext);
  const { restaurantId } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const [view, setView] = useState(searchParams.get('tab') || 'info');
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

        if (!rest.openingHours) {
          rest.openingHours = {};
        }
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

  const handleOrderStatusChange = async (orderId, newStatus) => {
    try {
      await updateOrderStatus(orderId, newStatus);
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
      toast.success('Order status updated');
    } catch (err) {
      toast.error(err.message || 'Status update failed');
    }
  };

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

  const [newCategoryName, setNewCategoryName] = useState('');
  const [showAddCategory, setShowAddCategory] = useState(false);
  const [categoryError, setCategoryError] = useState('');

  const addCategory = () => {
    const trimmed = newCategoryName.trim();
    if (!trimmed) {
      setCategoryError('Category name cannot be empty');
      return;
    }
    if (restaurant.categories?.some(cat => cat.name.toLowerCase() === trimmed.toLowerCase())) {
      setCategoryError('Category already exists');
      return;
    }
    setRestaurant(prev => ({
      ...prev,
      categories: [...(prev.categories || []), { id: null, name: trimmed }]
    }));
    setNewCategoryName('');
    setShowAddCategory(false);
    setCategoryError('');
  };

  const handleOpeningTimeChange = (day, field, val) => {
    setRestaurant(prev => ({
      ...prev,
      openingHours: {
        ...prev.openingHours,
        [day]: {
          ...(prev.openingHours?.[day] || {}),
          [field]: val
        }
      }
    }));
  };

  const saveRestaurantInfo = async () => {
    setLoading(true);
    try {

      const updated = await updateRestaurant(restaurantId, restaurant);
      if (!updated.openingHours) {
        updated.openingHours = {};
      }
      setRestaurant(updated);
      toast.success('Restaurant info saved!');
    } catch (err) {
      toast.error(err.message || 'Save failed');
    } finally {
      setLoading(false);
    }
  };

  const handleRestaurantImageUpload = async (file) => {
    await uploadRestaurantImage(file, restaurantId);
    const updatedRestaurant = await fetchRestaurant(restaurantId);
    setRestaurant(updatedRestaurant);
  };

  const handleRestaurantImageUpdate = async (file, imageId) => {
    await updateImage(file, imageId);
    const updatedRestaurant = await fetchRestaurant(restaurantId);
    setRestaurant(updatedRestaurant);
  };

  const allCategories = [...new Set(products?.map(p => p.category?.name))];
  const filteredProducts = selectedCategories?.length
    ? products?.filter(p => p.category && selectedCategories?.includes(p.category?.name))
    : products;

  const toggleCategory = cat => setSelectedCategories(prev =>
    prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
  );

  const openCreate = () => { setCurrentProduct({ name: '', description: '', price: 0, inventory: 0, category: { name: '' } }); setShowCreate(true); };
  const openEdit = p => { setCurrentProduct({ ...p, category: p.category || { name: '' } }); setShowEdit(true); };
  const openDelete = p => { setCurrentProduct(p); setShowDelete(true); };

  const handleProdChange = e => {
    const { name, value } = e.target;
    setCurrentProduct(prev => {
      if (name === 'price' || name === 'inventory') {
        const numValue = parseFloat(value);
        return {
          ...prev,
          [name]: isNaN(numValue) ? 0 : numValue
        };
      }
      return {
        ...prev,
        [name]: value
      };
    });
  };

  const saveProduct = async () => {
    if (!currentProduct.name || !currentProduct.category?.name || currentProduct.price <= 0) {
      toast.error('Please fill all required fields with valid values');
      return;
    }
    
    setLoading(true);
    try {
      const productData = {
        ...currentProduct,
        price: parseFloat(currentProduct.price) || 0,
        inventory: parseInt(currentProduct.inventory) || 0,
        restaurant: { id: restaurantId }
      };
      
      let saved;
      if (currentProduct.id) {
        saved = await updateProduct(currentProduct.id, productData);
        setProducts(ps => ps.map(p => p.id === saved.id ? saved : p));
        toast.success('Product updated');
      } else {
        saved = await createProduct(productData);
        // Upload image if there's a preview file
        if (currentProduct.image?.file) {
          try {
            const imageResult = await uploadProductImage(currentProduct.image.file, saved.id);
            saved.image = imageResult.data;
          } catch (error) {
            toast.warning('Product created but image upload failed');
          }
        }
        setProducts(ps => [saved, ...ps]);
        toast.success('Product created');
      }
      setShowCreate(false); setShowEdit(false);
    } catch (err) {
      toast.error(err.message || 'Save failed');
    } finally { setLoading(false); }
  };

  const handleProductImageUpdate = (imageData) => {
    setCurrentProduct(prev => ({ ...prev, image: imageData }));
    setProducts(ps => ps.map(p => p.id === currentProduct.id ? { ...p, image: imageData } : p));
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

  if (loading || !restaurant) return <LoadingSpinner text="Loading restaurant management..." />;

  console.log('Restaurant opening hours:', restaurant.openingHours);

  return (
    <div className="container mx-auto p-6 space-y-8">
      <ToastContainer />
      
      <div className="bg-white p-6 rounded-lg shadow">
        <h1 className="text-3xl font-bold mb-2">{restaurant.name} - Management</h1>
        <p className="text-neutral/60">Manage your restaurant information, products, and orders</p>
      </div>

      <div className="flex justify-between items-center">
        <div className="flex space-x-2">
          {['info', 'hours', 'products', 'orders'].map(tab => (
            <button 
              key={tab}
              className={`btn ${view === tab ? 'btn-primary' : 'btn-outline'}`} 
              onClick={() => {
                setView(tab);
                setSearchParams({ tab });
              }}
            >
              {tab === 'info' ? 'Restaurant Info' : 
               tab === 'hours' ? 'Opening Hours' :
               tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
        </div>
        <button
          onClick={() => window.location.reload()}
          className="btn btn-primary btn-sm"
        >
          Refresh
        </button>
      </div>

      {view === 'info' && (
        <section className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-2xl font-bold mb-4">Restaurant Information</h2>
          
          <div className="mb-6">
            <h3 className="font-semibold mb-2">Restaurant Logo</h3>
            <ImageUpload
              currentImage={restaurant.logo}
              onUpload={handleRestaurantImageUpload}
              onUpdate={handleRestaurantImageUpdate}
              placeholder="Upload Restaurant Logo"
            />
          </div>
          
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          <div>
            <label className="block text-sm font-medium mb-1">Restaurant Name *</label>
            <input name="name" value={restaurant.name} onChange={handleInfoChange}
              className="input input-bordered w-full" placeholder="Enter restaurant name" required />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Phone Number *</label>
            <input name="phoneNumber" value={restaurant.phoneNumber} onChange={handleInfoChange}
              className="input input-bordered w-full" placeholder="Enter phone number" required />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Delivery Fee ($)</label>
            <input name="deliveryFee" type="number" step="0.01" min="0"
              value={restaurant.deliveryFee || 0.00}
              onChange={handleDeliveryFeeChange}
              className="input input-bordered w-full" placeholder="0.00" />
          </div>
        </div>
        <div className="mb-6">
          <h3 className="font-semibold mb-2">Food Categories</h3>
          <div className="space-y-2">
            {restaurant.categories?.map((cat, idx) => (
              <div key={idx} className="flex items-center gap-2">
                <input value={cat.name} onChange={e => handleCategoryNameChange(idx, e.target.value)}
                  className="input input-bordered flex-1" placeholder="Category Name" />
                <button className="btn btn-sm btn-error" onClick={() => removeCategory(idx)}>Remove</button>
              </div>
            ))}
            {showAddCategory ? (
              <div className="mt-2">
                <div className="flex gap-2">
                  <input 
                    value={newCategoryName} 
                    onChange={(e) => {setNewCategoryName(e.target.value); setCategoryError('');}}
                    className={`input input-bordered input-sm flex-1 ${categoryError ? 'input-error' : ''}`}
                    placeholder="Category name" 
                    onKeyPress={(e) => e.key === 'Enter' && addCategory()}
                  />
                  <button className="btn btn-primary btn-sm" onClick={addCategory}>Add</button>
                  <button className="btn btn-ghost btn-sm" onClick={() => {setShowAddCategory(false); setNewCategoryName(''); setCategoryError('');}}>Cancel</button>
                </div>
                {categoryError && <p className="text-error text-sm mt-1">{categoryError}</p>}
              </div>
            ) : (
              <button className="btn btn-secondary btn-sm mt-2" onClick={() => setShowAddCategory(true)}>Add Category</button>
            )}
          </div>
        </div>
        <button className="btn btn-primary" onClick={saveRestaurantInfo} disabled={loading}>
          {loading ? 'Saving...' : 'Save Restaurant Info'}
        </button>
      </section>
      )}

      {view === 'hours' && (
        <section className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-2xl font-bold mb-4">Opening Hours</h2>
          <div className="mb-6">
            <h3 className="font-semibold mb-2">Opening Hours</h3>
            <div className="space-y-2">
              {daysOrder.map(day => {
                const hrs = restaurant.openingHours?.[day] || {};
                const openingTime = hrs.openingTime ? hrs.openingTime.substring(0, 5) : '';
                const closingTime = hrs.closingTime ? hrs.closingTime.substring(0, 5) : '';

                return (
                  <div key={day} className="flex items-center gap-4">
                    <span className="w-24 font-medium">{dayLabels[day]}</span>
                    <input type="time" value={openingTime}
                      onChange={e => handleOpeningTimeChange(day, 'openingTime', e.target.value)}
                      className="input input-bordered w-32" />
                    <span>to</span>
                    <input type="time" value={closingTime}
                      onChange={e => handleOpeningTimeChange(day, 'closingTime', e.target.value)}
                      className="input input-bordered w-32" />
                  </div>
                );
              })}
            </div>
          </div>
          <button className="btn btn-primary" onClick={saveRestaurantInfo} disabled={loading}>
            {loading ? 'Saving...' : 'Save Opening Hours'}
          </button>
        </section>
      )}

      {view === 'products' && (
        <section className="bg-white p-6 rounded-lg shadow space-y-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold">Manage Products</h2>
            <button className="btn btn-primary" onClick={openCreate}>New Product</button>
          </div>
          <div className="flex gap-6">
            <aside className="w-1/4 p-2 bg-base-100 rounded-lg space-y-4">
              <h4 className="font-semibold mb-2">Filter by Type</h4>
              {allCategories.filter(cat => cat).map(cat => (
                <label key={cat} className="flex items-center">
                  <input type="checkbox" checked={selectedCategories?.includes(cat)}
                    onChange={() => toggleCategory(cat)} className="mr-2" />
                  {cat}
                </label>
              ))}
              <button
                onClick={() => window.location.reload()}
                className="btn btn-primary btn-sm w-full mt-4"
              >
                Refresh
              </button>
            </aside>
            <main className="flex-1 space-y-4">
              {filteredProducts?.map(p => (
                <div key={p.id} className="flex items-center bg-white p-4 rounded shadow">
                  {p.image?.id && (
                    <img 
                      src={getImageUrl(p.image.id)} 
                      alt={p.name}
                      className="w-16 h-16 object-cover rounded mr-4"
                    />
                  )}
                  <div className="flex-1">
                    <h5 className="font-semibold">{p.name}</h5>
                    <p className="text-sm">${p.price.toFixed(2)}</p>
                    <p className="text-xs text-gray-500">{p.category?.name || 'No category'}</p>
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

      {view === 'orders' && (
        <section className="bg-white p-6 rounded-lg shadow space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-bold">Incoming Orders</h2>
            <button
              onClick={() => window.location.reload()}
              className="btn btn-primary btn-sm"
            >
              Refresh
            </button>
          </div>
          <div className="space-y-4">
            {orders.map(o => (
              <div key={o.id} className="bg-base-100 p-4 rounded-lg shadow flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div className="cursor-pointer" onClick={() => window.location.href = `/orders/${o.id}`}>
                  <p className="font-semibold text-blue-600 hover:underline">Order #{o.id}</p>
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

      {(showCreate || showEdit) && (
        <div className="modal modal-open">
          <div className="modal-box max-w-lg">
            <h3 className="font-bold text-lg mb-4">{showCreate ? 'New' : 'Edit'} Product</h3>
            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium mb-1">Product Name *</label>
                <input name="name" value={currentProduct.name} onChange={handleProdChange}
                  className="input input-bordered w-full" placeholder="Enter product name" required />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Description</label>
                <textarea name="description" value={currentProduct.description} onChange={handleProdChange}
                  className="textarea textarea-bordered w-full" placeholder="Enter product description" rows={3} />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Price ($) *</label>
                <input name="price" type="number" step="0.01" min="0" value={currentProduct.price || 0} onChange={handleProdChange}
                  className="input input-bordered w-full" placeholder="0.00" required />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Inventory *</label>
                <input name="inventory" type="number" step="1" min="0" value={currentProduct.inventory || 0} onChange={handleProdChange}
                  className="input input-bordered w-full" placeholder="0" required />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Category *</label>
                <input name="category.name" value={currentProduct.category.name} onChange={e => setCurrentProduct(prev => ({ ...prev, category: { name: e.target.value } }))}
                  className="input input-bordered w-full" placeholder="Enter category" required />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1">Product Image</label>
                <ProductImageManager
                  product={currentProduct}
                  onImageUpdate={handleProductImageUpdate}
                />
              </div>
            </div>
            <div className="modal-action">
              <button className="btn" onClick={() => { setShowCreate(false); setShowEdit(false); }}>Cancel</button>
              <button className="btn btn-primary" onClick={saveProduct}>Save</button>
            </div>
          </div>
        </div>
      )}

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