import { decodeJwt } from "../utils/jwt";
import axiosInstance from "./axiosConfig";

/**
 * Fetch full profile by decoding the JWT for id+role,
 * then calling the appropriate endpoint.
 */
export async function fetchProfile() {
  const stored = JSON.parse(localStorage.getItem("user") || "{}");
  const { token } = stored;
  if (!token) throw new Error("No auth token found");

  const claims = decodeJwt(token);
  const id = claims.id;
  const role = Array.isArray(claims.role) ? claims.role[0] : claims.role;

  let url;
  switch (role.authority) {
    case "ROLE_CUSTOMER":
      url = `/customers/${id}`;
      break;
    case "ROLE_RESTAURANT":
      url = `/restaurants/${id}`;
      break;
    case "ROLE_ADMIN":
      url = `/admins/${id}`;
      break;
    default:
      throw new Error(`Unknown role: ${role}`);
  }

  const { data } = await axiosInstance.get(url);
  // include id, role, token in the returned object
  return { ...data.data, id, role, token };
}
