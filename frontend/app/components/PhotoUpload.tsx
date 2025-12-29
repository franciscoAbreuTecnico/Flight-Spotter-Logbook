'use client';

import React, { useState, useRef } from 'react';
import { PhotoIcon, XMarkIcon, CloudArrowUpIcon } from '@heroicons/react/24/outline';

interface PhotoUploadProps {
  sightingId?: number;
  onPhotosChange?: (files: File[]) => void;
  maxPhotos?: number;
}

export default function PhotoUpload({ sightingId, onPhotosChange, maxPhotos = 5 }: PhotoUploadProps) {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [previewUrls, setPreviewUrls] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    const validFiles = files.filter(file => file.type.startsWith('image/'));
    
    // Limit number of photos
    const availableSlots = maxPhotos - selectedFiles.length;
    const filesToAdd = validFiles.slice(0, availableSlots);
    
    if (filesToAdd.length === 0) return;

    // Create preview URLs
    const newPreviewUrls = filesToAdd.map(file => URL.createObjectURL(file));
    
    const updatedFiles = [...selectedFiles, ...filesToAdd];
    const updatedPreviews = [...previewUrls, ...newPreviewUrls];
    
    setSelectedFiles(updatedFiles);
    setPreviewUrls(updatedPreviews);
    
    if (onPhotosChange) {
      onPhotosChange(updatedFiles);
    }
  };

  const removePhoto = (index: number) => {
    // Revoke the object URL to free memory
    URL.revokeObjectURL(previewUrls[index]);
    
    const updatedFiles = selectedFiles.filter((_, i) => i !== index);
    const updatedPreviews = previewUrls.filter((_, i) => i !== index);
    
    setSelectedFiles(updatedFiles);
    setPreviewUrls(updatedPreviews);
    
    if (onPhotosChange) {
      onPhotosChange(updatedFiles);
    }
  };

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Photos (Optional)
        </label>
        <p className="text-sm text-gray-500 mb-3">
          Add up to {maxPhotos} photos of your aircraft sighting
        </p>
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        onChange={handleFileSelect}
        className="hidden"
      />

      {/* Single upload area */}
      {selectedFiles.length === 0 ? (
        <div 
          onClick={triggerFileInput}
          className="border-2 border-dashed border-gray-300 rounded-xl p-12 text-center cursor-pointer hover:border-blue-500 hover:bg-blue-50 transition-all group"
        >
          <PhotoIcon className="h-16 w-16 mx-auto text-gray-400 group-hover:text-blue-500 mb-4 group-hover:scale-110 transition-transform" />
          <p className="text-gray-600 font-medium mb-2 group-hover:text-blue-600">
            Click to upload photos
          </p>
          <p className="text-sm text-gray-500">
            or drag and drop images here
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
          {previewUrls.map((url, index) => (
            <div key={index} className="relative group aspect-square bg-gray-100 rounded-lg overflow-hidden border-2 border-gray-300">
              <img
                src={url}
                alt={`Preview ${index + 1}`}
                className="w-full h-full object-cover"
              />
              <button
                type="button"
                onClick={() => removePhoto(index)}
                className="absolute top-2 right-2 p-1.5 bg-red-600 text-white rounded-full hover:bg-red-700 opacity-0 group-hover:opacity-100 transition-opacity shadow-lg"
                aria-label="Remove photo"
              >
                <XMarkIcon className="h-4 w-4" />
              </button>
            </div>
          ))}

          {/* Add more photos button */}
          {selectedFiles.length < maxPhotos && (
            <button
              type="button"
              onClick={triggerFileInput}
              className="aspect-square border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all flex flex-col items-center justify-center text-gray-500 hover:text-blue-600 group"
            >
              <CloudArrowUpIcon className="h-10 w-10 mb-2 group-hover:scale-110 transition-transform" />
              <span className="text-sm font-medium">Add More</span>
              <span className="text-xs text-gray-400 mt-1">
                {selectedFiles.length}/{maxPhotos}
              </span>
            </button>
          )}
        </div>
      )}
    </div>
  );
}
