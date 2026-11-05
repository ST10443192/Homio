# Homio – Rental Property Management Application

## About the Project

**Homio** is a Kotlin-based Android application designed to simplify rental property management by providing a digital platform where landlords and tenants can manage rental-related activities.

The application aims to improve communication, streamline property management processes, and make it easier to manage rental information, payments, and maintenance requests.

## Project Objectives

- Simplify rental property management.
- Improve communication between landlords and tenants.
- Provide a convenient platform for managing rental properties.
- Allow tenants to submit and track maintenance requests.
- Support efficient management of rental information.

## &#x20;Key Features

The application is being developed to support the following functionality:

- **User Management:** Support for landlord and tenant accounts.
- **Property Management:** Manage rental property information.
- **Rent Management:** Support rental payment management.
- **Maintenance Requests:** Allow tenants to report maintenance issues.
- **Communication:** Improve interaction between landlords and tenants.
- **Authentication:** Provide user access through the application's login functionality.

## &#x20;Technologies Used

| Technology     | Purpose                                      |
| -------------- | -------------------------------------------- |
| Kotlin         | Main programming language                    |
| Android Studio | Android development environment              |
| XML            | User interface layouts                       |
| Gradle         | Build automation and dependency management   |
| Git            | Version control                              |
| GitHub         | Source code hosting                          |
| JUnit          | Unit testing                                 |
| GitHub Actions | Continuous Integration and automated testing |

## Development Environment

- Android Studio
- Android SDK
- JDK 21
- Gradle
- Windows

## Testing

Homio includes automated testing to help ensure that the application's functionality works as expected.

The project uses:

- **Unit Tests:** Test individual functions and application logic.
- **Instrumented Tests:** Test application functionality on an Android device or emulator.

### Running Unit Tests

Open the project in Android Studio and navigate to the Terminal.

Run:

```powershell
.\gradlew.bat testDebugUnitTest
```

### Viewing Test Reports

After a successful test run, reports can be found at:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

## Continuous Integration (CI)

The project is being configured to use **GitHub Actions** for continuous integration.

The intended workflow will automatically:

1. Trigger when code is pushed to the main branch.
2. Set up the Java development environment.
3. Build the Android application.
4. Execute automated unit tests.
5. Report whether the build and tests pass or fail.

Workflow configuration:

```text
.github/workflows/android-ci.yml
```

## Project Structure

```text
Homio/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   ├── res/
│       │   └── AndroidManifest.xml
│       │
│       ├── test/
│       │   └── java/
│       │
│       └── androidTest/
│           └── java/
│
├── .github/
│   └── workflows/
│       └── android-ci.yml
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/ST10443192/Homio.git
```

### 2. Open the Project

- Open Android Studio.
- Select **Open**.
- Navigate to the cloned Homio project folder.
- Allow Gradle to synchronise the project.

### 3. Build and Run

Connect an Android device or start an emulator.

Select the app configuration and click **Run** in Android Studio.

### 4. Run Automated Tests

In the Android Studio Terminal, execute:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Future Improvements

- Enhance the user interface and user experience.
- Improve landlord and tenant communication.
- Expand rental payment functionality.
- Add more automated unit and instrumented tests.
- Complete GitHub Actions integration.
- Improve application security and performance.

## Author

**Developer:** Homio Project Developer

##  License

This project is developed for educational and demonstration purposes.

---

**Homio – Making Rental Property Management Simpler.** 
