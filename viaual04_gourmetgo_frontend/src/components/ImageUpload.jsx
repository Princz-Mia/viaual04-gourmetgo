import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { getImageUrl } from '../api/imageService';

const ImageUpload = ({ 
  currentImage, 
  onUpload, 
  onUpdate, 
  accept = "image/*",
  className = "",
  placeholder = "Upload Image"
}) => {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(null);

  const handleFileSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      toast.error('Please select an image file');
      return;
    }

    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Image size must be less than 5MB');
      return;
    }

    setUploading(true);
    try {
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => setPreview(e.target.result);
      reader.readAsDataURL(file);

      // Upload or update
      if (currentImage?.id && onUpdate) {
        await onUpdate(file, currentImage.id);
      } else if (onUpload) {
        await onUpload(file);
      }
      
      toast.success('Image uploaded successfully');
    } catch (error) {
      toast.error(error.message || 'Upload failed');
      setPreview(null);
    } finally {
      setUploading(false);
    }
  };

  const imageUrl = currentImage?.id ? getImageUrl(currentImage.id) : preview;

  return (
    <div className={`space-y-2 ${className}`}>
      <div className="flex items-center space-x-4">
        {imageUrl && (
          <img 
            src={imageUrl} 
            alt="Preview" 
            className="w-20 h-20 object-cover rounded-lg border"
          />
        )}
        <div className="flex-1">
          <input
            type="file"
            accept={accept}
            onChange={handleFileSelect}
            disabled={uploading}
            className="file-input file-input-bordered w-full"
          />
          {uploading && (
            <div className="text-sm text-gray-500 mt-1">Uploading...</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ImageUpload;