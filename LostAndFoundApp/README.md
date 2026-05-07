# Campus Lost & Found App

A modern Android application built with Jetpack Compose that helps students and campus community members report and find lost or found items on campus.

## Features

### Authentication
- User registration and login with Firebase Authentication
- Secure user session management

### Item Management
- **Report Lost Items** - Post items you've lost with details and photos
- **Report Found Items** - Post items you've found to help others
- **Browse Items** - View all lost or found items with filtering options
- **Search Functionality** - Search items by name, description, or location
- **Real-time Updates** - Get instant notifications when new items are posted

### User Interface
- Clean, modern Material Design 3 interface
- Intuitive navigation with quick action buttons
- Dashboard with notifications and statistics
- Image upload support for item identification

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Backend**: Firebase
  - Firebase Authentication
  - Cloud Firestore (Database)
  - Firebase Storage (Images)
  - Firebase Cloud Messaging (Notifications)
- **Image Loading**: Coil
- **Navigation**: Jetpack Navigation Compose
- **Async Operations**: Kotlin Coroutines

## Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- Android SDK 24 or higher
- Kotlin 1.9.20
- Gradle 8.2

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/lost-and-found-app.git
cd lost-and-found-app
```

### 2. Firebase Configuration
1. Create a new project in [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Download the `google-services.json` file
4. Place it in the `app/` directory
5. Enable the following in Firebase Console:
   - Authentication (Email/Password)
   - Cloud Firestore
   - Firebase Storage
   - Cloud Messaging (optional)

### 3. Build and Run
1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on an emulator or physical device

## Project Structure

```
app/src/main/java/com/lostandfound/
├── data/
│   ├── firebase/          # Firebase configuration
│   ├── models/            # Data models (Item, User)
│   └── repositories/      # Data access layer
├── presentation/
│   ├── auth/              # Login and registration screens
│   ├── home/              # Home dashboard
│   ├── items/             # Item listing and reporting
│   ├── navigation/        # Navigation graph
│   └── ...                # Other feature screens
└── MainActivity.kt        # App entry point
```

## Key Components

### Data Models
- **Item**: Represents lost or found items with properties like name, description, location, image, and status
- **User**: User profile information
- **ItemType**: Enum for LOST or FOUND items
- **ItemStatus**: Enum for ACTIVE, RECOVERED, or CLAIMED items

### Repositories
- **AuthRepository**: Handles user authentication
- **ItemsRepository**: Manages item CRUD operations and real-time updates

### Screens
- **LoginScreen**: User authentication
- **RegisterScreen**: New user registration
- **HomeScreen**: Dashboard with quick actions and notifications
- **ItemsListScreen**: Browse and search items
- **ReportItemScreen**: Report lost or found items
- **ItemDetailScreen**: View detailed item information

## Features in Development

- Automatic matching system for lost and found items
- In-app chat between users
- Item claim verification process
- Location-based item discovery
- Push notifications for matches

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please open an issue in the GitHub repository.

## Acknowledgments

- Built with modern Android development best practices
- Follows Material Design 3 guidelines
- Uses Firebase for backend infrastructure
