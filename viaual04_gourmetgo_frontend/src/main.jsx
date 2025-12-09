import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import { ToastContainer, Slide } from 'react-toastify';
import './index.css';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider } from './contexts/AuthContext.jsx';
import { CartProvider } from './contexts/CartContext.jsx';
import { ChatProvider } from './contexts/ChatContext.jsx';

createRoot(document.getElementById('root')).render(
  <AuthProvider>
    <CartProvider>
      <ChatProvider>
        <App />
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
        transition={Slide}
      />
      </ChatProvider>
    </CartProvider>
  </AuthProvider>,
);
