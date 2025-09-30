import React, { useState, useEffect } from "react";
import {
  getUsers,
  lockUser,
  deleteUser,
} from "../api/userService";
import {
  fetchAllOrders,
  fetchOrderById,
  updateOrderStatus as updateOrderStatusApi,
} from "../api/orderService";
import {
  getPendingRestaurants,
  approveRestaurant,
  rejectRestaurant,
} from "../api/restaurantService";
import {
  getCoupons,
  createCoupon,
  updateCoupon as updateCouponApi,
  deleteCoupon as deleteCouponApi,
} from "../api/couponService";
import imageNotFound from "../assets/images/image_not_found.jpg"
import {
  OrderStatuses,
  StatusLabels,
  StatusClasses
} from "../utils/statusUtils";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";

const roles = ["ALL", "ROLE_CUSTOMER", "ROLE_RESTAURANT"];

export default function CompanyManagement() {
  const [view, setView] = useState("users");

  // Users state
  const [users, setUsers] = useState([]);
  const [userSearch, setUserSearch] = useState("");
  const [userFilterRole, setUserFilterRole] = useState("ALL");
  const [userSort, setUserSort] = useState("nameAsc");
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [userDeleteConfirm, setUserDeleteConfirm] = useState(null);

  // Orders state
  const [orders, setOrders] = useState([]);
  const [orderSearch, setOrderSearch] = useState("");
  const [orderSort, setOrderSort] = useState("dateDesc");
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [filterStatus, setFilterStatus] = useState("");
  const [selectedOrderId, setSelectedOrderId] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [orderModalOpen, setOrderModalOpen] = useState(false);


  // Pending restaurants state
  const [pending, setPending] = useState([]);
  const [loadingPending, setLoadingPending] = useState(false);
  const [pendingActionConfirm, setPendingActionConfirm] = useState(null);

  // Coupons state
  const [coupons, setCoupons] = useState([]);
  const [couponSearch, setCouponSearch] = useState("");
  const [couponSort, setCouponSort] = useState("codeAsc");
  const [loadingCoupons, setLoadingCoupons] = useState(false);
  const [couponModal, setCouponModal] = useState(null);
  const [couponDeleteConfirm, setCouponDeleteConfirm] = useState(null);

  const today = new Date().toISOString().split('T')[0]; // 'YYYY-MM-DD'

  // Fetch all on mount
  useEffect(() => {
    async function fetchAll() {
      setLoadingUsers(true);
      setLoadingOrders(true);
      setLoadingPending(true);
      setLoadingCoupons(true);
      try {
        const [u, o, p, c] = await Promise.all([
          getUsers(),
          fetchAllOrders(),
          getPendingRestaurants(),
          getCoupons(),
        ]);
        const usersAdminRoleExcuded = u.filter((us) => us.role != "ROLE_ADMIN")
        setUsers(usersAdminRoleExcuded);
        setOrders(o);
        setPending(p);
        setCoupons(c);
      } catch (err) {
        console.error(err);
      } finally {
        setLoadingUsers(false);
        setLoadingOrders(false);
        setLoadingPending(false);
        setLoadingCoupons(false);
      }
    }
    fetchAll();
  }, []);

  const openOrderModal = async (orderId) => {
    try {
      const data = await fetchOrderById(orderId);
      setSelectedOrder(data);
      setSelectedOrderId(orderId);
      setOrderModalOpen(true);
    } catch (e) {
      toast.error("Failed to load order details");
    }
  };

  const closeOrderModal = () => {
    setOrderModalOpen(false);
    setSelectedOrder(null);
    setSelectedOrderId(null);
  };

  // User operations
  const handleLockToggle = async (user) => {
    try {
      await lockUser(user.id, !user.locked);
      setUsers((us) =>
        us.map((u) => (u.id === user.id ? { ...u, locked: !u.locked } : u))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const confirmDeleteUser = (user) => setUserDeleteConfirm(user);
  const cancelDeleteUser = () => setUserDeleteConfirm(null);
  const handleDeleteUser = async () => {
    const id = userDeleteConfirm.id;
    try {
      await deleteUser(id);
      setUsers((us) => us.filter((u) => u.id !== id));
    } catch (err) {
      console.error(err);
    } finally {
      setUserDeleteConfirm(null);
    }
  };

  // Order operations
  const handleStatusChange = async (order, status) => {
    try {
      await updateOrderStatusApi(order.id, status);
      setOrders((os) =>
        os.map((o) => (o.id === order.id ? { ...o, status } : o))
      );
    } catch (err) {
      console.error(err);
    }
  };

  // Pending restaurant operations
  const [pendingDetail, setPendingDetail] = useState(null);
  const confirmPendingAction = (r, action) => setPendingActionConfirm({ r, action });
  const cancelPendingAction = () => setPendingActionConfirm(null);
  const handleApprove = async () => {
    const { r } = pendingActionConfirm;
    try {
      await approveRestaurant(r.id);
      setPending((ps) => ps.filter((x) => x.id !== r.id));
    } catch (err) {
      console.error(err);
    } finally {
      cancelPendingAction();
    }
  };
  const handleReject = async () => {
    const { r } = pendingActionConfirm;
    try {
      await rejectRestaurant(r.id);
      setPending((ps) => ps.filter((x) => x.id !== r.id));
    } catch (err) {
      console.error(err);
    } finally {
      cancelPendingAction();
    }
  };

  // Coupon operations
  const handleSaveCoupon = async (coupon) => {
    try {
      let saved;
      if (coupon.id) {
        saved = await updateCouponApi(coupon);
        setCoupons((cs) => cs.map((c) => (c.id === saved.id ? saved : c)));
      } else {
        saved = await createCoupon(coupon);
        console.log(saved);
        setCoupons((cs) => [...cs, saved]);
      }
      setCouponModal(null);
    } catch (err) {
      console.error(err);
    }
  };
  const confirmDeleteCoupon = (c) => setCouponDeleteConfirm(c);
  const cancelDeleteCoupon = () => setCouponDeleteConfirm(null);
  const handleDeleteCoupon = async () => {
    const id = couponDeleteConfirm.id;
    try {
      await deleteCouponApi(id);
      setCoupons((cs) => cs.filter((c) => c.id !== id));
    } catch (err) {
      console.error(err);
    } finally {
      cancelDeleteCoupon();
    }
  };

  // Filtering & sorting
  const filteredUsers = users
    .filter(
      (u) =>
        (userFilterRole === "ALL" || u.role === userFilterRole) &&
        (u.fullName.toLowerCase().includes(userSearch.toLowerCase()) ||
          u.emailAddress.toLowerCase().includes(userSearch.toLowerCase()))
    )
    .sort((a, b) => {
      if (userSort === "nameAsc") return a.fullName.localeCompare(b.fullName);
      if (userSort === "nameDesc") return b.fullName.localeCompare(a.fullName);
      if (userSort === "idAsc") return a.id.localeCompare(b.id);
      if (userSort === "idDesc") return b.id.localeCompare(a.id);
      return 0;
    });

  const filteredOrders = orders
    .filter(o =>
      (!filterStatus || o.status === filterStatus) &&
      (o.id.includes(orderSearch) ||
        o.restaurant.name.toLowerCase().includes(orderSearch.toLowerCase()))
    )
    .sort((a, b) => {
      if (orderSort === "dateAsc") return new Date(a.date) - new Date(b.date);
      if (orderSort === "dateDesc") return new Date(b.date) - new Date(a.date);
      if (orderSort === "idAsc") return a.id.localeCompare(b.id);
      if (orderSort === "idDesc") return b.id.localeCompare(a.id);
      return 0;
    });

  const filteredCoupons = coupons
    .filter((c) => c.code.toLowerCase().includes(couponSearch.toLowerCase()))
    .sort((a, b) => {
      if (couponSort === "codeAsc") return a.code.localeCompare(b.code);
      if (couponSort === "codeDesc") return b.code.localeCompare(a.code);
      if (couponSort === "expAsc") return new Date(a.expirationDate) - new Date(b.expirationDate);
      if (couponSort === "expDesc") return new Date(b.expirationDate) - new Date(a.expirationDate);
      return 0;
    });

  return (
    <div className="container mx-auto px-4 py-8 space-y-6">
      {/* View Toggle */}
      <div className="flex space-x-2">
        {['users', 'orders', 'restaurants', 'coupons'].map(v => (
          <button
            key={v}
            className={`btn ${view === v ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setView(v)}
          >
            {v === 'restaurants' ? 'Pending Rest.' : v.charAt(0).toUpperCase() + v.slice(1)}
          </button>
        ))}
      </div>

      {/* Users Panel */}
      {view === 'users' && (
        loadingUsers
          ? <p>Loading users...</p>
          : <div className="space-y-4">
            <aside className="w-1/4 bg-base-100 p-4 rounded-lg shadow space-y-4">
              <input value={userSearch} onChange={e => setUserSearch(e.target.value)}
                type="text" placeholder="Search name or email" className="input input-bordered w-full" />
              <select value={userFilterRole} onChange={e => setUserFilterRole(e.target.value)}
                className="select select-bordered w-full">
                {roles.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              <select value={userSort} onChange={e => setUserSort(e.target.value)}
                className="select select-bordered w-full">
                <option value="nameAsc">Name ↑</option>
                <option value="nameDesc">Name ↓</option>
                <option value="idAsc">ID ↑</option>
                <option value="idDesc">ID ↓</option>
              </select>
            </aside>
            <main className="flex-1 space-y-4">
              {filteredUsers.map(u => (
                <div key={u.id} className="bg-white p-4 rounded-lg shadow flex justify-between items-center">
                  <div>
                    <p className="font-semibold">{u.fullName} ({u.role})</p>
                    <p className="text-sm text-gray-500">{u.emailAddress}</p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      className={`btn btn-sm ${u.locked ? 'btn-success' : 'btn-warning'}`}
                      onClick={() => handleLockToggle(u)}
                    >{u.locked ? 'Unlock' : 'Lock'}</button>
                    <button className="btn btn-error btn-sm"
                      onClick={() => confirmDeleteUser(u)}
                    >Delete</button>
                  </div>
                </div>
              ))}

              {/* Delete User Confirm Modal */}
              {userDeleteConfirm && (
                <div className="modal modal-open">
                  <div className="modal-box">
                    <h3 className="font-bold text-lg">Delete User?</h3>
                    <p className="mb-4">Are you sure you want to delete {userDeleteConfirm.fullName}?</p>
                    <div className="modal-action">
                      <button className="btn" onClick={cancelDeleteUser}>Cancel</button>
                      <button className="btn btn-error" onClick={handleDeleteUser}>Delete</button>
                    </div>
                  </div>
                </div>
              )}
            </main>
          </div>
      )
      }

      {/* Orders Panel */}
      {
        view === 'orders' && (
          loadingOrders
            ? <p>Loading orders...</p>
            : <div className="space-y-4">
              <aside className="w-1/4 bg-base-100 p-4 rounded-lg shadow space-y-4">
                <input value={orderSearch} onChange={e => setOrderSearch(e.target.value)}
                  placeholder="Search..." className="input input-bordered w-full" />
                <select value={orderSort} onChange={e => setOrderSort(e.target.value)}
                  className="select select-bordered w-full">
                  <option value="dateDesc">Newest</option>
                  <option value="dateAsc">Oldest</option>
                  <option value="idAsc">ID ↑</option>
                  <option value="idDesc">ID ↓</option>
                </select>
                <select
                  value={filterStatus}
                  onChange={e => setFilterStatus(e.target.value)}
                  className="select select-bordered w-full"
                >
                  <option value="">All Statuses</option>
                  {OrderStatuses.map(s => (
                    <option key={s} value={s}>{StatusLabels[s]}</option>
                  ))}
                </select>
              </aside>
              <main className="flex-1 space-y-4">
                {filteredOrders.map(o => (
                  <div
                    key={o.id}
                    className="bg-white p-4 rounded-lg shadow flex justify-between items-center cursor-pointer"
                    onClick={() => openOrderModal(o.id)}
                  >
                    <div>
                      <p className="font-semibold">Order #{o.id}</p>
                      <p className="text-sm text-gray-500">{o.restaurant.name}</p>
                    </div>
                    <div className="flex items-center space-x-4">
                      {/* Státusz váltó select */}
                      <select
                        value={o.status}
                        onChange={e => handleStatusChange(o, e.target.value)}
                        onClick={e => e.stopPropagation()}
                        className="select select-bordered select-sm"
                      >
                        {OrderStatuses.map(s => (
                          <option key={s} value={s}>{StatusLabels[s]}</option>
                        ))}
                      </select>

                      {/* Aktuális státusz badge */}
                      <span className={StatusClasses[o.status]}>
                        {StatusLabels[o.status]}
                      </span>
                    </div>
                  </div>
                ))}

                {filteredOrders.length === 0 && (
                  <p className="text-gray-500">No orders found.</p>
                )}
              </main>
            </div>
        )
      }

      {orderModalOpen && selectedOrder && (
        <div className="modal modal-open">
          <div className="modal-box max-w-3xl w-full relative">
            <button
              className="btn btn-sm btn-circle absolute right-2 top-2"
              onClick={closeOrderModal}
            >✕</button>

            <h2 className="text-2xl font-bold mb-2">
              Order #{selectedOrder.id}
            </h2>
            <p className="text-sm text-gray-500 mb-4">
              Date: {new Date(selectedOrder.orderDate).toLocaleString()}
            </p>
            <div className="flex flex-wrap gap-4 mb-4">

              {/* Étterem */}
              {selectedOrder.restaurant && (
                <p>
                  From:&nbsp;
                  <Link to={`/restaurants/${selectedOrder.restaurant.id}`} className="font-medium text-blue-600 hover:underline">
                    {selectedOrder.restaurant.name}
                  </Link>
                </p>
              )}
              <span className={StatusClasses[selectedOrder.status]}>
                {StatusLabels[selectedOrder.status]}
              </span>
            </div>

            {/* Billing & Shipping */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
              <div className="bg-base-100 p-4 rounded-lg">
                <h3 className="font-semibold mb-2">Billing</h3>
                <p>{selectedOrder.billingInformation.fullName}</p>
                <p>{selectedOrder.billingInformation.phoneNumber}</p>
                <p>
                  {[
                    selectedOrder.billingInformation.address.streetNumber,
                    selectedOrder.billingInformation.address.addressLine,
                    selectedOrder.billingInformation.address.city,
                    selectedOrder.billingInformation.address.postalCode,
                    selectedOrder.billingInformation.address.region
                  ].filter(Boolean).join(", ")}
                </p>
              </div>
              <div className="bg-base-100 p-4 rounded-lg">
                <h3 className="font-semibold mb-2">Shipping</h3>
                <p>{selectedOrder.shippingInformation.fullName}</p>
                <p>{selectedOrder.shippingInformation.phoneNumber}</p>
                <p>
                  {[
                    selectedOrder.shippingInformation.address.streetNumber,
                    selectedOrder.shippingInformation.address.addressLine,
                    selectedOrder.shippingInformation.address.city,
                    selectedOrder.shippingInformation.address.postalCode,
                    selectedOrder.shippingInformation.address.region
                  ].filter(Boolean).join(", ")}
                </p>
              </div>
            </div>

            {/* Items */}
            <div className="overflow-x-auto mb-4">
              <table className="table w-full">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th className="text-right">Qty</th>
                    <th className="text-right">Unit Price</th>
                    <th className="text-right">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedOrder.orderItems.map(item => (
                    <tr key={item.id}>
                      <td>{item.product.name}</td>
                      <td className="text-right">{item.quantity}</td>
                      <td className="text-right">${item.price.toFixed(2)}</td>
                      <td className="text-right">${(item.price * item.quantity).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Coupon */}
            {selectedOrder.coupon && (
              <div className="mb-4 p-4 bg-green-50 rounded-lg">
                <strong>Coupon:</strong> {selectedOrder.coupon.code} —{" "}
                {selectedOrder.coupon.type === "FREE_SHIP"
                  ? "Free Shipping"
                  : `$${selectedOrder.coupon.value.toFixed(2)} off`}
              </div>
            )}

            {/* Summary */}
            <div className="bg-base-200 p-4 rounded-lg text-right space-y-1">
              <p className="flex justify-between">
                <span>Items Total:</span>
                <span>${selectedOrder.orderItems
                  .reduce((acc, i) => acc + i.price * i.quantity, 0)
                  .toFixed(2)}
                </span>
              </p>
              {selectedOrder.coupon?.type === "AMOUNT" && (
                <p className="flex justify-between text-green-600">
                  <span>Coupon:</span>
                  <span>-${selectedOrder.coupon.value.toFixed(2)}</span>
                </p>
              )}
              <p className="flex justify-between">
                <span>Delivery Fee:</span>
                <span>${selectedOrder.restaurant.deliveryFee.toFixed(2)}</span>
              </p>
              <hr />
              <p className="flex justify-between font-bold text-lg">
                <span>Total Paid:</span>
                <span>${selectedOrder.totalAmount.toFixed(2)}</span>
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Pending Restaurants Panel */}
      {
        view === 'restaurants' && (
          loadingPending
            ? <p>Loading...</p>
            : <div className="space-y-4">
              {pending.map(r => (
                <div key={r.id} className="bg-white p-4 rounded-lg shadow flex justify-between items-center">
                  <img src={
                    r.logo?.downloadUrl
                      ? `http://localhost:8080${r.logo.downloadUrl}`
                      : imageNotFound
                  } alt={`${r.name} logo`} className="w-16 h-16 object-cover rounded-full"
                    onError={(e) => {
                      e.currentTarget.onerror = null; // végtelen ciklus elkerülése
                      e.currentTarget.src = imageNotFound;
                    }} />
                  <div className="flex-1 ml-6">
                    <p className="font-semibold">{r.name}</p>
                    <p className="text-sm text-gray-500">Owner: {r.ownerName}</p>
                  </div>
                  <div className="flex gap-2">
                    <button className="btn btn-primary btn-sm" onClick={() => setPendingDetail(r)}>View</button>
                    <button className="btn btn-success btn-sm" onClick={() => confirmPendingAction(r, 'approve')}>Approve</button>
                    <button className="btn btn-error btn-sm" onClick={() => confirmPendingAction(r, 'reject')}>Reject</button>
                  </div>
                </div>
              ))}

              {/* Detail Modal */}
              {pendingDetail && (
                <div className="modal modal-open">
                  <div className="modal-box">
                    <div className="flex items-center gap-4 mb-2">
                      <img src={
                        pendingDetail.logo?.downloadUrl
                          ? `http://localhost:8080${pendingDetail.logo.downloadUrl}`
                          : imageNotFound
                      } alt={`${pendingDetail.name} logo`}
                        className="w-10 h-10 object-cover rounded-full"
                        onError={(e) => {
                          e.currentTarget.onerror = null; // végtelen ciklus elkerülése
                          e.currentTarget.src = imageNotFound;
                        }} />
                      <h3 className="font-bold text-lg">{pendingDetail.name}</h3>
                    </div>
                    <p>Owner: {pendingDetail.ownerName}</p>
                    <p>Email address: {pendingDetail.emailAddress}</p>
                    <p>Phone number: {pendingDetail.phoneNumber}</p>
                    <p>Applied On: {pendingDetail.createdAt}</p>
                    <p className="mt-2">{pendingDetail.details}</p>
                    <div className="modal-action">
                      <button className="btn btn-success" onClick={() => {
                        setPendingDetail(null);
                        setPendingActionConfirm({ r: pendingDetail, action: 'approve' });
                      }}>Approve</button>
                      <button className="btn btn-error" onClick={() => {
                        setPendingDetail(null);
                        setPendingActionConfirm({ r: pendingDetail, action: 'reject' });
                      }}>Reject</button>
                      <button className="btn" onClick={() => setPendingDetail(null)}>Close</button>
                    </div>
                  </div>
                </div>
              )}

              {/* Approve/Reject Confirm Modal */}
              {pendingActionConfirm && (
                <div className="modal modal-open">
                  <div className="modal-box">
                    <h3 className="font-bold text-lg">{pendingActionConfirm.action === 'approve' ? 'Approve' : 'Reject'} Restaurant?</h3>
                    <p className="mb-4">Are you sure you want to {pendingActionConfirm.action} "{pendingActionConfirm.r.name}"?</p>
                    <div className="modal-action">
                      <button className="btn" onClick={cancelPendingAction}>Cancel</button>
                      <button className={`btn btn-${pendingActionConfirm.action === 'approve' ? 'success' : 'error'}`} onClick={pendingActionConfirm.action === 'approve' ? handleApprove : handleReject}>
                        Yes, {pendingActionConfirm.action}
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
        )
      }

      {/* Coupons Panel */}
      {
        view === 'coupons' && (
          loadingCoupons
            ? <p>Loading coupons...</p>
            : <div className="flex gap-6">
              <aside className="w-1/4 bg-base-100 p-4 rounded-lg shadow space-y-4">
                <input value={couponSearch} onChange={e => setCouponSearch(e.target.value)}
                  placeholder="Search code..." className="input input-bordered flex-1" />
                <select value={couponSort} onChange={e => setCouponSort(e.target.value)}
                  className="select select-bordered">
                  <option value="codeAsc">Code ↑</option>
                  <option value="codeDesc">Code ↓</option>
                  <option value="expAsc">Expires ↑</option>
                  <option value="expDesc">Expires ↓</option>
                </select>
                <button className="btn btn-primary" onClick={() => setCouponModal({ id: null, code: '', type: 'AMOUNT', value: 0, expirationDate: '' })}>
                  New Coupon
                </button>
              </aside>
              <main className="flex-1 space-y-4">
                {filteredCoupons.map(c => (
                  <div key={c.id} className="card bg-base-100 shadow p-4 flex justify-between">
                    <div>
                      <p className="font-semibold">{c.code}</p>
                      <p className="text-sm text-gray-500">Expires: {c.expirationDate}</p>
                    </div>
                    <div className="flex gap-2">
                      <button className="btn btn-outline btn-sm" onClick={() => setCouponModal(c)}>Edit</button>
                      <button className="btn btn-error btn-sm" onClick={() => confirmDeleteCoupon(c)}>Delete</button>
                    </div>
                  </div>
                ))}

                {/* Coupon Modal */}
                {couponModal && (
                  <div className="modal modal-open">
                    <div className="modal-box max-w-lg">
                      <h3 className="font-bold text-lg mb-4">{couponModal.id ? 'Edit' : 'New'} Coupon</h3>
                      <input value={couponModal.code} onChange={e => setCouponModal({ ...couponModal, code: e.target.value })} placeholder="Code" className="input input-bordered w-full mb-2" />
                      <select value={couponModal.type} onChange={e => setCouponModal({ ...couponModal, type: e.target.value })} className="select select-bordered w-full mb-2">
                        <option value="AMOUNT">Amount</option>
                        <option value="FREE_SHIP">Free Shipping</option>
                      </select>
                      {couponModal.type === 'AMOUNT' && (
                        <input type="number" value={couponModal.value} onChange={e => setCouponModal({ ...couponModal, value: parseFloat(e.target.value) })} placeholder="Value" className="input input-bordered w-full mb-2" />
                      )}
                      <input type="date" min={today} value={couponModal.expirationDate} onChange={e => setCouponModal({ ...couponModal, expirationDate: e.target.value })} className="input input-bordered w-full mb-4" />
                      <div className="modal-action">
                        <button className="btn" onClick={() => setCouponModal(null)}>Cancel</button>
                        <button className="btn btn-primary" onClick={() => {
                          if (couponModal.expirationDate < today) {
                            toast.error("Expiration date cannot be in the past");
                            return;
                          }
                          handleSaveCoupon(couponModal)
                        }}>Save</button>
                      </div>
                    </div>
                  </div>
                )}

                {/* Delete Coupon Confirm Modal */}
                {couponDeleteConfirm && (
                  <div className="modal modal-open">
                    <div className="modal-box">
                      <h3 className="font-bold text-lg">Delete Coupon?</h3>
                      <p className="mb-4">Are you sure you want to delete {couponDeleteConfirm.code}?</p>
                      <div className="modal-action">
                        <button className="btn" onClick={cancelDeleteCoupon}>Cancel</button>
                        <button className="btn btn-error" onClick={handleDeleteCoupon}>Delete</button>
                      </div>
                    </div>
                  </div>
                )}
              </main>
            </div>
        )
      }
    </div >
  );
};