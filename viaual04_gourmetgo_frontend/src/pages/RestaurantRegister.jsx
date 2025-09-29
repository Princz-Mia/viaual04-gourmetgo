import React, { useState } from 'react';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { registerRestaurant } from '../api/restaurantService';

// Validation schema for restaurant registration
const openingHoursShape = {
    openingTime: yup.string().required('Opening time is required'),
    closingTime: yup.string().required('Closing time is required'),
};
const schema = yup.object().shape({
    name: yup.string().required('Restaurant name is required'),
    email: yup.string().email('Invalid email address').required('Email is required'),
    phone: yup.string().required('Phone number is required'),
    ownerName: yup.string().required("Owner's name is required"),
    deliveryFee: yup
        .number()
        .typeError('Delivery fee must be a number')
        .positive('Delivery fee must be greater than 0')
        .required('Delivery fee is required'),
    categoryNames: yup.array()
        .of(yup.string().required('Category is required'))
        .min(1, 'At least one category is required'),
    region: yup.string().required('Region is required'),
    postalCode: yup.string().required('Postal code is required'),
    city: yup.string().required('City is required'),
    addressLine: yup.string().required('Address line is required'),
    logo: yup.mixed().required('Restaurant logo is required'),
    openingHours: yup.object().shape(
        Object.fromEntries(
            ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
                .map(day => [day, yup.object().shape(openingHoursShape)])
        )
    ),
});

// JSON PARSE MINDEN LEGYEN NAGY BETŰ MINT SZERVEROLDALON
const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function RestaurantRegister() {
    const navigate = useNavigate();
    const [logoPreview, setLogoPreview] = useState(null);

    const {
        register,
        handleSubmit,
        control,
        formState: { errors, isSubmitting }
    } = useForm({
        resolver: yupResolver(schema),
        defaultValues: {
            deliveryFee: '',
            categoryNames: [''],
            openingHours: days.reduce((acc, day) => {
                acc[day] = { openingTime: '', closingTime: '' };
                return acc;
            }, {}),
        }
    });

    const { fields: categories, append, remove } = useFieldArray({
        control,
        name: 'categoryNames'
    });

    const onSubmit = async (data) => {
        // Prepare DTO payload
        const dto = {
            name: data.name,
            emailAddress: data.email,
            phoneNumber: data.phone,
            ownerName: data.ownerName,
            deliveryFee: parseFloat(data.deliveryFee),
            categoryNames: data.categoryNames,
            address: {
                region: data.region,
                postalCode: data.postalCode,
                city: data.city,
                addressLine: data.addressLine,
            },
            openingHours: Object.fromEntries(
                Object.entries(data.openingHours)
                    .map(([day, hours]) => [day.toUpperCase(), hours])
            )
        };

        console.log("dto:" + dto);

        // Build FormData
        const formData = new FormData();
        //formData.append('data', JSON.stringify(dto));
        //formData.append('logo', data.logo[0]);
        formData.append(
            'data',
            new Blob([JSON.stringify(dto)], { type: 'application/json' })
        );
        formData.append('logo', data.logo[0]);

        for (let [key, value] of formData.entries()) {
            console.log(key, value);
        }

        try {
            const res = await registerRestaurant(formData);
            if (res !== null) {
                toast.success('Restaurant registered successfully!');
                navigate('/login');
            }
        } catch (err) {
            const message = err.response?.data?.message || err.message;
            toast.error(`Registration failed: ${message}`);
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
                    <form onSubmit={handleSubmit(
                        onSubmit,
                        (errors) => {
                            console.log("❌ Validation failed:", errors);
                        }
                    )} className="space-y-6">

                        {/* Logo Upload */}
                        <div className="form-control">
                            <label className="label"><span className="label-text">Restaurant Logo</span></label>
                            <input
                                type="file"
                                accept="image/*"
                                {...register('logo')}
                                onChange={e => { register('logo').onChange(e); handleLogoChange(e); }}
                                className="file-input file-input-bordered file-input-primary w-full"
                            />
                            {logoPreview && (
                                <img src={logoPreview} alt="Logo preview" className="mt-2 w-32 h-32 object-cover rounded" />
                            )}
                            {errors.logo && <span className="text-sm text-error">{errors.logo.message}</span>}
                        </div>

                        {/* Basic Info including Email */}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div className="form-control">
                                <label className="label"><span className="label-text">Name</span></label>
                                <input type="text" {...register('name')} placeholder="Restaurant Name" className="input input-bordered" />
                                {errors.name && <span className="text-sm text-error">{errors.name.message}</span>}
                            </div>
                            <div className="form-control">
                                <label className="label"><span className="label-text">Email</span></label>
                                <input type="email" {...register('email')} placeholder="Email Address" className="input input-bordered" />
                                {errors.email && <span className="text-sm text-error">{errors.email.message}</span>}
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
                        </div>

                        {/* Delivery Fee */}
                        <div className="form-control">
                            <label className="label"><span className="label-text">Delivery Fee</span></label>
                            <input
                                type="number"
                                step="0.01"
                                min="0.01"
                                {...register('deliveryFee')}
                                placeholder="e.g., 5.00"
                                className="input input-bordered w-full"
                            />
                            {errors.deliveryFee && <span className="text-sm text-error">{errors.deliveryFee.message}</span>}
                        </div>

                        {/* Categories */}
                        <div>
                            <h3 className="font-semibold mb-2">Categories</h3>
                            <div className="space-y-2">
                                {categories.map((field, index) => (
                                    <div key={field.id} className="flex items-center gap-2">
                                        <input
                                            type="text"
                                            {...register(`categoryNames.${index}`)}
                                            placeholder="Cuisine Category"
                                            className="input input-bordered flex-1"
                                        />
                                        <button
                                            type="button"
                                            onClick={() => remove(index)}
                                            className="btn btn-sm btn-error"
                                        >Remove</button>
                                    </div>
                                ))}
                                {errors.categoryNames && <p className="text-sm text-error">{errors.categoryNames.message}</p>}
                                <button type="button" onClick={() => append('')} className="btn btn-secondary btn-sm mt-2">Add Category</button>
                            </div>
                        </div>

                        {/* Opening Hours */}
                        <div>
                            <h3 className="font-semibold mb-2">Opening Hours</h3>
                            <p className="text-sm text-gray-500 mb-4">If closed, set both times equal (e.g., 00:00 - 00:00).</p>
                            <div className="space-y-2">
                                {days.map(day => (
                                    <div key={day} className="flex items-center gap-4">
                                        <span className="w-24 font-medium">{day}</span>
                                        <Controller name={`openingHours.${day}.openingTime`} control={control} render={({ field }) => <input type="time" {...field} className="input input-bordered w-32" />} />
                                        <span>to</span>
                                        <Controller name={`openingHours.${day}.closingTime`} control={control} render={({ field }) => <input type="time" {...field} className="input input-bordered w-32" />} />
                                        {errors.openingHours?.[day]?.openingTime && (
                                            <p className="text-sm text-error">
                                                {errors.openingHours[day].openingTime.message}
                                            </p>
                                        )}
                                        {errors.openingHours?.[day]?.closingTime && (
                                            <p className="text-sm text-error">
                                                {errors.openingHours[day].closingTime.message}
                                            </p>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Address */}
                        <div>
                            <h3 className="font-semibold mb-2">Address</h3>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div className="form-control"><label className="label"><span className="label-text">Region</span></label><input type="text" {...register('region')} placeholder="Region" className="input input-bordered" />{errors.region && <span className="text-sm text-error">{errors.region.message}</span>}</div>
                                <div className="form-control"><label className="label"><span className="label-text">Postal Code</span></label><input type="text" {...register('postalCode')} placeholder="Postal Code" className="input input-bordered" />{errors.postalCode && <span className="text-sm text-error">{errors.postalCode.message}</span>}</div>
                                <div className="form-control"><label className="label"><span className="label-text">City</span></label><input type="text" {...register('city')} placeholder="City" className="input input-bordered" />{errors.city && <span className="text-sm text-error">{errors.city.message}</span>}</div>
                                <div className="form-control"><label className="label"><span className="label-text">Address Line</span></label><input type="text" {...register('addressLine')} placeholder="Address Line" className="input input-bordered" />{errors.addressLine && <span className="text-sm text-error">{errors.addressLine.message}</span>}</div>
                            </div>
                        </div>

                        <button type="submit" className="btn btn-primary w-full" disabled={isSubmitting}>{isSubmitting ? 'Registering...' : 'Register Restaurant'}</button>
                    </form>
                </div>
            </div>
        </div>
    );
}
