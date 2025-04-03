import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { verify } from '../api/authService';

const Verification = () => {
  const [searchParams] = useSearchParams();
  const key = searchParams.get('key');

  useEffect(() => {
    if (key) {
      verify(key).then((response) => {
        if (response) {
          toast.success('Account verified successfully!');
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

export default Verification;
