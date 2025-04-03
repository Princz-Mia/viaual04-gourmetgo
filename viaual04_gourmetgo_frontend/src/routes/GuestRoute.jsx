import { Navigate } from 'react-router-dom';

// This component allows access only if the user is not logged in
const GuestRoute = ({ children }) => {
  const user = localStorage.getItem('user');
  if (user) {
    return <Navigate to="/" />;
  }
  return children;
};

export default GuestRoute;
