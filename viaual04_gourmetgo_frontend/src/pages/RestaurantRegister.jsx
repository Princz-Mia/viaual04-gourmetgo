import React, { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

// Validation schema for restaurant registration
const openingHoursShape = {
    open: yup.string().required('Opening time is required'),
    close: yup.string().required('Closing time is required'),
};
const schema = yup.object().shape({
    name: yup.string().required('Restaurant name is required'),
    phone: yup.string().required('Phone number is required'),
    ownerName: yup.string().required("Owner's name is required"),
    category: yup.string().required('Category is required'),
    region: yup.string().required('Region is required'),
    postalCode: yup.string().required('Postal code is required'),
    city: yup.string().required('City is required'),
    addressLine: yup.string().required('Address line is required'),
    streetNumber: yup.string().required('Street number is required'),
    logo: yup.mixed().required('Restaurant logo is required'),
    openingHours: yup.object().shape({
        Monday: yup.object().shape(openingHoursShape),
        Tuesday: yup.object().shape(openingHoursShape),
        Wednesday: yup.object().shape(openingHoursShape),
        Thursday: yup.object().shape(openingHoursShape),
        Friday: yup.object().shape(openingHoursShape),
        Saturday: yup.object().shape(openingHoursShape),
        Sunday: yup.object().shape(openingHoursShape),
    }),
});

const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const RestaurantRegister = () => {
    const navigate = useNavigate();
    const [logoPreview, setLogoPreview] = useState(null);

    const { register, handleSubmit, control, formState: { errors, isSubmitting } } =
        useForm({
            resolver: yupResolver(schema),
            defaultValues: {
                openingHours: days.reduce((acc, day) => {
                    acc[day] = { open: '', close: '' };
                    return acc;
                }, {}),
            }
        });

    const onSubmit = async (data) => {
        const formData = new FormData();
        formData.append('name', data.name);
        formData.append('phone', data.phone);
        formData.append('ownerName', data.ownerName);
        formData.append('category', data.category);
        formData.append('region', data.region);
        formData.append('postalCode', data.postalCode);
        formData.append('city', data.city);
        formData.append('addressLine', data.addressLine);
        formData.append('streetNumber', data.streetNumber);
        formData.append('logo', data.logo[0]);
        formData.append('openingHours', JSON.stringify(data.openingHours));

        try {
            //await registerRestaurant(formData);
            toast.success('Restaurant registered successfully!');
            navigate('/login');
        } catch (err) {
            toast.error('Registration failed. Please try again.');
            console.error(err);
        }
    };

    const handleLogoChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setLogoPreview(URL.createObjectURL(file));
        }
    };

    return (
        <div className="container mx-auto p-4">
            <div className="card bg-base-100 shadow-xl max-w-2xl mx-auto">
                <div className="card-body">
                    <h2 className="card-title text-2xl mb-4">Restaurant Registration</h2>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">

                        {/* Logo Upload */}
                        <div className="form-control">
                            <label className="label">
                                <span className="label-text">Restaurant Logo</span>
                            </label>
                            <input
                                type="file"
                                accept="image/*"
                                {...register('logo')}
                                onChange={(e) => { register('logo').onChange(e); handleLogoChange(e); }}
                                className="file-input file-input-bordered file-input-primary w-full"
                            />
                            {logoPreview && <img src={logoPreview} alt="Logo preview" className="mt-2 w-32 h-32 object-cover rounded" />}
                            {errors.logo && <span className="text-sm text-error">{errors.logo.message}</span>}
                        </div>

                        {/* Basic Info */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div className="form-control">
                                <label className="label"><span className="label-text">Name</span></label>
                                <input type="text" {...register('name')} placeholder="Restaurant Name" className="input input-bordered" />
                                {errors.name && <span className="text-sm text-error">{errors.name.message}</span>}
                            </div>
                            <div className="form-control">
                                <label className="label"><span className="label-text">Phone</span></label>
                                <input type="tel" {...register('phone')} placeholder="Phone Number" className="input input-bordered" />
                                {errors.phone && <span className="text-sm text-error">{errors.phone.message}</span>}
                            </div>
                            <div className="form-control">
                                <label className="label"><span className="label-text">Owner Name</span></label>
                                <input type="text" {...register('ownerName')} placeholder="Owner's Name" className="input input-bordered" />
                                {errors.ownerName && <span className="text-sm text-error">{errors.ownerName.message}</span>}
                            </div>
                            <div className="form-control">
                                <label className="label"><span className="label-text">Category</span></label>
                                <input type="text" {...register('category')} placeholder="Cuisine Category" className="input input-bordered" />
                                {errors.category && <span className="text-sm text-error">{errors.category.message}</span>}
                            </div>
                        </div>

                        {/* Opening Hours */}
                        <div>
                            <h3 className="font-semibold mb-2">Opening Hours</h3>
                            <p className="text-sm text-gray-500 mb-4">If the restaurant is closed on a particular day, set the opening and closing times to the exact same value (e.g., 00:00 - 00:00) to indicate it remains closed.</p>
                            <div className="space-y-2">
                                {days.map(day => (
                                    <div key={day} className="flex items-center gap-4">
                                        <span className="w-24 font-medium">{day}</span>
                                        <Controller
                                            name={`openingHours.${day}.open`}
                                            control={control}
                                            render={({ field }) => (
                                                <input type="time" {...field} className="input input-bordered w-32" />
                                            )}
                                        />
                                        <span>to</span>
                                        <Controller
                                            name={`openingHours.${day}.close`}
                                            control={control}
                                            render={({ field }) => (
                                                <input type="time" {...field} className="input input-bordered w-32" />
                                            )}
                                        />
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Address */}
                        <div>
                            <h3 className="font-semibold mb-2">Address</h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div className="form-control">
                                    <label className="label"><span className="label-text">Region</span></label>
                                    <input type="text" {...register('region')} placeholder="Region" className="input input-bordered" />
                                    {errors.region && <span className="text-sm text-error">{errors.region.message}</span>}
                                </div>
                                <div className="form-control">
                                    <label className="label"><span className="label-text">Postal Code</span></label>
                                    <input type="text" {...register('postalCode')} placeholder="Postal Code" className="input input-bordered" />
                                    {errors.postalCode && <span className="text-sm text-error">{errors.postalCode.message}</span>}
                                </div>
                                <div className="form-control">
                                    <label className="label"><span className="label-text">City</span></label>
                                    <input type="text" {...register('city')} placeholder="City" className="input input-bordered" />
                                    {errors.city && <span className="text-sm text-error">{errors.city.message}</span>}
                                </div>
                                <div className="form-control">
                                    <label className="label"><span className="label-text">Address Line</span></label>
                                    <input type="text" {...register('addressLine')} placeholder="Address Line" className="input input-bordered" />
                                    {errors.addressLine && <span className="text-sm text-error">{errors.addressLine.message}</span>}
                                </div>
                                <div className="form-control sm:col-span-2">
                                    <label className="label"><span className="label-text">Street Number</span></label>
                                    <input type="text" {...register('streetNumber')} placeholder="Street Number" className="input input-bordered w-full" />
                                    {errors.streetNumber && <span className="text-sm text-error">{errors.streetNumber.message}</span>}
                                </div>
                            </div>
                        </div>

                        <button type="submit" className="btn btn-primary w-full" disabled={isSubmitting}>
                            {isSubmitting ? 'Registering...' : 'Register Restaurant'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default RestaurantRegister;