import React, { useState, useMemo, useEffect, useContext } from "react";
import CartItem from "../components/CartItem";
import { useCart } from "../contexts/CartContext";
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { Link } from "react-router-dom";
import { validateCoupon } from "../api/couponService";
import RewardRedemption from "../components/RewardRedemption";
import LoadingSpinner from '../components/LoadingSpinner';
import { AuthContext } from "../contexts/AuthContext";
import { rewardApi } from "../api/rewardApi";

const Cart = () => {
  const { user } = useContext(AuthContext);
  const {
    items,
    total,
    loading,
    updateItem,
    removeItem,
    clear: clearCart,
    applyCoupon,
    pointsToRedeem,
    setPoints
  } = useCart();

  const [couponCode, setCouponCode] = useState("");
  const [appliedCoupon, setAppliedCoupon] = useState(null);      // a CouponDto objektum
  const [pendingCoupon, setPendingCoupon] = useState("");        // csak a kód
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [userBalance, setUserBalance] = useState(0);

  useEffect(() => {
    if (user?.id) {
      const fetchBalance = async () => {
        try {
          const response = await rewardApi.getRewardBalance(user.id);
          const data = response.data?.data || response.data || {};
          setUserBalance(data.balance || 0);
        } catch (error) {
          console.error('Error fetching balance:', error);
        }
      };
      fetchBalance();
    }
  }, [user]);

  // A kosárból az első tétel éttermének díja
  const baseDeliveryFee = useMemo(() => {
    if (!items.length) return 0;
    return items[0].restaurant?.deliveryFee ?? 0;
  }, [items]);

  // Ha FREE_SHIP kupon érvényben, díj = 0
  const effectiveDeliveryFee = appliedCoupon?.type === "FREE_SHIP"
    ? 0
    : baseDeliveryFee;

  // Általános kupon alkalmazó logika
  const couponApplied = async (code) => {
    const resCoupon = await validateCoupon(code);
    if (!resCoupon) return false;
    setAppliedCoupon(resCoupon);
    applyCoupon(resCoupon);
    toast.success(
      resCoupon.type === "FREE_SHIP"
        ? "Free shipping applied!"
        : `$${resCoupon.value.toFixed(2)} off applied!`
    );
    return true;
  };

  const handleApplyClick = async () => {
    const code = couponCode.trim().toUpperCase();
    if (!code) return;
    if (appliedCoupon && appliedCoupon.code !== code) {
      setPendingCoupon(code);
      setShowConfirmModal(true);
    } else {
      await couponApplied(code);
    }
  };

  const confirmApply = async () => {
    setShowConfirmModal(false);
    const ok = await couponApplied(pendingCoupon);
    if (ok) setCouponCode(pendingCoupon);
    setPendingCoupon("");
  };

  const cancelApply = () => {
    setPendingCoupon("");
    setShowConfirmModal(false);
    setCouponCode(appliedCoupon?.code || "");
  };

  const handleQuantityChange = (item, qty) => {
    if (qty <= 0) {
      removeItem(item.id);
    } else if (qty <= item.inventory) {
      updateItem(item.id, qty);
    } else {
      toast.error(`Maximum available quantity is ${item.inventory}`);
    }
  };

  const handleRemove = (id) => removeItem(id);

  // Kedvezmény értéke forintban (csak AMOUNT)
  const discountValue = appliedCoupon?.type === "AMOUNT"
    ? appliedCoupon.value
    : 0;

  // Reward points discount
  const rewardDiscount = pointsToRedeem / 10;

  // Végösszegek
  const subTotal = items.length > 0 ? total : 0;
  const totalAfterDiscount = Math.max(0, subTotal - discountValue - rewardDiscount);
  const grandTotal = totalAfterDiscount + effectiveDeliveryFee;

  if (loading) {
    return <LoadingSpinner text="Loading cart..." />;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Your Cart</h1>
      <div className="flex flex-col lg:flex-row gap-8">

        {/* Cart tételek */}
        <div className="flex-1 space-y-4">
          {items.length > 0 ? items.map(item => (
            <CartItem
              key={item.id}
              image={item.image}
              name={item.name}
              price={item.price}
              quantity={item.quantity}
              onQuantityChange={qty => handleQuantityChange(item, qty)}
              onRemove={() => handleRemove(item.id)}
            />
          )) : (
            <p className="text-center text-gray-500">Your cart is empty.</p>
          )}
        </div>

        {/* Összegzés panel */}
        <aside className="w-full lg:w-1/3 bg-base-200 p-6 rounded-lg shadow-lg space-y-6">
          <h2 className="text-xl font-semibold">Order Summary</h2>

          <ul className="space-y-2">
            {items.map(item => (
              <li key={item.id} className="flex justify-between">
                <span>{item.name} x {item.quantity}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </li>
            ))}
          </ul>

          <div className="flex justify-between font-medium">
            <span>Subtotal:</span>
            <span>${subTotal.toFixed(2)}</span>
          </div>

          <div className="flex justify-between">
            <span>Delivery Fee:</span>
            <span>${effectiveDeliveryFee.toFixed(2)}</span>
          </div>

          {/* Reward Points */}
          {items.length > 0 && (
            <RewardRedemption
              orderTotal={subTotal}
              onPointsChange={setPoints}
            />
          )}

          {/* Kupon mező */}
          <div>
            <label className="block font-medium mb-2">Coupon Code</label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Enter code"
                value={couponCode}
                onChange={e => setCouponCode(e.target.value)}
                className="input input-bordered flex-1"
              />
              <button className="btn btn-primary" onClick={handleApplyClick}>
                Apply
              </button>
            </div>
            {appliedCoupon && (
              <p className="mt-2 text-green-700">
                Applied: <strong>{appliedCoupon.code}</strong> —{" "}
                {appliedCoupon.type === "FREE_SHIP"
                  ? "Free Shipping"
                  : `$${appliedCoupon.value.toFixed(2)} off`}
              </p>
            )}
          </div>

          {/* Kedvezmény sorok */}
          {discountValue > 0 && (
            <div className="flex justify-between text-green-600 font-medium">
              <span>Coupon Discount:</span>
              <span>-${discountValue.toFixed(2)}</span>
            </div>
          )}
          {rewardDiscount > 0 && (
            <div className="flex justify-between text-blue-600 font-medium">
              <span>Reward Points ({pointsToRedeem} pts):</span>
              <span>-${rewardDiscount.toFixed(2)}</span>
            </div>
          )}

          {/* Végösszeg */}
          <div className="flex justify-between font-bold text-lg">
            <span>Total:</span>
            <span>${grandTotal.toFixed(2)}</span>
          </div>

          {/* Checkout */}
          <div>
            {(() => {
              const isCartEmpty = items.length === 0;
              const isPointsInvalid = pointsToRedeem > 0 && (pointsToRedeem < 25 || pointsToRedeem < 0);
              const hasInsufficientPoints = pointsToRedeem > userBalance;
              const isDisabled = isCartEmpty || isPointsInvalid || hasInsufficientPoints;
              
              return (
                <>
                  <Link to={!isDisabled ? "/checkout" : "#"} className={isDisabled ? "pointer-events-none" : ""}>
                    <button 
                      className={`btn w-full ${
                        !isDisabled
                          ? "btn-accent" 
                          : "btn-disabled bg-gray-300 text-gray-500 cursor-not-allowed"
                      }`}
                      disabled={isDisabled}
                    >
                      Proceed to Checkout
                    </button>
                  </Link>
                  {isCartEmpty && <p className="text-error text-sm mt-2">Your cart is empty</p>}
                  {pointsToRedeem > 0 && pointsToRedeem < 25 && <p className="text-error text-sm mt-2">Minimum 25 points required</p>}
                  {pointsToRedeem < 0 && <p className="text-error text-sm mt-2">Points cannot be negative</p>}
                  {hasInsufficientPoints && <p className="text-error text-sm mt-2">You only have {userBalance} points available</p>}
                </>
              );
            })()} 
          </div>

          {/* Clear Cart */}
          {items.length > 0 && (
            <button className="btn btn-outline w-full mt-4" onClick={clearCart}>
              Clear Cart
            </button>
          )}
        </aside>
      </div>

      {/* Replace Coupon Confirm Modal */}
      {showConfirmModal && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-lg mb-4">Replace Coupon?</h3>
            <p className="mb-6">
              You already have <strong>{appliedCoupon.code}</strong> applied.<br />
              Replace with <strong>{pendingCoupon}</strong>?
            </p>
            <div className="modal-action">
              <button className="btn" onClick={cancelApply}>Cancel</button>
              <button className="btn btn-primary" onClick={confirmApply}>Confirm</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;
