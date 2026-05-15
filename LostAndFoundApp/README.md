# 🔍 Lost & Found App

A modern Android application designed to help people reunite with their lost items through an intelligent matching system and secure verification process.

## 📱 Overview

Lost & Found is a comprehensive mobile solution that connects people who have lost items with those who have found them. The app uses smart matching algorithms to automatically identify potential matches and provides a secure OTP-based verification system to ensure items are returned to their rightful owners.

## ✨ Key Features

### 🎯 Core Functionality

- **Report Lost Items** - Users can report items they've lost with detailed descriptions, photos, location, and contact information
- **Report Found Items** - Good Samaritans can report items they've found to help return them to owners
- **Smart Matching Algorithm** - Automatically matches lost and found items based on:
  - Item category (Phone, Wallet, Keys, etc.)
  - Color and brand
  - Location proximity
  - Time frame
  - Detailed descriptions
- **Browse Items** - View all reported lost and found items with filtering options
- **Real-time Updates** - Firebase integration ensures instant synchronization across devices

### 🔐 Security & Verification

- **Secure Item Claiming** - Multi-step verification process to prevent fraud
- **OTP Email Verification** - 6-digit verification codes sent via email
- **Authorization Checks** - Only the original reporter can claim their lost item
- **Audit Logging** - Complete tracking of all claim attempts for security
- **Session Management** - Time-limited OTP sessions (5 minutes) with attempt limits

### 👤 User Management

- **Firebase Authentication** - Secure user registration and login
- **Email/Password Authentication** - Simple and secure account creation
- **User Profiles** - Manage personal information and contact details
- **Contact Information** - Required email and phone number for successful reunification

### 📸 Rich Media Support

- **Image Upload** - Attach photos of lost/found items
- **Firebase Storage Integration** - Secure cloud storage for images
- **Image Preview** - View item photos before claiming
- **Gallery Integration** - Easy photo selection from device

### 🎨 Modern UI/UX

- **Material Design 3** - Beautiful, modern interface following Google's design guidelines
- **Jetpack Compose** - Declarative UI framework for smooth, responsive experiences
- **Custom Color Scheme** - Purple and pink gradient theme for visual appeal
- **Intuitive Navigation** - Easy-to-use multi-step wizards and clear navigation flows
- **Responsive Design** - Adapts to different screen sizes and orientations

## 🏗️ Technical Architecture

### Technology Stack

**Frontend:**
- Kotlin
- Jetpack Compose (UI)
- Material Design 3
- Navigation Component
- Coroutines & Flow

**Backend:**
- Firebase Authentication
- Cloud Firestore (Database)
- Firebase Storage (Images)
- Firebase Cloud Messaging (Notifications)

**Email Service:**
- JavaMail API
- Gmail SMTP
- HTML Email Templates

**Networking:**
- Retrofit
- OkHttp
- Gson

**Image Loading:**
- Coil

### Project Structure

```
app/
├── data/
│   ├── api/              # External API interfaces
│   ├── firebase/         # Firebase configuration
│   ├── models/           # Data models
│   ├── repositories/     # Data access layer
│   └── services/         # Business logic services
├── presentation/
│   ├── auth/             # Login & Registration
│   ├── home/             # Home screen
│   ├── items/            # Item reporting & browsing
│   ├── matching/         # Matching algorithm UI
│   ├── matches/          # Match results
│   ├── claim/            # Claim verification flow
│   └── navigation/       # App navigation
└── MainActivity.kt
```

## 🚀 How It Works

### For Users Who Lost Items:

1. **Register/Login** - Create an account or sign in
2. **Report Lost Item** - Fill out a detailed form with:
   - Item name and category
   - Color, brand, and unique features
   - Location where lost
   - Date and time
   - Contact information (email & phone)
   - Optional photo
3. **Wait for Matches** - The app automatically searches for matching found items
4. **Review Matches** - View potential matches with similarity scores
5. **Claim Item** - Initiate secure verification process
6. **Verify Identity** - Enter OTP code sent to email
7. **Get Contact Info** - Receive finder's contact details
8. **Reunite** - Contact the finder to retrieve the item

### For Users Who Found Items:

1. **Register/Login** - Create an account or sign in
2. **Report Found Item** - Provide details about the item:
   - Item name and category
   - Color, brand, and condition
   - Location where found
   - Contact information (email & phone)
   - Optional photo
3. **Wait to be Contacted** - Your contact info is shared only after verification
4. **Help Reunite** - Coordinate with the verified owner to return the item

### Matching Algorithm:

The app uses an intelligent scoring system that considers:
- **Category Match** (40 points) - Items must be in the same category
- **Color Match** (20 points) - Matching colors increase score
- **Brand Match** (20 points) - Same brand adds confidence
- **Location Proximity** (10 points) - Items lost/found nearby
- **Description Similarity** (10 points) - Text analysis of descriptions

Items with scores above 60% are considered potential matches.

### Verification Process:

1. **User clicks "Claim"** on a matched item
2. **Authorization Check** - System verifies user is the original reporter
3. **OTP Generation** - Unique 6-digit code created
4. **Email Sent** - Professional HTML email with OTP code
5. **User Enters OTP** - Code must be entered within 5 minutes
6. **Verification** - System validates the code
7. **Contact Info Released** - Finder's details shared with verified owner
8. **Items Marked as Claimed** - Both items updated in database

## 🔧 Setup & Installation

### Prerequisites

- Android Studio (latest version)
- Android SDK (API 24+)
- Firebase account
- Gmail account (for email service)

### Firebase Setup

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app to your project
3. Download `google-services.json` and place it in `app/` directory
4. Enable Authentication (Email/Password)
5. Create Firestore database
6. Set up Firebase Storage
7. Configure security rules (see `FIREBASE_SETUP.md`)

### Email Service Setup

1. Create a Gmail account for the app
2. Enable 2-Step Verification
3. Generate App Password
4. Update credentials in `EmailService.kt`:
   ```kotlin
   private const val SENDER_EMAIL = "your-email@gmail.com"
   private const val SENDER_PASSWORD = "your-app-password"
   ```
5. See `CONFIGURE_EMAIL.txt` for detailed instructions

### Build & Run

```bash
# Clone the repository
git clone <repository-url>

# Open in Android Studio
# File > Open > Select project directory

# Sync Gradle
# Build > Make Project

# Run on device/emulator
# Run > Run 'app'
```

## 📊 Database Schema

### Users Collection
```json
{
  "uid": "string",
  "email": "string",
  "displayName": "string",
  "phoneNumber": "string"
}
```

### Items Collection
```json
{
  "id": "string",
  "userId": "string",
  "itemName": "string",
  "description": "string",
  "category": "string",
  "color": "string",
  "brand": "string",
  "location": "string",
  "contactEmail": "string",
  "contactPhone": "string",
  "imageUrl": "string",
  "type": "LOST | FOUND",
  "status": "ACTIVE | CLAIMED",
  "date": "timestamp",
  "datePosted": "timestamp"
}
```

### OTP Verification Sessions
```json
{
  "id": "string",
  "otpCode": "string",
  "userId": "string",
  "matchId": "string",
  "lostItemId": "string",
  "foundItemId": "string",
  "createdAt": "timestamp",
  "expiresAt": "timestamp",
  "isVerified": "boolean",
  "attemptCount": "number"
}
```

## 🎨 Design System

### Color Palette
- **Primary Purple**: `#8B5CF6`
- **Secondary Pink**: `#EC4899`
- **Light Purple**: `#F3E8FF`
- **Background**: `#FAFAFA`
- **Success Green**: `#10B981`
- **Error Red**: `#EF4444`

### Typography
- **Headlines**: Bold, 24-28sp
- **Titles**: SemiBold, 18-20sp
- **Body**: Regular, 14-16sp
- **Captions**: Regular, 12sp

## 📱 Screenshots

*(Add screenshots of your app here)*

## 🔒 Security Features

- **Encrypted Communication** - All data transmitted over HTTPS
- **Secure Authentication** - Firebase Auth with industry-standard security
- **OTP Verification** - Time-limited codes prevent unauthorized access
- **Rate Limiting** - Maximum 3 resend attempts per hour
- **Attempt Tracking** - Maximum 5 verification attempts per session
- **Audit Logging** - All claim attempts logged for review
- **Authorization Checks** - Only item owners can claim their items

## 🚧 Future Enhancements

- [ ] Push notifications for new matches
- [ ] In-app messaging between users
- [ ] QR code scanning for quick item identification
- [ ] Multi-language support
- [ ] Dark mode
- [ ] Advanced search filters
- [ ] Item categories expansion
- [ ] Location-based search radius
- [ ] User ratings and reviews
- [ ] Admin dashboard for moderation

## 📄 Documentation

- [Report Item Screens](REPORT_ITEM_SCREENS.md) - Detailed documentation of reporting functionality
- [Email Setup](EMAIL_SETUP_INSTRUCTIONS.md) - Complete email service configuration guide
- [Firebase Setup](FIREBASE_SETUP.md) - Firebase configuration instructions
- [Email Configuration](CONFIGURE_EMAIL.txt) - Quick email setup guide

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Development Team** - Initial work and ongoing maintenance

## 🙏 Acknowledgments

- Firebase for backend infrastructure
- Material Design for UI guidelines
- Jetpack Compose for modern Android UI
- JavaMail for email functionality
- All contributors and testers

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Contact: [your-email@example.com]

## 🔄 Version History

### v1.0.0 (Current)
- Initial release
- Core lost & found functionality
- Smart matching algorithm
- OTP verification system
- Email notifications
- Image upload support
- Firebase integration

---

**Made with ❤️ to help people reunite with their lost items**
