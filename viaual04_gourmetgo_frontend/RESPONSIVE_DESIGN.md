# Responsive Design Implementation

## Mobile-First Approach

This application follows a mobile-first responsive design strategy, starting with mobile layouts and progressively enhancing for larger screens.

## Breakpoints

- **Mobile**: 320px - 639px (default)
- **Small**: 640px+ (sm:)
- **Medium**: 768px+ (md:)
- **Large**: 1024px+ (lg:)
- **Extra Large**: 1280px+ (xl:)
- **2X Large**: 1536px+ (2xl:)

## Key Responsive Components

### 1. Header Component
- Mobile: Compact logo, hidden navigation text, smaller buttons
- Tablet+: Full navigation, larger buttons, visible text labels
- Desktop: Full layout with all elements visible

### 2. Hero Section
- Mobile: Stacked layout, smaller text, full-width search
- Tablet: Larger text, better spacing
- Desktop: Maximum visual impact with large text and imagery

### 3. Restaurant Grid
- Mobile: Single column layout
- Tablet: 2 columns
- Desktop: 3 columns
- Large screens: Maintains 3 columns with better spacing

### 4. Navigation
- Mobile: Bottom navigation bar (btm-nav)
- Desktop: Header navigation only

### 5. Cards (Restaurant/Product)
- Mobile: Optimized for touch, larger tap targets
- Desktop: Hover effects, smaller compact design

## Mobile-Specific Features

1. **Bottom Navigation**: Easy thumb navigation on mobile devices
2. **Touch-Friendly**: Larger tap targets, appropriate spacing
3. **Optimized Images**: Responsive image sizing for different screen densities
4. **Collapsible Filters**: Modal-based filters on mobile, sidebar on desktop
5. **Swipe Gestures**: Enhanced for mobile interaction patterns

## Performance Considerations

- Images are optimized for different screen sizes
- Touch interactions are prioritized on mobile devices
- Hover effects are disabled on touch devices
- Minimal layout shifts between breakpoints

## Testing

Test the responsive design on:
- Mobile devices (320px - 767px)
- Tablets (768px - 1023px)
- Laptops (1024px - 1439px)
- Desktop screens (1440px+)

## Utilities Added

- Line clamp utilities for text truncation
- Touch-friendly interaction styles
- Custom spacing utilities
- Mobile-first component variants