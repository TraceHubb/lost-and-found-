# CampusFind - Lost & Found App
# contributers
1. Aman Baye(Aman-OG)
2. Amanuel nebey(Aman-2121)
3. Betelehem Beshahe(frehi582)
4. Bruktawit Zelalem(Bruktawit-t)
5. Dagmawit adeferes(Kalddass)
6. Kindie Abera(kindieabera)

A modern Android application built with Jetpack Compose that helps campus communities reunite lost items with their owners through a secure verification system.

## 🌟 Features

### Core Functionality
- **Report Lost Items**: Users can report items they've lost with detailed descriptions and verification questions
- **Report Found Items**: Users can report items they've found and create security questions for verification
- **Smart Suggestions**: Browse all available lost and found items with intelligent categorization
- **Secure Verification**: 4-question yes/no verification system to ensure items reach their rightful owners
- **History Tracking**: View your reported items and claim history with status updates

### Security & Verification
- **Question-Based Verification**: Item reporters create 4 yes/no questions that only the true owner would know
- **Automatic Claim Processing**: Claims are automatically approved when all 4 questions are answered correctly
- **Contact Information Protection**: Contact details are only revealed after successful verification
- **Firebase Authentication**: Secure user authentication and data protection

### User Experience
- **Modern UI**: Clean, intuitive interface built with Material Design 3
- **Real-time Updates**: Live data synchronization using Firebase Firestore
- **Image Support**: Upload photos of lost/found items for better identification
- **Category Organization**: Items organized by categories for easier browsing
- **Status Tracking**: Real-time status updates (Active, Claimed, Expired)

## 🏗️ Architecture

### Tech Stack
- **Frontend**: Jetpack Compose (Modern Android UI)
- **Backend**: Firebase (Authentication, Firestore, Storage)
- **Architecture**: MVVM with Repository Pattern
- **Language**: Kotlin
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Async Operations**: Kotlin Coroutines & Flow

### Project Structure
```
app/src/main/java/com/lostandfound/
├── data/
│   ├── models/           # Data models (SimpleFoundItem, SimpleLostItem, SimpleClaim)
│   ├── repositories/     # Data access layer (SimpleItemsRepository, AuthRepository)
│   ├── services/         # Business logic services
│   └── firebase/         # Firebase configuration
├── presentation/
│   ├── auth/            # Login & Registration screens
│   ├── home/            # Dashboard and main navigation
│   ├── items/           # Item reporting and browsing screens
│   ├── claim/           # Claim verification screens
│   ├── suggestions/     # Item suggestions and matching
│   ├── history/         # User history and reports
│   ├── components/      # Reusable UI components
│   └── navigation/      # App navigation setup
└── MainActivity.kt      # Main activity
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/campusfind-app.git
   cd campusfind-app/LostAndFoundApp
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password)
   - Enable Firestore Database
   - Enable Storage
   - Download `google-services.json` and place it in `app/` directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and run the project.

### Firebase Configuration

#### Firestore Collections
The app uses the following Firestore collections:
- `simple_found_items` - Found item reports
- `simple_lost_items` - Lost item reports  
- `simple_claims` - Verification claims
- `users` - User profiles

#### Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Anyone can read items, only authenticated users can write
    match /simple_found_items/{itemId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    match /simple_lost_items/{itemId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Claims can be read/written by authenticated users
    match /simple_claims/{claimId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 📱 How It Works

### For Item Finders
1. **Report Found Item**: Take a photo and describe the item
2. **Create Verification Questions**: Set 4 yes/no questions only the owner would know
3. **Wait for Claims**: Receive notifications when someone claims your found item
4. **Automatic Verification**: Contact info is shared automatically when verification passes

### For Item Owners
1. **Browse Suggestions**: Look through found items that might be yours
2. **Answer Questions**: Complete the 4-question verification process
3. **Get Contact Info**: Receive finder's contact details upon successful verification
4. **Coordinate Pickup**: Arrange to retrieve your item

### Verification System
- **4 Yes/No Questions**: Simple but effective verification
- **Boolean Logic**: Each question has a definitive true/false answer
- **All-or-Nothing**: Must answer all 4 questions correctly
- **Immediate Results**: Instant approval or rejection

## 🎨 UI/UX Features

### Design System
- **Material Design 3**: Modern, accessible design language
- **Purple & Pink Gradient**: Distinctive brand colors
- **Consistent Typography**: Clear hierarchy and readability
- **Responsive Layout**: Works on various screen sizes

### Key Screens
- **Home Dashboard**: Statistics, quick actions, and overview
- **Suggestions**: Tabbed interface for browsing items
- **History**: Personal reports and claims tracking
- **Verification**: Step-by-step claim process
- **Success**: Contact information reveal

## 🔧 Development

### Key Components

#### Data Models
```kotlin
data class SimpleFoundItem(
    val id: String,
    val itemName: String,
    val category: String,
    val question1: String,
    val answer1: Boolean,
    // ... 4 verification questions
)
```

#### Repository Pattern
```kotlin
object SimpleItemsRepository {
    suspend fun addFoundItem(item: SimpleFoundItem): String
    suspend fun submitFoundClaim(claim: SimpleClaim): String
    fun getActiveFoundItems(): Flow<List<SimpleFoundItem>>
}
```

#### Compose Navigation
```kotlin
NavHost(navController, startDestination = "home") {
    composable("suggestions") { SuggestionsScreen(...) }
    composable("history") { HistoryScreen(...) }
    composable("simple_claim/{itemId}") { SimpleClaimScreen(...) }
}
```

### Testing
- Unit tests for repositories and business logic
- UI tests for critical user flows
- Integration tests for Firebase operations

## 📊 Statistics & Analytics

The app tracks:
- Items reunited successfully
- User engagement metrics
- Verification success rates
- Category popularity
- Response times

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex business logic
- Maintain consistent formatting

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Firebase for backend infrastructure
- Jetpack Compose team for modern Android UI
- Material Design for design system
- Coil for efficient image loading

## 📞 Support

For support, email support@campusfind.app or create an issue in this repository.

---

**CampusFind** - Reuniting lost items with their owners, one verification at a time. 🎯
