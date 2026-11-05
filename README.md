# Homio — Rental Property Management App

> A rental property management application designed to support communication and day-to-day rental administration between landlords and tenants in South Africa.

**Android application ID / namespace:** `com.example.homio`  
**Primary mobile technology:** Kotlin with Android XML layouts  
**Backend services:** Firebase and a Node.js API  
**API deployment target:** Vercel (`https://homio-api.vercel.app`)

---

## Table of contents

1. [Application overview](#1-application-overview)
2. [Problem statement and purpose](#2-problem-statement-and-purpose)
3. [Target users and objectives](#3-target-users-and-objectives)
4. [Core functionality](#4-core-functionality)
5. [Innovative features and value](#5-innovative-features-and-value)
6. [Design considerations](#6-design-considerations)
7. [Languages and localisation](#7-languages-and-localisation)
8. [Technology stack and architecture](#8-technology-stack-and-architecture)
9. [Data access and security considerations](#9-data-access-and-security-considerations)
10. [Project setup](#10-project-setup)
11. [API setup and deployment](#11-api-setup-and-deployment)
12. [Release notes](#12-release-notes)
13. [Testing and verification](#13-testing-and-verification)
14. [Limitations and outstanding work](#14-limitations-and-outstanding-work)
15. [AI tools: use and acknowledgement](#15-ai-tools-use-and-acknowledgement)
16. [References](#16-references)

---

## 1. Application overview

Homio is a rental property management app for tenants and landlords. Its purpose is to provide a shared digital environment in which the two user groups can access rental-related information and manage relevant tasks. The application is being developed for the South African context, with support planned for English, Afrikaans and isiZulu.

The Android application uses role-based navigation. After authentication, the app reads the signed-in user's `userType` from Firestore and directs the user to the appropriate landlord or tenant dashboard.

Homio also has a separate Node.js API project (`homio-api`). The API is intended to support backend operations such as maintenance and payment-related requests. It is deployed, or intended to be deployed, through Vercel.

## 2. Problem statement and purpose

Rental arrangements can involve information being exchanged across phone calls, messages, paper records and separate payment or maintenance processes. When information is scattered, tenants may be uncertain about rent details or the progress of a maintenance request, while landlords may find it difficult to keep property-related records and tenant communication organised.

Homio aims to bring relevant rental-management activities into one application. It is intended to make rental information easier to access, improve communication between tenants and landlords, and help each user see information relevant to their role.

The application is not intended to replace formal lease agreements, legal advice, banking services or the responsibilities of either party. It is a digital management aid.

## 3. Target users and objectives

### 3.1 Tenants

Tenants are users who rent a property managed through Homio. The tenant experience is intended to provide access to property and rental information, payment-related information, maintenance requests and relevant notifications.

### 3.2 Landlords

Landlords are users who manage one or more rental properties. The landlord experience is intended to help them manage property records, link tenants to properties, and review relevant maintenance and payment activity.

### 3.3 Project objectives

- Provide separate, role-appropriate experiences for landlords and tenants.
- Authenticate users and identify their role.
- Associate tenants with the correct rental property.
- Present rent, due-date and deposit information from the tenant's linked property.
- Support maintenance and payment workflows through the backend.
- Restrict access so that users only see information they are authorised to access.
- Make the interface available in English, Afrikaans and isiZulu.
- Provide a maintainable structure that can be extended as the application develops.

## 4. Core functionality

The following table separates functionality evidenced in the development notes from functionality described as the next development step.

| Functionality | Description | Status in available project notes |
|---|---|---|
| User registration and login | Firebase Authentication is used for account authentication. | Previously implemented in the Android project; verify in the current build. |
| Role-based navigation | Reads `userType` from Firestore and routes users to the landlord or tenant dashboard. | Confirmed in the existing `MainActivity` design. |
| Landlord dashboard | A landlord-specific entry point for property-management activities. | Dashboard/activity exists in the project structure; verify individual dashboard functions. |
| Tenant dashboard | A tenant-specific entry point for rental activities. | Dashboard/activity exists in the project structure; verify individual dashboard functions. |
| Property-to-tenant linking | A landlord enters the email address used by a tenant to register; the API links the tenant to the property. | Planned next feature; not confirmed as implemented. |
| Maintenance requests | API route and middleware structure includes maintenance functionality. | Backend route is referenced; end-to-end workflow needs verification. |
| Payments | API route and middleware structure includes payment functionality. | Backend route is referenced; end-to-end workflow needs verification. |
| Property-based rent information | Tenant Rent screen displays monthly rent, due date and deposit from the linked property. | Planned as part of property linking; not confirmed as implemented. |
| Landlord-specific ticket/payment access | Tickets and payments are associated with a landlord so that records can be scoped correctly. | Planned; not confirmed as implemented. |
| Notifications | Landlord alerts should be limited to the landlord's own property/tenant records. | Planned access-scoping change; verify notification implementation. |
| Language selection | Support for English, Afrikaans and isiZulu, with user-facing text stored in Android string resources. | Requirement identified; implementation and translation review must be verified. |

## 5. Innovative features and value

The following features are the main differentiators identified in the current development plan. They should be described as release innovations only once they have been implemented and tested.

### 5.1 Property-to-tenant linking by registered email

A landlord can add a tenant to a property by entering the email address the tenant used when registering. The API resolves the registered tenant and associates the tenant account with the selected property.

**Value:** This creates a clear relationship between a property and its tenant account, reducing the need to manage that association informally.

### 5.2 Property-aware rent information

After a tenant is linked to a property, the tenant's Rent screen can display the actual monthly rent, rent due date and deposit stored for that property rather than relying on placeholder values.

**Value:** The tenant sees rental information relevant to their own agreement, while the information can be maintained centrally against the property record.

### 5.3 Landlord-scoped maintenance and payment records

New maintenance tickets and payment records are intended to record the responsible landlord. Queries and notification listeners can then be restricted to the signed-in landlord's own records.

**Value:** This is an important privacy and data-isolation improvement, particularly where multiple landlords use the same system.

### 5.4 Three-language interface

Homio is intended to support English, Afrikaans and isiZulu. User-facing text should be moved out of layouts and Kotlin source files and into Android string resources, with a translation set for each supported language.

**Value:** Localisation makes the app more accessible to a wider range of South African users and avoids requiring users to navigate an interface in only one language.

### 5.5 Role-specific experience

The application distinguishes landlord and tenant users after authentication and routes each to a dashboard appropriate to their role.

**Value:** Each user group can focus on relevant tasks without being presented with unnecessary controls intended for the other group.

## 6. Design considerations

### 6.1 User-centred design

The application serves two user groups with different responsibilities. Tenant screens should prioritise clarity around the tenant's own property, rent information, maintenance and payments. Landlord screens should prioritise property and tenant administration, maintenance oversight and payment records.

### 6.2 Consistency and ease of navigation

Navigation, labels, buttons, form fields and feedback messages should behave consistently across the app. Similar actions should use similar layouts and terminology. Important actions should be easy to identify, and destructive or financially significant actions should require clear confirmation.

### 6.3 Readability and accessibility

- Use readable text sizes and sufficient colour contrast.
- Do not communicate important status using colour alone; include text labels or icons with accessible descriptions.
- Provide clear labels and helpful validation messages for form fields.
- Ensure controls have practical touch targets.
- Support Android system font scaling where possible.
- Avoid overcrowded screens and unnecessary decorative elements.

### 6.4 Localisation and language-aware design

All user-facing text should be stored in Android string resources rather than hardcoded in Kotlin files or XML layouts. Translations should preserve the meaning of the English source and fit the available screen space. Dates, currency and other locale-sensitive values should be formatted appropriately rather than assembled as fixed text.

### 6.5 Responsive Android layouts

Layouts should adapt to different Android screen sizes and orientations where supported. Use flexible layout constraints and avoid fixed dimensions that cause text clipping, overlapping controls or inaccessible content on smaller devices.

### 6.6 Privacy and trust

Rental information can be sensitive. The interface should make it clear which property or account a record belongs to. Tenant and landlord actions should be protected by authentication and server-side authorisation, not merely by hiding controls in the Android interface.

### 6.7 Error prevention and feedback

Forms should validate required fields and explain how to correct invalid input. Network failures, missing records and permission errors should be communicated in plain language. Loading indicators and success/failure feedback should be provided for asynchronous actions.

## 7. Languages and localisation

The required application languages are:

- English — default/source language.
- Afrikaans.
- isiZulu.

### 7.1 Android resource structure

Use Android's resource qualifier folders for translated strings:

```text
app/src/main/res/
├── values/
│   └── strings.xml          # Default English strings
├── values-af/
│   └── strings.xml          # Afrikaans translations
└── values-zu/
    └── strings.xml          # isiZulu translations
```

Example:

```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">Homio</string>
    <string name="welcome_message">Welcome to Homio</string>
    <string name="login">Log in</string>
</resources>
```

```xml
<!-- res/values-af/strings.xml -->
<resources>
    <string name="app_name">Homio</string>
    <string name="welcome_message">Welkom by Homio</string>
    <string name="login">Meld aan</string>
</resources>
```

```xml
<!-- res/values-zu/strings.xml -->
<resources>
    <string name="app_name">Homio</string>
    <string name="welcome_message">Siyakwamukela ku-Homio</string>
    <string name="login">Ngena ngemvume</string>
</resources>
```

These examples illustrate the resource approach and are not a complete translation set.

### 7.2 Localisation implementation checklist

- Move visible text from Kotlin files into `strings.xml`.
- Replace hardcoded XML `android:text`, hints, content descriptions, menu labels and accessibility text with `@string/...` references.
- Add matching string names to the Afrikaans and isiZulu resource files.
- Keep resource names identical across all language folders.
- Use formatted string resources for dynamic values, such as rent amounts and names.
- Do not translate technical identifiers, database field names, API paths or code constants.
- Test every screen in all three languages, including longer translated labels and validation messages.
- Ensure the selected language is applied consistently after navigation and app restart.

**Implementation note:** The project notes establish the three-language requirement, but do not prove that every hardcoded string has already been removed or that all translations have been completed and reviewed.

## 8. Technology stack and architecture

### 8.1 Android application

| Technology | Purpose |
|---|---|
| Kotlin | Main Android application programming language. |
| Android Studio | Android development environment. |
| XML layouts | UI layout definitions; the project is not based on Jetpack Compose. |
| AndroidX AppCompat | Activity and compatibility support. |
| Firebase Authentication | User registration and authentication. |
| Cloud Firestore | User records and application data, including the `userType` used for role routing. |
| SharedPreferences | Local storage for simple app preferences where used. |
| AppCompatDelegate | Theme handling for Light, Dark or System appearance where implemented. |

The Android project uses the application ID / namespace `com.example.homio`. The previously documented Gradle configuration uses compile SDK 36, target SDK 36, minimum SDK 24 and Java 11. Confirm these values against the current Gradle files before publishing.

### 8.2 Node.js API

The supplied backend notes describe a Node.js API project called `homio-api`. The intended server entry point is `src/server.js`.

Expected source structure from the supplied development notes:

```text
homio-api/
├── package.json
└── src/
    ├── constants.js
    ├── firebase.js
    ├── server.js
    ├── utils.js
    ├── middleware/
    │   ├── auth.js
    │   └── errors.js
    └── routes/
        ├── maintenance.js
        └── payments.js
```

The backend notes state that `src/server.js` loads the authentication and error middleware, the maintenance and payments routes, and shared Firebase, utility and constants modules.

The obsolete `src/app.js` and `src/index.js` files were identified as possible deployment entry-point conflicts because the deployment platform could select them before `src/server.js`. The recommended resolution was to remove those obsolete files if they contain no required code and make the package scripts explicitly start `src/server.js`.

### 8.3 Architecture overview

```text
Android app
   |
   |-- Firebase Authentication
   |
   |-- Cloud Firestore
   |
   |-- HTTPS requests
          |
          v
     Vercel-hosted Node.js API
          |
          |-- Authentication middleware
          |-- Error-handling middleware
          |-- Maintenance routes
          |-- Payments routes
          |
          v
       Firebase services
```

This is a high-level view based on the available project notes. Confirm the actual request flow and Firebase services used by each route in the source code.

## 9. Data access and security considerations

The property-linking work introduces important access-control requirements.

- A tenant should only be linked to the intended property.
- A landlord should only retrieve tenants and records belonging to properties they are authorised to manage.
- Maintenance tickets and payment records should carry sufficient ownership information to enforce landlord-level scoping.
- API endpoints must validate the authenticated identity and authorise access on the server.
- Firestore security rules should enforce the same access boundaries as the API.
- Notification listeners should query only records the signed-in user is authorised to receive.
- Never rely solely on client-side filtering or hidden UI elements for security.
- Do not store service-account credentials or private keys in source control.
- Keep secrets in the deployment platform's environment-variable configuration.
- Return useful error messages without exposing credentials, tokens or sensitive internal details.

The exact Firestore collection structure and security rules must be documented from the current source and Firebase console configuration. They are not inferred here.

## 10. Project setup

### 10.1 Android application

1. Install Android Studio and the required Android SDK components.
2. Open the Homio Android project in Android Studio.
3. Allow Gradle to synchronise.
4. Confirm that the Firebase configuration file is present and belongs to the correct Firebase project. Do not commit private credentials or configuration secrets to a public repository.
5. Confirm that the required Firebase products are enabled and configured.
6. Select an Android emulator or physical device.
7. Build and run the application.

The exact Gradle module names and Firebase setup steps may differ in the current repository. Follow the project's actual Gradle configuration.

### 10.2 Node.js API

1. Install a supported Node.js version.
2. Open a terminal in the `homio-api` directory.
3. Install the dependencies:

```bash
npm install
```

4. Confirm that `package.json` uses the intended server entry point:

```json
{
  "scripts": {
    "start": "node src/server.js",
    "dev": "node --watch src/server.js"
  }
}
```

5. Configure the required environment variables using the names expected by `src/firebase.js` and the other backend modules. Do not guess variable names; inspect the source code and deployment configuration.
6. Start the API locally:

```bash
npm start
```

7. Test the health endpoint:

```text
http://localhost:3000/health
```

The expected response described in the development notes is:

```json
{"status":"ok"}
```

Stop the local server with `Ctrl+C`.

## 11. API setup and deployment

The API deployment URL identified in the development notes is:

```text
https://homio-api.vercel.app
```

The health-check endpoint is:

```text
https://homio-api.vercel.app/health
```

The deployment troubleshooting notes recommend:

- Use `src/server.js` as the single intended Node.js entry point.
- Remove obsolete `src/app.js` and `src/index.js` files only after confirming that they contain no required work.
- Ensure the `start` script runs `node src/server.js`.
- Ensure the development script runs `node --watch src/server.js`.
- Test the API locally before pushing changes.
- Commit and push the changes so the deployment platform can redeploy.
- If deployment still fails, inspect the latest deployment/runtime logs and resolve the newest error rather than assuming the earlier CORS issue remains.

Example Git commands:

```bash
git add -A
git commit -m "Use server.js as API entry point"
git push
```

The health endpoint should be checked after deployment. A successful health response confirms that the health route is responding; it does not by itself prove that authentication, maintenance, payment or property-linking workflows work correctly.

## 12. Release notes

### Release history policy

The release notes below record the development changes and requirements evidenced in the available project history. They do not claim that proposed work has been completed. Before submitting or publishing the app, replace the provisional version labels and dates with the actual Git tags or release dates.

### Prototype — Initial application

**Baseline capabilities recorded**
- Homio was established as an Android rental-management application for South African tenants and landlords.
- Kotlin and Android Studio were selected for development.
- Firebase was selected as the backend platform.
- Separate tenant and landlord experiences were planned.
- Authentication and user-role handling were established as foundational requirements.

### Development update — Authentication and role navigation

**Changes recorded**
- Firebase Authentication was used for account authentication.
- The existing `MainActivity` checks Firebase Authentication and reads the user's `userType` from Firestore.
- Authenticated users are routed to `LandlordDashboardActivity` or `TenantDashboardActivity`.
- The Android project uses Kotlin activities and XML layouts rather than Jetpack Compose.

**Innovative value:** Role-aware navigation provides a different starting point for landlords and tenants, based on the responsibilities of each user group.

### Development update — User settings and appearance

**Changes previously specified in the project work**
- A settings experience was designed/supplied for Light, Dark and System theme choices.
- Profile editing was included in the settings work.
- Language preferences were considered as part of the settings experience.

**Status:** The earlier implementation discussion included code and resource guidance. Confirm the current app contains and correctly persists each setting before describing it as released.

### Current development update — Backend API structure and deployment troubleshooting

**Changes recorded in the supplied development notes**
- A separate `homio-api` Node.js backend was structured around `src/server.js`.
- Shared modules include Firebase integration, constants and utilities.
- Authentication and error-handling middleware are included in the described server structure.
- Maintenance and payments routes are included in the described backend structure.
- The Vercel deployment entry-point conflict involving obsolete `src/app.js` and `src/index.js` files was identified.
- The recommended package scripts explicitly start `src/server.js`.
- A `/health` endpoint is used to verify that the API process responds.

**Innovative value:** Separating API routes and middleware creates a clearer foundation for expanding rental-management operations and applying consistent authentication and error handling.

### Current development update — Multilingual support requirement

**New requirement**
- Support English, Afrikaans and isiZulu.
- Remove hardcoded user-facing text from Kotlin source and XML layouts.
- Store default English text in `res/values/strings.xml`.
- Add corresponding Afrikaans strings in `res/values-af/strings.xml`.
- Add corresponding isiZulu strings in `res/values-zu/strings.xml`.

**Innovative value:** Three-language support is intended to make Homio more inclusive and accessible to South African users. This is a new requirement in the available notes; mark it as released only after all screens and messages have been translated and tested.

### Planned next release — Property and tenant linking

**Planned changes**
- Allow a landlord to add a tenant to a property using the email address the tenant registered with.
- Link the tenant and property through the API.
- Associate new maintenance tickets and payment records with the responsible landlord.
- Restrict landlord ticket, payment and alert queries to that landlord's own records.
- Replace placeholder Rent screen information with the linked property's actual monthly rent, due date and deposit.

**Innovative value:** This release is intended to connect property, tenant, rent, maintenance and payment information through a consistent relationship. It also introduces more precise landlord-level data scoping.

**Release status:** The supplied conversation ends while this work is being prepared. It must be labelled *planned / in progress*, not completed, until implementation and tests confirm it.

## 13. Testing and verification

The following checks are recommended before calling a feature complete.

### Authentication and role access
- Register a tenant and a landlord account.
- Sign in with each account type.
- Confirm that each account opens the correct dashboard.
- Confirm that signing out prevents access to authenticated screens.

### Localisation
- Inspect all screens for hardcoded user-facing text.
- Switch the device/app language to English, Afrikaans and isiZulu.
- Confirm that buttons, hints, dialogs, errors, menus and notifications are translated.
- Check longer translations for clipping and layout problems.

### Property linking
- Link a tenant using the email address associated with their registered account.
- Test an email address that does not belong to a registered tenant.
- Test duplicate linking and attempts to link a tenant to an unintended property.
- Confirm that the tenant sees the correct property and rent details.

### Data scoping and security
- Create records for more than one landlord.
- Confirm that each landlord only sees their own tenants, maintenance tickets, payments and alerts.
- Attempt unauthorised API requests and verify that the server rejects them.
- Verify that Firestore rules enforce the intended access boundaries.

### API and deployment
- Confirm that `npm start` launches `src/server.js`.
- Confirm the local `/health` endpoint returns the expected response.
- Confirm the deployed `/health` endpoint responds.
- Test maintenance and payment routes with valid and invalid requests.
- Inspect deployment logs after each deployment.

## 14. Limitations and outstanding work

Based on the information currently available, the following items still require confirmation or completion:

1. Complete the three-language resource migration and translation review across every screen.
2. Confirm the language-selection mechanism and whether the chosen language persists after app restart.
3. Implement and test property-to-tenant linking by registered email.
4. Confirm the final Firestore data model for properties, tenants, maintenance tickets and payments.
5. Implement landlord ownership fields and enforce landlord-scoped access in API queries and Firestore rules.
6. Scope notification listeners to the authorised landlord's records.
7. Populate the tenant Rent screen from the linked property's actual rent, due date and deposit.
8. Test the Android app and backend together, including error and permission cases.
9. Confirm deployment environment variables and production security configuration.
10. Replace provisional release-note entries with verified release dates, version numbers and commit references.

## 15. AI tools: use and acknowledgement

AI tools were used as development-support resources during the planning, implementation and troubleshooting of Homio. ChatGPT and Claude were used to help reason through application structure, explain Kotlin/Android and Firebase concepts, suggest implementation approaches, review backend organisation, and assist with debugging and deployment issues. AI assistance also supported the preparation of development instructions and project documentation.

The tools were used to generate suggestions and explain possible solutions; their output was not treated as automatically correct. Suggested code and configuration changes were intended to be reviewed, adapted to the existing project and tested by the developer. For example, the backend deployment discussion identified a possible conflict between obsolete entry files and the intended `src/server.js` entry point. The proposed property-to-tenant linking, landlord-level data scoping and multilingual resource migration were treated as development tasks requiring implementation and verification, not as completed features merely because AI described them.

AI tools were not used as substitutes for the developer's responsibility to understand the application, make design decisions, verify functionality or evaluate security. Any AI-generated material retained in the project should be checked against the actual codebase and the relevant technical documentation. Where institutional rules require acknowledgement, the specific tool, purpose of use and date should be cited in the format prescribed by the institution. Any code, text or design generated with AI should be disclosed where required.

**Suggested acknowledgement references (adapt to the institution's required citation style):**
- OpenAI. (2026). *ChatGPT* [Large language model]. https://chatgpt.com/
- Anthropic. (2026). *Claude* [Large language model]. https://claude.ai/

## 16. References

- Android Developers. (n.d.). *Localise your app*. https://developer.android.com/guide/topics/resources/localization
- Android Developers. (n.d.). *String resources*. https://developer.android.com/guide/topics/resources/string-resource
- Firebase. (n.d.). *Firebase documentation*. https://firebase.google.com/docs
- Vercel. (n.d.). *Node.js on Vercel*. https://vercel.com/docs/functions/runtimes/node-js
- OpenAI. (2026). *ChatGPT* [Large language model]. https://chatgpt.com/
- Anthropic. (2026). *Claude* [Large language model]. https://claude.ai/

