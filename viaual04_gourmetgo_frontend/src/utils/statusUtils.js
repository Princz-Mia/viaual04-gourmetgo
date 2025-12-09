export const OrderStatuses = [
  "PENDING",
  "CONFIRMED",
  "PREPARING",
  "READY_FOR_PICKUP",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "CANCELLED",
  "COMPENSATED",
];

export const StatusLabels = {
  PENDING: "Pending",
  CONFIRMED: "Confirmed",
  PREPARING: "Preparing",
  READY_FOR_PICKUP: "Ready for Pickup",
  OUT_FOR_DELIVERY: "Out for Delivery",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
  COMPENSATED: "Compensated",
};

export const StatusClasses = {
  PENDING: "badge badge-info text-right w-32",
  CONFIRMED: "badge badge-primary text-right w-32",
  PREPARING: "badge badge-warning text-right w-32",
  READY_FOR_PICKUP: "badge badge-secondary text-right w-32",
  OUT_FOR_DELIVERY: "badge badge-info text-right w-32",
  DELIVERED: "badge badge-success text-right w-32",
  CANCELLED: "badge badge-error text-right w-32",
  COMPENSATED: "badge badge-accent text-right w-32",
};
