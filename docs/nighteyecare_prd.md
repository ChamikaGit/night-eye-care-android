# NightEyeCare - Blue Light Filter Android App
## Product Requirements Document (PRD)

---

## 1. Product Overview

### App Name: NightEyeCare
**Tagline:** "Protect Your Eyes, Improve Your Sleep"

**Purpose:** A comprehensive blue light filter application that reduces eye strain and improves sleep quality by applying customizable color temperature overlays and screen dimming features.

**Target Audience:** 
- Users who spend long hours on mobile devices
- People with sleep disorders or eye strain issues
- Night shift workers and students
- Health-conscious smartphone users

---

## 2. Technical Specifications

### Platform & Compatibility
- **Platform:** Android Native (Kotlin)
- **Minimum SDK:** API 29 (Android 10)
- **Target SDK:** API 35 (Android 15)
- **Architecture:** Simple MVVM (UI + Data layers only, no Domain layer)
- **Database:** Room Database (Local Storage)

### Required Permissions
- **SYSTEM_ALERT_WINDOW** (Draw over other apps) - For blue light overlay
- **FOREGROUND_SERVICE** - For auto-timer and persistent filtering
- **WAKE_LOCK** - For scheduled operations
- **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS** - For reliable background operation
- **POST_NOTIFICATIONS** - For foreground service notifications
- **BOOT_COMPLETED** - For auto-start after device restart

---

## 3. Design System

### Color Palette
- **Primary Background:** `#122117`
- **Accent Color:** `#38E078` (Green for buttons, icons, widgets)
- **Text Color:** `#FFFFFF` (White for contrast)
- **Secondary Text:** `#A0A0A0`
- **Card Background:** `#1E2E23`

### Typography
- **Primary Font:** Poppins (all text elements)
- **Font Weights:** Regular (400), Medium (500), SemiBold (600)

### Component Guidelines
- **Buttons:** Rounded corners (12dp radius), height 48dp
- **Cards:** Rounded corners (16dp radius), elevation 4dp
- **Seekbars:** Custom green accent, rounded thumb
- **Icons:** 24dp standard size, green tint for active states

---

## 4. Core Features & Functionality

### 4.1 Blue Light Filter System

#### Color Temperature Presets
| Preset | Color Temperature | RGB Overlay | Use Case |
|--------|------------------|-------------|----------|
| **Candle** | 1800K | `#FF8C00` (Dark Orange) | Deep night reading |
| **Sunset** | 2000K | `#FF7F50` (Coral) | Evening relaxation |
| **Lamp** | 2700K | `#FFD700` (Warm Yellow) | Indoor evening |
| **Night Mode** | 3200K | `#FFA500` (Light Orange) | General night use |
| **Room Light** | 3400K | `#FFFF99` (Light Yellow) | Bright indoor |
| **Sun** | 5000K | `#FFFFFF` (Minimal filter) | Daytime use |

#### Intensity Control
- **Range:** 0-100%
- **Implementation:** Alpha blending of color overlay
- **Default:** 50% for each preset
- **UI:** Horizontal seekbar with percentage display

#### Screen Dim Feature
- **Range:** 0-100% (independent of color filter)
- **Implementation:** Additional black overlay with alpha
- **Default:** 0% (no dimming)
- **UI:** Separate horizontal seekbar

### 4.2 Auto-Timer Scheduling
- **Single Schedule:** Start time and End time selection
- **Time Format:** 12-hour format with AM/PM
- **Default Schedule:** 7:00 PM to 7:00 AM
- **Background Operation:** Foreground service with battery optimization handling
- **Auto-restart:** Survives app closure and device reboot

### 4.3 Information & Education System
- **Preset Information:** Dedicated screen explaining each color temperature
- **Blue Light Education:** Tutorial explaining blue light effects on eyes and sleep
- **Scientific Backing:** User-friendly explanations without complex jargon

---

## 5. Screen Flow & Navigation

### 5.1 First Launch Flow
```
Splash Screen (2s)
    ↓
Language Selection
    ↓
Tutorial Screen (3 pages)
    ↓
Permission Request
    ↓
Home Screen
```

### 5.2 Subsequent Launch Flow
```
Splash Screen (2s)
    ↓
Home Screen
```

### 5.3 Navigation Structure
```
Home Screen
├── Settings
│   ├── Language
│   ├── Battery Optimization
│   ├── Feedback
│   └── Privacy Policy (WebView)
├── Color Temperature Info
├── Tutorial (accessible anytime)
├── About Us
├── FAQ
└── Terms & Conditions (WebView)
```

---

## 6. Detailed Screen Specifications

### 6.1 Home Screen
**Layout Components:**
- **Header:** App logo + title, settings icon (top-right)
- **Main Toggle:** Large rounded button (Enable/Disable filter)
- **Status Indicator:** Current filter state and active preset
- **Preset Section:** Horizontal scrollable icons for 6 presets
- **Intensity Control:** Seekbar (0-100%) with current value
- **Screen Dim Control:** Separate seekbar (0-100%) with current value
- **Schedule Section:** Toggle for auto-timer + time display
- **Quick Actions:** Info button, tutorial access

### 6.2 Settings Screen
**Components:**
- **Language Selection:** Dropdown with 9 languages
- **Battery Optimization:** Button to request battery optimization exemption
- **Notifications:** Toggle for foreground service notifications
- **Feedback:** Email/form submission
- **Privacy Policy:** WebView link
- **App Version:** Display current version

### 6.3 Tutorial Screen (3 Pages)
**Page 1: Blue Light Introduction**
- What is blue light?
- Sources of blue light exposure

**Page 2: Health Impact**
- Eye strain and fatigue effects
- Sleep disruption science
- Benefits of blue light filtering

**Page 3: App Features**
- How to use NightEyeCare
- Preset explanations
- Scheduling benefits

### 6.4 Color Temperature Info Screen
**Content Structure:**
- **Preset Cards:** Each preset with icon, name, and description
- **Candle (1800K):** "Perfect for late-night reading, mimics candlelight warmth"
- **Sunset (2000K):** "Ideal for evening wind-down, reduces sleep disruption"
- **Lamp (2700K):** "Comfortable indoor evening use, reduces eye strain"
- **Night Mode (3200K):** "Balanced night protection without color distortion"
- **Room Light (3400K):** "Suitable for bright indoor environments"
- **Sun (5000K):** "Minimal filtering for daytime use"

---

## 7. Technical Implementation Details

### 7.0 Architecture Overview
**Simple MVVM Structure:**
```
UI Layer (Activities, Fragments, Compose)
    ↓
ViewModels (Business Logic)
    ↓
Repository (Data Access Abstraction)
    ↓
Data Layer (Room Database + Shared Preferences)
```

**Benefits for Frontend-focused App:**
- Simplified structure for UI-heavy application
- Direct ViewModel to Repository communication
- No unnecessary Domain layer abstraction
- Faster development and easier maintenance

### 7.1 Overlay System
**Implementation:** `WindowManager` with `TYPE_APPLICATION_OVERLAY`
**Overlay Behavior:**
- Covers entire screen including status bar (if possible)
- Non-intrusive touch events (allows normal app interaction)
- Layered system: Color filter + Screen dim (if both enabled)
- Real-time updates when settings change

### 7.2 Foreground Service
**Service Features:**
- Persistent notification with app icon
- **Notification Actions:** Pause/Resume, Stop, Open App (horizontal layout)
- Notification importance: `IMPORTANCE_LOW` (no sound/vibration)
- Auto-start capability after device reboot
- Battery optimization bypass request

### 7.3 Scheduling System
**Components:**
- `AlarmManager` for precise timing
- `BroadcastReceiver` for schedule triggers
- Persistent storage of schedule preferences
- Handles device timezone changes

### 7.4 Data Storage (Room Database)
**Entities:**
```kotlin
@Entity
data class FilterSettings(
    val isEnabled: Boolean,
    val selectedPreset: String,
    val intensity: Int,
    val dimLevel: Int,
    val scheduleEnabled: Boolean,
    val scheduleStartTime: String,
    val scheduleEndTime: String
)

@Entity
data class AppPreferences(
    val selectedLanguage: String,
    val onboardingCompleted: Boolean,
    val batteryOptimizationShown: Boolean
)
```

---

## 8. Localization Structure

### 8.1 Supported Languages
- **English** (en) - Default
- **French** (fr)
- **Portuguese** (pt)
- **Hindi** (hi)
- **Urdu** (ur)
- **Chinese Simplified** (zh-CN)
- **Korean** (ko)
- **Japanese** (ja)
- **Thai** (th)

### 8.2 Localization Files Structure
```
res/
├── values/ (English - default)
├── values-fr/ (French)
├── values-pt/ (Portuguese)
├── values-hi/ (Hindi)
├── values-ur/ (Urdu)
├── values-zh/ (Chinese)
├── values-ko/ (Korean)
├── values-ja/ (Japanese)
└── values-th/ (Thai)
```

---

## 9. User Experience & Interaction

### 9.1 Onboarding Experience
- **Duration:** 3-5 minutes
- **Mandatory:** Cannot skip tutorial on first launch
- **Permission Explanation:** Clear dialog explaining why "Draw over apps" is needed
- **Visual Guide:** Screenshots and animations showing app benefits

### 9.2 Daily Usage Flow
1. **Quick Enable:** Single tap main toggle
2. **Preset Selection:** Horizontal icon tap
3. **Fine-tuning:** Intensity and dim sliders
4. **Set Schedule:** Time picker with save
5. **Background Operation:** Automatic with notification

### 9.3 Accessibility Considerations
- High contrast color scheme (dark background, bright green accents)
- Large touch targets (minimum 48dp)
- Clear iconography with text labels
- Readable font sizes (minimum 16sp)

---

## 10. Quality Assurance

### 10.1 Testing Requirements
- **Unit Tests:** Core logic and data handling
- **UI Tests:** Critical user flows
- **Device Testing:** Various Android versions (API 29-35)
- **Performance Testing:** Battery usage and memory leaks
- **Permission Testing:** All permission scenarios

### 10.2 Edge Cases
- Device rotation handling
- App killed by system
- Permission revocation
- Battery optimization interference
- Multiple overlays conflict

---

## 11. Success Metrics & Analytics (Future)

### 11.1 Key Performance Indicators
- Daily Active Users (DAU)
- Session Duration
- Feature Adoption Rate
- Schedule Usage Rate
- User Retention (7-day, 30-day)

### 11.2 User Behavior Tracking
- Most used presets
- Average intensity settings
- Schedule setup completion rate
- Tutorial completion rate

---

## 12. Maintenance & Updates

### 12.1 Regular Updates
- Android OS compatibility
- Bug fixes and performance improvements
- New preset additions
- UI/UX enhancements

### 12.2 User Feedback Integration
- In-app feedback form
- Play Store review monitoring
- Feature request collection
- Usability issue tracking

---

## 13. Legal & Compliance

### 13.1 Required Documents
- Privacy Policy (data collection and usage)
- Terms and Conditions (app usage terms)
- Open Source Licenses (if applicable)

### 13.2 Data Protection
- Local storage only (no cloud data)
- No personal data collection
- No analytics or tracking (initially)
- User consent for permissions

---

**Document Version:** 1.0
**Last Updated:** July 22, 2025
**Created By:** Product Development Team
**Approval Status:** Ready for Development