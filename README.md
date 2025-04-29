# RockSolid - Ai Generated Fitness Programs For Rock Climbers 

---

RockSolid is a personal fitness and climbing assistant app designed to guide users through customised training plans, track achievements, and improve performance. The app offers a coach-led tutorial system, training progression tracking, achievements, notifications, and more—all built with a friendly and motivating user experience.

## Features

- **Personalised Training Plans**
  - Weekly plans generated based on user experience and performance.
  - Daily training routines including warmups, exercises, and cooldowns.

- **Coach Rocky Assistant**
  - Friendly guide that appears contextually (on first use, training start, new achievements, etc.).
  - Delivers information and encouragement using interactive dialogs.

- **Progress Tracking**
  - Visual progress bars showing completion rates of exercises and sessions.
  - Linear progress indicators and achievement milestones.

- **Achievements and Badges**
  - Unlockable badges for completing training milestones (1, 10, 50 sessions).
  - Achievement animations and celebratory dialogs.

- **Notification System**
  - Real-time feedback for new achievements and unread alerts.
  - Visual badge indicators on the home screen.

- **Surveys and Personalisation**
  - Onboarding surveys for tailoring the program to the user's level and goals.

- **Tutorial Integration**
  - Informational tooltips and dialogs triggered per exercise or screen section.
  - Hardcoded and dynamic tutorial content.

## Technologies Used

- **Kotlin** & **Jetpack Compose** – Modern Android UI toolkit.
- **Firebase Firestore** – Real-time database for user data and training plans.
- **Firebase Authentication** – Secure user login management.
- **MediaPlayer** – Audio feedback on achievement unlocks.
- **Coroutines** – Asynchronous loading of user data and remote resources.

## Screens & UX

- **Home Screen**
  - Displays training focus, current stats, profile picture, and survey status.

- **Training Program Screen**
  - Calendar view for selecting training days.
  - Exercise previews and day-specific details.

- **Exercise Screen**
  - Interactive interface for completing and tracking exercise sets.
  - Confirmation dialogs and partial progress tracking.

- **Achievements Screen**
  - Visual reward board, achievement dialog system, and badges.
  - Celebration dialogs for milestone completions.

## Installation

1. Clone the repository or unzip the provided project.
2. Open in **Android Studio IDE**.
3. Ensure the Firebase project is connected with the correct `google-services.json`.
4. Sync Gradle and run on an Android device or emulator.

## Customisation Notes

- All training plans are generated based on the user's onboarding flow.
- Tutorial content and coach dialogs are handled with composable overlay logic.
- Notifications and progress are stored in Firestore under respective collections (`Notification`, `Progress`, `Users`).

## Folder Structure Overview

- `screens/` – Contains all Composable screens (Home, Exercise, Training, etc.).
- `navigation/` – Route declarations and NavHost control.
- `viewmodel/` – ViewModel for authentication state.
- `modelScripts/` – Logic for generating training plans and injecting tutorials.
- `res/` – Drawable assets including Coach Rocky images and badges.

## License

This project is intended for educational and personal development purposes only. Commercial use is not permitted without explicit permission.


---

# Commit 1
1. Project Structure Setup
2. Screens set up
3. Firebase set up
4. issue with logging in and registering

---

# Commit 2
1. Login and Register Fixed
2. UX/UI remodelled to align with apps brand

---

# Commit 3
1. Home Screen added with cards to access Training Programs and the Progress Dashboard.
2. Instant Login Button Added

---

# Commit 4
1. UX clean up on home screen
2. Nav to Training Program Screen
3. Added an interactive calender to allow user to see current workouts and previous.
4. Added a checkbox system to track users workout completion
5. UX polish to match apps theme

--- 

# Commit 5
1. Added the Progress Dashboard
2. Added Back buttons to screens for navigation
3. Added a Logout button
4. Matched button colours with app theme for consistency on multiple screens

---

# Commit 6
1. Fixed the notes issue in the training plan screen
2. Edited the UI to make exercises more readable

---

# Commit 7
1. AuthViewModel added
2. Login functionality added to AVM

---

# Commit 8
1. Surveys added to the app to categorise users into different skill levels (beginner, intermediate, advanced)
2. two types of surveys, quick & tailored
3. surveyviewmodel hold the user answers right now, needs to be sent to the database by next commit.
4. UX polish required for the home page, add a nav draw/hamburg for additional features

---

# Commit 9
1. Added Achievements to the app to gamify it for users
2. Added Badges that are earned per achievement

---

# Commit 10
1. Swapped out dummy data with real data in profile card
2. Edit details now works and is prepopulated
3. Profile pic is work in progress

---

# Commit 11
1. Signup validation and error handling added, no duplicate accounts allowed, passwords must meet security requirements
2. Sign in only allows valid details, and no duplicates
3. UX improvement for error messages and password building

---

# Commit 12: 
1. FirstTimeUser now is fucntional and is set when surveys are complete
2. Coach Rocky Tutorial screen only appears for a first time user
3. Survey screen automatically flows after the tutorial so the user data is taken
4. Db sucessdfully being filled in the SurveyAnswers table allowing data to be used by the ML Model.

---

# Commit 13:
1. Training Programs are now generated based on user survey selections
2. Exercise screen added as a workout area
3. UX enhancements needed

---

# Commit 14-22:
1. All App functionality is working 
- surveys
- training programs
- progress tracking 
- achievements
- notifications 
- forget password emails
2. Coach Rocky the app mascot was added to all screens for user interaction, better UX, and gamification
3. App is complete
