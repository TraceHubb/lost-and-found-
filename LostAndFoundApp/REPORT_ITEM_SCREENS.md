# Report Item Screens Documentation

## Overview

The Lost & Found app provides two main screens for reporting items:
1. **Report Lost Item Screen** - For users who have lost an item
2. **Report Found Item Screen** - For users who have found an item

Both screens collect detailed information about items to facilitate matching and claiming.

---

## Report Lost Item Screen

### File Location
`app/src/main/java/com/lostandfound/presentation/items/ReportItemScreen.kt`

### Description
A multi-step wizard that guides users through reporting a lost item with comprehensive details.

### User Flow

#### **Step 1: Select Report Type**
Users choose between:
- **I Lost an Item** - Report something they lost
- **I Found an Item** - Report something they found

**Features:**
- Visual cards with icons
- Clear descriptions
- Privacy notice at bottom
- Modern gradient design

#### **Step 2: Item Details**
Users provide detailed information about the item:

**Required Fields:**
- ✅ **Item Name** - e.g., "iPhone 13", "Black Wallet"
- ✅ **Category** - Dropdown selection from predefined categories
- ✅ **Location** - Where the item was lost
- ✅ **Date & Time** - When it was lost

**Optional Fields:**
- Color (e.g., Black, Blue)
- Brand (e.g., Apple, Samsung)
- Additional Details (scratches, stickers, unique features)
- Item Photo (upload from gallery)

**Validation:**
- Item name cannot be blank
- Category must be selected
- Location must be provided

#### **Step 3: Contact Information & Review**
Users provide contact details and review their report:

**Required Fields:**
- ✅ **Contact Email** - Must be valid email format
- ✅ **Contact Phone** - Phone number

**Validation:**
- At least one contact method (email OR phone) required
- Email format validation
- Clear error messages

**Optional Fields:**
- Additional Notes

**Review Section:**
- Summary of all entered information
- Editable by going back to previous steps

### UI Components

#### Progress Indicators
- Step indicators (1, 2, 3) at top
- Current step highlighted in purple
- Completed steps shown in purple
- Upcoming steps in gray

#### Form Fields
- Modern rounded text fields
- Purple accent color (#8B5CF6)
- Light purple backgrounds (#F3E8FF)
- Clear placeholder text
- Icon indicators for each field

#### Category Dropdown
Predefined categories:
- Phone
- Wallet
- Keys
- Bag/Backpack
- Laptop
- Tablet
- Headphones
- Watch
- Jewelry
- Clothing
- Books
- ID/Documents
- Glasses
- Umbrella
- Water Bottle
- Other

#### Image Upload
- Tap to select from gallery
- Preview selected image
- Remove button (X) to clear selection
- Optional feature

#### Error Handling
- Red error cards at top of form
- Specific error messages:
  - "Please enter item name"
  - "Please select category"
  - "Please enter location"
  - "Please provide at least one contact method (email or phone)"
  - "Please enter a valid email address"

### Data Model

```kotlin
Item(
    itemName: String,
    description: String,        // Built from category, color, brand, details
    location: String,
    contactEmail: String,
    contactPhone: String,
    category: String,
    color: String,
    brand: String,
    additionalDetails: String,
    type: ItemType.LOST,
    date: Long                  // Timestamp
)
```

### Color Scheme
- **Primary Purple**: #8B5CF6
- **Secondary Pink**: #EC4899
- **Light Purple**: #F3E8FF
- **Background**: #FAFAFA
- **Card White**: #FFFFFF
- **Text Dark**: #1F2937
- **Text Gray**: #6B7280

---

## Report Found Item Screen

### File Location
`app/src/main/java/com/lostandfound/presentation/items/ReportFoundItemScreen.kt`

### Description
A single-page form for quickly reporting found items with all necessary details.

### User Flow

#### Single Form Layout
All fields presented on one scrollable screen for quick entry.

**Required Fields:**
- ✅ **Item Name** - e.g., "iPhone 13"
- ✅ **Category** - Dropdown selection
- ✅ **Location** - Where item was found
- ✅ **Contact Email** - Valid email address
- ✅ **Contact Phone** - Phone number

**Optional Fields:**
- Color
- Brand
- Additional Details (unique features)
- Item Photo

**Validation:**
- Item name required
- Category required
- Location required
- At least one contact method required
- Email format validation

### UI Components

#### Header
- Back button
- "Campus Lost & Found" title
- Profile icon

#### Form Fields
All fields use consistent styling:
- Light purple background (#F3E8FF)
- Rounded corners (12dp)
- Icon indicators
- Clear placeholder text

#### Category Dropdown
Same categories as Report Lost Item screen:
- Phone, Wallet, Keys, Bag/Backpack, etc.
- Searchable dropdown
- Easy selection

#### Image Upload Card
- Large upload area
- "Upload Item Photo (Optional)" text
- Tap to select from gallery
- Preview with remove button
- Upload progress indicator

#### Submit Button
- Full-width purple button
- "Post Found Item Report" text
- Loading spinner during submission
- Disabled state while loading

### Error Handling

Error messages displayed at top:
- "Please enter item name"
- "Please select item category"
- "Please enter location"
- "Please provide at least one contact method (email or phone)"
- "Please enter a valid email address"
- "Failed to post item: [error details]"

### Data Model

```kotlin
Item(
    itemName: String,
    description: String,        // Built from category, color, brand, details
    location: String,
    contactEmail: String,
    contactPhone: String,
    category: String,
    color: String,
    brand: String,
    additionalDetails: String,
    type: ItemType.FOUND,
    date: Long                  // Timestamp
)
```

---

## Common Features

### Contact Information Requirements

Both screens now **require** contact information:

**Previous Behavior:**
- Contact fields were optional
- Users could submit without contact info

**Current Behavior:**
- At least one contact method required (email OR phone)
- Email validation enforced
- Clear error messages guide users
- Cannot submit without valid contact info

**Why This Matters:**
- Enables successful item reunification
- Allows OTP verification for claims
- Facilitates direct communication
- Improves match success rate

### Image Upload

**Supported:**
- Select from device gallery
- Preview before submission
- Remove and reselect
- Upload to Firebase Storage

**Process:**
1. User taps upload area
2. Gallery picker opens
3. User selects image
4. Preview shown
5. Image uploaded during submission
6. URL stored in Firestore

### Firebase Integration

**Data Storage:**
- Items stored in Firestore `items` collection
- Images stored in Firebase Storage
- User ID linked to item
- Timestamp recorded

**Item Document Structure:**
```json
{
  "id": "auto-generated-id",
  "userId": "firebase-auth-uid",
  "itemName": "iPhone 13",
  "description": "Category: Phone\nColor: Black\nBrand: Apple\nDetails: Cracked screen",
  "location": "Library 2nd Floor",
  "date": 1234567890,
  "imageUrl": "https://firebase-storage-url",
  "type": "LOST" | "FOUND",
  "status": "ACTIVE",
  "contactEmail": "user@example.com",
  "contactPhone": "+1234567890",
  "category": "Phone",
  "color": "Black",
  "brand": "Apple",
  "additionalDetails": "Cracked screen",
  "datePosted": 1234567890
}
```

---

## Navigation

### Entry Points

**Report Lost Item:**
- Home screen → "Report Lost" button
- Navigation: `AppNavGraph.kt` → `"reportLost"` route

**Report Found Item:**
- Home screen → "Report Found" button
- Navigation: `AppNavGraph.kt` → `"reportFound"` route

### Exit Points

Both screens navigate back to home on:
- ✅ Successful submission
- ❌ Back button press (with confirmation)
- ❌ Cancel action

---

## User Experience Enhancements

### Visual Feedback

**Loading States:**
- Circular progress indicator during submission
- Button disabled while loading
- "Uploading image..." text when applicable

**Success States:**
- Automatic navigation to home
- Item appears in user's items list
- Matching algorithm runs automatically

**Error States:**
- Red error cards
- Specific error messages
- Fields highlighted in red
- Scroll to error location

### Accessibility

**Features:**
- Large touch targets (minimum 48dp)
- Clear labels and placeholders
- High contrast text
- Icon + text combinations
- Error announcements
- Keyboard navigation support

### Responsive Design

**Adapts to:**
- Different screen sizes
- Portrait and landscape
- Various Android versions (API 24+)
- Different text sizes

---

## Technical Implementation

### State Management

**Report Lost Item:**
```kotlin
var currentStep by remember { mutableStateOf(1) }
var reportType by remember { mutableStateOf<ItemType?>(null) }
var itemName by remember { mutableStateOf("") }
var category by remember { mutableStateOf("") }
var contactEmail by remember { mutableStateOf("") }
var contactPhone by remember { mutableStateOf("") }
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }
```

**Report Found Item:**
```kotlin
var itemName by remember { mutableStateOf("") }
var category by remember { mutableStateOf("") }
var contactEmail by remember { mutableStateOf("") }
var contactPhone by remember { mutableStateOf("") }
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }
```

### Validation Logic

```kotlin
// Item name validation
if (itemName.isBlank()) {
    errorMessage = "Please enter item name"
    return@Button
}

// Category validation
if (category.isBlank()) {
    errorMessage = "Please select item category"
    return@Button
}

// Location validation
if (location.isBlank()) {
    errorMessage = "Please enter location"
    return@Button
}

// Contact validation
if (contactEmail.isBlank() && contactPhone.isBlank()) {
    errorMessage = "Please provide at least one contact method (email or phone)"
    return@Button
}

// Email format validation
if (contactEmail.isNotBlank() && 
    !android.util.Patterns.EMAIL_ADDRESS.matcher(contactEmail).matches()) {
    errorMessage = "Please enter a valid email address"
    return@Button
}
```

### Repository Integration

```kotlin
// Submit item
ItemsRepository.addItem(item, selectedImageUri, context)
```

**Repository handles:**
- Image upload to Firebase Storage
- Document creation in Firestore
- Error handling
- Success callbacks

---

## Testing Checklist

### Report Lost Item
- [ ] Step 1: Type selection works
- [ ] Step 2: All fields accept input
- [ ] Step 2: Category dropdown works
- [ ] Step 2: Image upload works
- [ ] Step 2: Validation prevents empty required fields
- [ ] Step 3: Contact fields required
- [ ] Step 3: Email validation works
- [ ] Step 3: Review shows correct data
- [ ] Submit creates item in Firestore
- [ ] Navigation returns to home
- [ ] Back button works at each step

### Report Found Item
- [ ] All fields accept input
- [ ] Category dropdown works
- [ ] Image upload works
- [ ] Contact fields required
- [ ] Email validation works
- [ ] Validation prevents submission with missing data
- [ ] Submit creates item in Firestore
- [ ] Loading state shows during submission
- [ ] Error messages display correctly
- [ ] Navigation returns to home
- [ ] Back button works

### Contact Information
- [ ] Email-only submission works
- [ ] Phone-only submission works
- [ ] Both email and phone submission works
- [ ] Invalid email shows error
- [ ] Empty contact fields show error
- [ ] Error clears when user types

---

## Future Enhancements

### Potential Improvements

1. **Auto-fill Location**
   - GPS-based location detection
   - Common campus locations dropdown
   - Recent locations history

2. **Date/Time Picker**
   - Calendar widget for date selection
   - Time picker for precise timing
   - "Just now" quick option

3. **Image Enhancements**
   - Multiple image upload
   - Camera capture option
   - Image compression
   - Image editing (crop, rotate)

4. **Smart Suggestions**
   - Auto-suggest category based on item name
   - Brand suggestions based on category
   - Common color palette

5. **Draft Saving**
   - Save incomplete reports
   - Resume later
   - Auto-save progress

6. **Barcode/QR Scanning**
   - Scan item barcodes
   - Auto-fill product details
   - Serial number capture

7. **Voice Input**
   - Voice-to-text for descriptions
   - Hands-free reporting
   - Accessibility enhancement

---

## Troubleshooting

### Common Issues

**Issue: "Failed to post item"**
- Check internet connection
- Verify Firebase configuration
- Check Firestore permissions
- Review error logs

**Issue: Image upload fails**
- Check storage permissions
- Verify Firebase Storage rules
- Check image size (max 10MB)
- Try different image format

**Issue: Email validation fails**
- Ensure proper email format
- Check for spaces
- Verify @ symbol present
- Check domain format

**Issue: Category dropdown not showing**
- Verify ItemCategories object exists
- Check dropdown state management
- Review UI rendering

---

## Code References

### Key Files
- `ReportItemScreen.kt` - Lost item reporting (multi-step)
- `ReportFoundItemScreen.kt` - Found item reporting (single page)
- `Item.kt` - Data model
- `ItemsRepository.kt` - Firebase operations
- `AppNavGraph.kt` - Navigation setup

### Key Functions
- `ReportLostItemScreen()` - Main composable for lost items
- `ReportFoundItemScreen()` - Main composable for found items
- `Step1SelectType()` - Type selection step
- `Step2ItemDetails()` - Item details form
- `Step3ContactAndReview()` - Contact and review step
- `FormField()` - Reusable form field component
- `ItemsRepository.addItem()` - Submit item to Firebase

---

## Support

For issues or questions:
1. Check error logs in Logcat
2. Review Firebase console for data
3. Verify network connectivity
4. Check app permissions
5. Review validation logic

---

**Last Updated:** 2024
**Version:** 1.0
**Maintained by:** Lost & Found Development Team
