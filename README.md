<h1 align="center">
  <a href="https://github.com/sagieinav/android-mono-task"><img src="https://github.com/user-attachments/assets/1f8097ba-7108-4dbc-8c5c-4a6f92c4736e" alt="MonoTask Logo" width="200"></a>
  <br>
  MonoTask
</h1>


<p align="center">
  <b>A gamified task management app for Android that focuses on eliminating task paralysis and enhancing task completion.</b>
</p>

<p align="center">
  <a href="https://github.com/sagieinav/android-mono-task/issues">Report Bug</a>
  •
  <a href="https://github.com/sagieinav/android-mono-task/issues">Request Feature</a>
  •
  <a href="#4-demonstration">View Demo</a>
</p>

<h2></h2>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Firebase-DD2C00?style=flat&logo=firebase&logoColor=white" alt="Firebase" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License" />
</p>


<!-- Screenshots -->
<img width="3944" height="2917" alt="ss_batch1" src="https://github.com/user-attachments/assets/c8fa30fc-371c-48b1-93b4-0a9261094a2a" />
<img width="3916" height="2881" alt="ss_batch2" src="https://github.com/user-attachments/assets/01208d25-50e0-4b98-bf37-481e22128d05" />
<img width="3935" height="2895" alt="ss_batch3" src="https://github.com/user-attachments/assets/58058926-7e7f-4607-96a6-6ccf9f3ce099" />


<!-- <p align="center">
  <img src="https://github.com/user-attachments/assets/294e2731-869f-459a-9a62-13447138aba2" height="500"
	  /><img src="https://github.com/user-attachments/assets/d891a239-640d-4c6b-9908-08a1a8acc51a" height="500"
			/><img src="https://github.com/user-attachments/assets/981ca6ef-1a3c-478f-ab77-488ce260645b" height="500" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/f3d855ec-478f-4b57-865a-edda39b3ceac" height="500"
	  /><img src="https://github.com/user-attachments/assets/6b0050c6-c92c-462f-9262-74d8cd03b4ea" height="500"
			/><img src="https://github.com/user-attachments/assets/87b1497d-2915-4193-9dc5-e92554dcae1f" height="500" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/193079e1-60dc-4732-bad5-da3fb11ea84a" height="500"
	  /><img src="https://github.com/user-attachments/assets/b2a9a55a-afab-4f5c-9ee9-5e0b0f79b2a3" height="500"
			/><img src="https://github.com/user-attachments/assets/d2d52e79-875d-4434-9c8a-dfd85d23d785" height="500" /> -->


## 1 About The Project

Developed as a final project for the 'UI Development' course at Afeka College of Engineering, as part of my Computer Science BSc.

MonoTask rewards focus. The idea is simple: stop multitasking, lock in on one task, complete it, and earn XP. Users level up as they complete tasks, build streaks, and unlock achievements. 


<!-- KEY FEATURES -->
## 2 Key Features

- **Focus**: Work through tasks one at a time. Each completion awards XP based on task priority and snooze count.
- **Kanban Board**: View and organize all tasks in an accessible kanban view, with sorting and archive support.
- **Hyperfocus:** An optional toggle that locks access to the kanban board, for those who wanna maximize their focus.
- **Gamification**: XP, levels, streaks, and achievements to make productivity feel rewarding.
- **Statistics**: Activity heatmap, bar and line charts, and trend indicators for tracking your output over time.
- **Social Layer**: Add friends and follow their progress and activity.
- **Daily Brief**: A summary screen that shows your task load and completion status for the day.
- **Custom Design System**: A dedicated `:designsystem` module with reusable Compose components, and a consistent visual language across all screens.

<!-- TECH STACK -->
## 3 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Backend:** [Firebase](https://firebase.google.com/) (Firestore & Authentication)
- **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Asynchronous Programming:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Local Storage:** [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Testing:** [JUnit 5](https://junit.org/junit5/), [Turbine](https://github.com/cashapp/turbine), [AssertK](https://github.com/willowtreeapps/assertk)

<!-- ARCHITECTURE -->
## 4 Architecture

The project follows Clean Architecture for the overall file structure of the project, and an optimized MVVM with Contract pattern as the app architecture.

The project is split into two Gradle modules. The `:app` module contains all screens, ViewModels, repositories, and business logic. The `:designsystem` module is a standalone library with all reusable UI components and the app theme.

Each screen follows the **Contract pattern**: three files per feature (`*Screen.kt`, `*ViewModel.kt`, `*Contract.kt`) with typed State, Event, and Effect classes. Every ViewModel extends `BaseViewModel<S, E, Ef>`, which enforces this structure.

```
android-mono-task/
├── app/                         # Main application module
│   └── src/main/java/dev/sagi/monotask/
│       ├── data/                # Data layer
│       │   ├── model/           # Data models: Task, User, Workspace, etc.
│       │   └── repository/      # Firebase Firestore repository implementations
│       ├── di/                  # Hilt dependency injection modules
│       ├── domain/              # Business logic layer
│       │   ├── repository/      # Repository interfaces (injected by ViewModels)
│       │   ├── service/         # Domain services: XpEngine, TaskSelector
│       │   └── usecase/         # Use cases: CompleteTaskUseCase, SnoozeTaskUseCase
│       ├── ui/                  # Presentation layer
│       │   ├── auth/            # Login and authentication flow
│       │   ├── brief/           # Daily brief screen
│       │   ├── common/          # BaseViewModel and shared screen components
│       │   ├── focus/           # Focus screen
│       │   ├── kanban/          # Kanban board screen
│       │   ├── onboarding/      # First launch onboarding
│       │   ├── profile/         # User profile screen
│       │   ├── settings/        # Settings screen
│       │   ├── shell/           # App's UI shell: main scaffold, top and bottom bars
│       └── └── statistics/      # Statistics and charts screen
└── designsystem/                # Shared design system module
    └── src/main/java/dev/sagi/monotask/designsystem/
        ├── animation/           # Shared transitions, gestures, nav animations
        ├── components/          # Reusable Compose components
	    ├── gesture/             # Swipe gestures
        └── theme/               # Color, typography, shape, custom tokens
```

<!-- DEMONSTRATION -->
## 5 Demonstration

<!-- DEMO_VIDEO_URL -->

> Demo video coming soon.

<!-- GETTING STARTED -->
## 6 Getting Started

### 6.1 Install the App (Easy)

Download the latest APK from the [Releases](https://github.com/sagieinav/android-mono-task/releases) page and install it on any Android device running API 26+.

> **Note:** You may need to allow installation from unknown sources in your device settings.

### 6.2 Build from Source

For developers who want to import and run the project locally.

**Prerequisites**
- **Android Studio**: Ladybug or newer (https://developer.android.com/studio)
- **Kotlin**: 2.x
- **Min SDK**: 26 (Android 8.0)

**Steps**

1. Clone the repo
```
git clone https://github.com/sagieinav/android-mono-task.git
```

2. Open the project in Android Studio

3. Set up Firebase
	- Create a Firebase project at https://console.firebase.google.com
	- Enable Authentication (Google sign-in) and Firestore
	- Download `google-services.json` and place it in the `app/` folder

4. Run on an emulator or physical device (API 26+)

<!-- USAGE -->
## 7 Usage

**How to Use MonoTask:**

1. **Sign up or log in** with a Google account
2. **Create a workspace** and add your tasks with a title, priority, and optional details
3. **Start focusing**: the app picks your next task based on priority
4. **Complete the task** to earn XP and build your streak
5. **Check your stats** on the Statistics screen to review your productivity over time
6. **Invite friends** and follow each other's progress and activity


<!-- ROADMAP -->
## 8 Roadmap

- [ ] Full onboarding flow for new users
- [ ] Drag & Drop in the kanban screen
- [ ] Sound effects
- [ ] Task filtering
- [ ] Offline support with local cache


<!-- LEARNING SOURCES -->
## 9 Learning Sources
I've developed this app with Jetpack Compose, following all structural patterns that go with it.
As this was the first time I was exposed to Compose, I had to learn its fundamentals by myself, as well as learning lots of things on-the-fly.

For the fundamentals, I've watched the YouTube videos of Google's official course: [Jetpack Compose for Android Developers](https://developer.android.com/courses/jetpack-compose/course).

For enriching my knowledge further, I watched some YouTube videos of the excellent [Philipp Lackner](https://www.youtube.com/@PhilippLackner), along with learning from documentation, hands-on practice, and active conversation with LLM.


<!-- ASSETS AND CREDITS -->
## 10 Assets and Credits

- **[Haze](https://github.com/chrisbanes/haze)** by Chris Banes: blur and glassmorphism effects used in the UI
- **[datetime-wheel-picker](https://github.com/darkokoa/datetime-wheel-picker)** by darkokoa: wheel-style date and time picker for task scheduling
- **[AwesomeUI](https://github.com/ArcaDone/AwesomeUI)** by Danilo Arcadipane: baseline of the statistics components
- **[Notion Club](https://www.figma.com/community/file/1146080946962518693)** by Zafar Ismatullaev: illustrations, used for empty states.
- **[Firebase](https://firebase.google.com)**: authentication (Google sign-in) and Firestore real-time database


<!-- CONTRIBUTORS -->
## 11 Contributors

<div align="center">
  <a href="https://github.com/sagieinav/android-mono-task/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=sagieinav/android-mono-task" alt="contrib.rocks image" />
  </a>
  </br>
  Sagi Einav
</div>


## 12 License

Distributed under the MIT License. See `LICENSE` for more information.

