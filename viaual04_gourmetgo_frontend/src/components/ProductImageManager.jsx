import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { uploadProductImage, updateImage, getImageUrl } from '../api/imageService';

const ProductImageManager = ({ product, onImageUpdate }) => {
  const [uploading, setUploading] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);

  const handleImageUpload = async (file) => {
    if (!product?.id) {
      toast.error('Product must be saved before uploading an image');
      return;
    }

    setUploading(true);
    try {
      const result = await uploadProductImage(file, product.id);
      onImageUpdate(result.data);
      toast.success('Image uploaded successfully');
    } catch (error) {
      toast.error(error.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleImageUpdate = async (file) => {
    if (!product?.image?.id) return;

    setUploading(true);
    try {
      const result = await updateImage(file, product.image.id);
      onImageUpdate(result.data);
      toast.success('Image updated successfully');
    } catch (error) {
      toast.error(error.message || 'Update failed');
    } finally {
      setUploading(false);
    }
  };

  const handleFileSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error('Please select an image file');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toast.error('Image size must be less than 5MB');
      return;
    }

    // Create preview for new products
    if (!product?.id) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreviewImage(e.target.result);
        onImageUpdate({ previewUrl: e.target.result, file });
      };
      reader.readAsDataURL(file);
      return;
    }

    if (product?.image?.id) {
      await handleImageUpdate(file);
    } else {
      await handleImageUpload(file);
    }
  };

  return (
    <div className="space-y-2">
      <h4 className="font-semibold">Product Image</h4>
      <div className="flex items-center space-x-4">
        {(product?.image?.id || previewImage) && (
          <img 
            src={product?.image?.id ? getImageUrl(product.image.id) : previewImage} 
            alt="Product" 
            className="w-20 h-20 object-cover rounded-lg border"
          />
        )}
        <div className="flex-1">
          <input
            type="file"
            accept="image/*"
            onChange={handleFileSelect}
            disabled={uploading}
            className="file-input file-input-bordered w-full"
          />
          {uploading && (
            <div className="text-sm text-gray-500 mt-1">
              {product?.image?.id ? 'Updating...' : 'Uploading...'}
            </div>
          )}
          {!product?.id && (
            <div className="text-sm text-gray-500 mt-1">
              Image will be uploaded when product is saved
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductImageManager;