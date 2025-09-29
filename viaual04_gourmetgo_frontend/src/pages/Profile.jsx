import React, { useContext, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { AuthContext } from '../contexts/AuthContext';
import { toast } from 'react-toastify';
import { requestPasswordReset, updateProfile } from '../api/userService';

const profileSchema = yup.object().shape({
  fullName: yup.string().required('Full name is required'),
  email: yup.string().email('Invalid email address').required('Email is required'),
});

const Profile = () => {
  const { user, logout } = useContext(AuthContext);

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty, isSubmitting },
    reset,
  } = useForm({
    resolver: yupResolver(profileSchema),
  });

  // 🔄 Reset the form values when user data is available
  useEffect(() => {
    if (user) {
      reset({
        fullName: user.fullName || '',
        email: user.emailAddress || '',
      });
    }
  }, [user, reset]);

  const onSubmit = async (data) => {
    try {
      const updated = await updateProfile({
        fullName: data.fullName,
        emailAddress: data.email,
      });
      toast.success('Profile updated successfully');
      if (updated !== null) {
        window.location.reload();
      }
    } catch {
      // toast az updateProfile belül már kilövésre került
    }
  };

  const handlePasswordReset = async () => {
    try {
      await requestPasswordReset(user.emailAddress);
      toast.info('Password reset email sent');
      logout();
    } catch {
      // hibaüzenet a service-ben
    }
  };

  if (!user) {
    return <div className="text-center mt-10">Loading profile...</div>;
  }

  return (
    <div className="container mx-auto p-4">
      <div className="card bg-base-100 shadow-xl max-w-xl mx-auto">
        <div className="card-body">
          <h2 className="card-title mb-4 text-2xl">My Profile</h2>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label htmlFor="fullName" className="label">
                <span className="label-text">Full Name</span>
              </label>
              <input
                id="fullName"
                type="text"
                {...register('fullName')}
                className={`input input-bordered w-full ${errors.fullName ? 'input-error' : ''}`}
              />
              {errors.fullName && <p className="text-sm text-error mt-1">{errors.fullName.message}</p>}
            </div>

            <div>
              <label htmlFor="email" className="label">
                <span className="label-text">Email Address</span>
              </label>
              <input
                id="email"
                type="email"
                {...register('email')}
                className={`input input-bordered w-full ${errors.email ? 'input-error' : ''}`}
              />
              {errors.email && <p className="text-sm text-error mt-1">{errors.email.message}</p>}
            </div>

            <button
              type="submit"
              className="btn btn-primary w-full"
              disabled={!isDirty || isSubmitting}
            >
              {isSubmitting ? 'Saving...' : 'Save Changes'}
            </button>
          </form>

          <div className="divider" />

          <div className="space-y-2">
            <p>
              <span className="font-semibold">Member since:</span>{' '}
              {new Date(user.createdAt).toLocaleDateString()}
            </p>
            <p>
              <span className="font-semibold">Loyalty Points:</span>{' '}
              <span className="badge badge-sm badge-primary">Soon to be implemented</span>
            </p>
          </div>

          <div className="divider" />

          <button
            onClick={handlePasswordReset}
            className="btn btn-secondary w-full"
          >
            Request Password Change
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile;
