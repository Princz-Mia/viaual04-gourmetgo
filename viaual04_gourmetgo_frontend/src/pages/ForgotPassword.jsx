import React from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';

// Validation schema for forgot password form
const schema = yup.object().shape({
  email: yup.string().email('Invalid email address').required('Email is required'),
});

export default function ForgotPassword() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: yupResolver(schema)
  });

  const onSubmit = async (data) => {
    try {
      //await requestPasswordReset(data.email);
      toast.success('Password reset email sent. Please check your inbox.');
    } catch (err) {
      console.error(err);
      toast.error('Failed to send reset email. Please try again later.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="card w-full max-w-md bg-base-100 shadow-lg">
        <div className="card-body">
          <h2 className="card-title text-2xl mb-4">Forgot Password</h2>
          <p className="mb-4 text-gray-600">Enter your registered email address to receive a password reset link.</p>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="form-control">
              <label htmlFor="email" className="label">
                <span className="label-text">Email Address</span>
              </label>
              <input
                id="email"
                type="email"
                {...register('email')}
                className={`input input-bordered w-full ${errors.email ? 'input-error' : ''}`}
                placeholder="you@example.com"
              />
              {errors.email && <p className="text-sm text-error mt-1">{errors.email.message}</p>}
            </div>

            <button
              type="submit"
              className="btn btn-primary w-full"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Sending...' : 'Send Reset Link'}
            </button>
          </form>
          <div className="divider">or</div>
          <div className="text-center">
            <Link to="/login" className="text-blue-500 hover:underline">
              Back to Login
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}