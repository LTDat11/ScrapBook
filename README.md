# ScrapBooking Android Application

ScrapBooking is a creative Android application designed to transform real-world captures into digital scrapbooking elements. The application focuses on a seamless, interactive experience for creating digital stamps from live camera feeds.

## Core Features
The current release implements a advanced camera-to-stamp workflow:

1. Interactive Stamp Frame: A professional metallic stamp frame overlay that users can interact with.
2. Motion Feedback: Smooth scale-down animations when pressing the frame, providing tactile visual feedback.
3. Precise Image Processing: Automated bitmap capture and cropping logic that precisely extracts the image within the frame boundaries.
4. Custom Stamp Perforations: A unique vector-based clipping system that creates a realistic postal stamp effect with exactly 7 horizontal and 9 vertical semi-circular perforations.
5. Instant Preview: A high-performance popup system to review created stamps immediately after capture.

## Technical Implementation
The project follows modern Android development standards to ensure scalability and maintainability:

1. Clean Architecture (MVVM): Separation of concerns between the UI layer, ViewModel, and Repository.
2. Logic Decoupling: Thread-safe image processing handled in the Repository layer, moved away from the UI thread to ensure zero frame drops.
3. State Management: Reactive UI updates using StateFlow and sealed classes to represent various application states (Loading, Ready, Capturing, Error).
4. Custom Graphics: Implementation of specialized Compose Shapes using Path and ArcTo vectors for unique UI elements.
5. Dependency Injection: Powered by Hilt for efficient resource management and testability.

## Software Stack
1. Language: Kotlin
2. UI Framework: Jetpack Compose
3. Camera API: CameraX
4. Concurrency: Kotlin Coroutines & Flow
5. Dependency Injection: Dagger Hilt
6. Image Input/Output: Android Graphics Bitmap API
7. Permissions: Google Accompanist

## Project Structure
1. com.example.scrapbooking.data.repository: Contains CameraRepository for bitmap manipulation and camera operations.
2. com.example.scrapbooking.viewmodel: HomeViewModel handles the business logic and UI state transitions.
3. com.example.scrapbooking.ui.screens: HomeScreen defines the layout and user interactions.
4. com.example.scrapbooking.ui.components: Reusable elements like StampShape, ErrorView, and LoadingView.
5. com.example.scrapbooking.ui.state: HomeUiState definitions for the MVI-style state management.

## Installation and Build
To build this project, you will need:
1. Android Studio Ladybug or newer.
2. JDK 17 or 21 (specifically the Android Studio JBR).
3. Android SDK Level 35 or higher.

To build from the command line:
./gradlew assembleDebug -Porg.gradle.java.home="/Path/To/Your/AndroidStudio/jbr/Contents/Home"

## Current Development Status
The application has completed its core camera and stamp creation engine. Future versions will include the AlbumScreen for organizing and managing the collection of created stamps.
