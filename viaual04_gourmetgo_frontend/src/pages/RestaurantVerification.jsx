import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { toast } from 'react-toastify';
import { verify } from '../api/restaurantService';

const schema = yup.object().shape({
    password: yup
        .string()
        .required('Password is required')
        .min(6, 'Password must be at least 6 characters'),
    confirmPassword: yup
        .string()
        .oneOf([yup.ref('password')], 'Passwords must match')
        .required('Please confirm your password'),
});

const RestaurantVerification = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const key = searchParams.get('key');

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
    });

    const onSubmit = async (data) => {
        if (!key) {
            toast.error('Verification key is missing.');
            return;
        }

        try {
            await verify(key, data);
            toast.success('Restaurant account verified successfully!');
            navigate("/login");
        } catch (err) {
            toast.error('Verification failed.');
            console.error(err);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4">
            <div className="w-full max-w-md bg-white rounded-lg shadow-md p-8">
                <h2 className="text-2xl font-bold mb-6 text-center">Verify Restaurant Account</h2>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="mb-4">
                        <label className="block text-gray-700">Password</label>
                        <input
                            type="password"
                            {...register('password')}
                            className={`w-full p-2 border rounded ${errors.password ? 'border-red-500' : 'border-gray-300'
                                }`}
                            placeholder="Password"
                        />
                        {errors.password && (
                            <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>
                        )}
                    </div>

                    <div className="mb-6">
                        <label className="block text-gray-700">Confirm Password</label>
                        <input
                            type="password"
                            {...register('confirmPassword')}
                            className={`w-full p-2 border rounded ${errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
                                }`}
                            placeholder="Confirm Password"
                        />
                        {errors.confirmPassword && (
                            <p className="text-red-500 text-sm mt-1">{errors.confirmPassword.message}</p>
                        )}
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition-colors"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? 'Verifying...' : 'Verify'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default RestaurantVerification;
