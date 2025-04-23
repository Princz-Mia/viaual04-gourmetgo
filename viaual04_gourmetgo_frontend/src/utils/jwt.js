// Simple JWT decoder without external dependencies
export function decodeJwt(token) {
  if (!token) return {};
  try {
    const [, payload] = token.split(".");
    const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decoded);
  } catch {
    return {};
  }
}
