import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { verify } from '../api/customerService';

const CustomerVerification = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const key = searchParams.get('key');

  useEffect(() => {
    if (key) {
      verify(key).then((response) => {
        if (response) {
          toast.success('Account verified successfully!');
          navigate('/login');
        }
      });
    }
  }, [key, verify]);

  return (
    <div className="flex items-center justify-center h-screen">
      <p>Verifying your account, please wait...</p>
    </div>
  );
};

export default CustomerVerification;
