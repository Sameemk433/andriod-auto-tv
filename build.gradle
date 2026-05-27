# AutoPlay TV - Android TV Auto-Launch Video Player

**Plays your chosen videos automatically when TV boots. Supports USB drives, local storage, looping, and playlists.**

Built for Android TV / Android TV Boxes (Android 7.0 - 14+)

### Features
- ✅ **Auto-launch on boot** - Starts automatically when TV powers on
- ✅ **Pick videos from USB or internal storage** - Uses Android file picker
- ✅ **Loop single video or entire playlist**
- ✅ **Multiple video playlist** - plays in order
- ✅ **Remembers your selection forever** - even after reboot
- ✅ **TV remote friendly** - D-pad navigation
- ✅ **Keeps screen on**

---

## How to Build

1. Open **Android Studio** (Hedgehog or newer)
2. File > Open > Select `AutoPlayTV` folder
3. Wait for Gradle sync
4. Build > Build APK(s)

Install APK on your TV via USB or ADB:
```bash
adb connect TV_IP:5555
adb install app-debug.apk
```

### IMPORTANT: Enable Auto-Start

After installing, open the app **once** and select your videos.

Then on most TVs/boxes you need to allow autostart:
- **Xiaomi/Mi Box**: Settings > Apps > Permissions > Autostart > Enable AutoPlay TV
- **Realme/OnePlus TV**: Settings > Apps > Special Access > Display over other apps
- **Generic Android Box**: Settings > Apps > AutoPlay TV > Allow "Start in background"
- **Sony/Android TV**: Usually works automatically after first launch

Give it "All files access" if prompted for USB.

---

## How to Use

1. Open app
2. Click **"Select Videos"** (using remote OK button)
3. Browse to USB drive or internal storage
4. Long-press OK to select multiple videos, then click "Open"
5. Toggle **"Loop"** ON for continuous playback
6. Click **"Save & Play"**

Next reboot: app will auto-start and play immediately.

To change videos: reopen app and select new ones.

---

## Files Included
- `MainActivity.kt` - Player + UI
- `BootReceiver.kt` - Starts app on boot
- `AndroidManifest.xml` - Permissions for boot + TV