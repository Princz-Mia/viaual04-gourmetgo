import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

// Validation schema: password and confirm must match
const schema = yup.object().shape({
  password: yup.string()
    .min(8, 'Password must be at least 8 characters')
    .required('New password is required'),
  confirmPassword: yup.string()
    .oneOf([yup.ref('password')], 'Passwords must match')
    .required('Please confirm your password'),
});

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors, isSubmitting } } =
    useForm({ resolver: yupResolver(schema) });

  useEffect(() => {
    if (!token) {
      toast.error('Password reset token is missing');
      navigate('/forgot-password');
    }
  }, [token, navigate]);

  const onSubmit = async (data) => {
    try {
      //await resetPassword({ token, newPassword: data.password });
      toast.success('Password has been reset. Please log in with your new password.');
      navigate('/login');
    } catch (err) {
      console.error(err);
      toast.error(
        err.response?.data?.message || 'Failed to reset password, please try again.'
      );
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="card w-full max-w-md bg-base-100 shadow-lg">
        <div className="card-body">
          <h2 className="card-title text-2xl mb-4">Reset Password</h2>
          <p className="mb-4 text-gray-600">
            Enter your new password below. Both fields must match.
          </p>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="form-control">
              <label htmlFor="password" className="label">
                <span className="label-text">New Password</span>
              </label>
              <input
                id="password"
                type="password"
                {...register('password')}
                className={`input input-bordered w-full ${errors.password ? 'input-error' : ''}`}
                placeholder="********"
              />
              {errors.password && (
                <p className="text-sm text-error mt-1">{errors.password.message}</p>
              )}
            </div>

            <div className="form-control">
              <label htmlFor="confirmPassword" className="label">
                <span className="label-text">Confirm Password</span>
              </label>
              <input
                id="confirmPassword"
                type="password"
                {...register('confirmPassword')}
                className={`input input-bordered w-full ${errors.confirmPassword ? 'input-error' : ''}`}
                placeholder="********"
              />
              {errors.confirmPassword && (
                <p className="text-sm text-error mt-1">{errors.confirmPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              className="btn btn-primary w-full"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Resetting...' : 'Reset Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}