# FB Video Player - Android App

Facebook video links click කළාම clean video player එකේ open වෙන Android app.

## App කරන දේ

- Facebook video/watch/reel/fb.watch links intercept කරනවා
- Facebook UI (feed, ads, navigation) hide කරනවා  
- Clean black screen එකේ video play කරනවා
- **ඔබේ existing Facebook session/cookies use කරනවා** (extra data නෑ)
- Landscape fullscreen support
- fb.watch short links support

## Build කරන විදිය (Android Studio)

### Step 1 - Android Studio Install
https://developer.android.com/studio download කරන්න

### Step 2 - Project Open
1. Android Studio open කරන්න
2. "Open an Existing Project" click කරන්න
3. මේ folder select කරන්න: `FBVideoPlayer/`

### Step 3 - Build
1. Build menu → "Build APK(s)"
2. APK file: `app/build/outputs/apk/debug/app-debug.apk`

### Step 4 - Phone Install
```
adb install app/build/outputs/apk/debug/app-debug.apk
```
හෝ APK file phone එකට copy කරලා install කරන්න
(Settings → Install unknown apps → Allow)

## ගොනු ව්‍යූහය

```
FBVideoPlayer/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml       ← URL intercept rules
│       ├── java/com/fbvideoplayer/
│       │   ├── MainActivity.kt       ← URL router
│       │   └── VideoPlayerActivity.kt ← Video player
│       └── res/values/
│           └── themes.xml            ← Black theme
└── build.gradle
```

## Setup කරන විදිය

App install කළාම:
1. Facebook app open කරන්න
2. ඕනෑම video link long press කරන්න → "Copy Link"
3. Link paste කළාම "FB Video Player" select කරන්න
   **හෝ**
4. Settings → Apps → Set Default Apps → Browser හෝ Links → fb.watch → FB Video Player

## ගැටලු

**"Facebook session නෑ" / Login ගන්නවා:**  
→ App first time open කළාම Facebook login කරන්න. Cookies save වෙනවා.

**Video play වෙන්නේ නෑ:**  
→ Internet connection check කරන්න. Facebook cookies expire වෙලා නම් re-login.

**Short fb.watch links work නොවෙනවා:**  
→ AndroidManifest.xml හි intent-filter correctly set වෙලා ද check කරන්න.
