// JWT decoding removed for security - tokens are now httpOnly cookies
// User information is fetched from the server instead of decoding client-side
export function decodeJwt(token) {
  console.warn('JWT decoding is deprecated. Use server-side user profile endpoint instead.');
  return {};
}
