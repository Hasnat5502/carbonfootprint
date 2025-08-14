# Carbonfootprint (Python + Kivy)

A Kivy port of the Android Carbonfootprint app. It replicates the core screens and flows: splash, onboarding, sign up, sign in, home, dashboard, survey, progress, search, and action cards. Firebase Authentication and Realtime Database are used.

## Prerequisites
- Python 3.10+
- Linux/macOS/Windows desktop for development

## Setup
```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\\Scripts\\activate
pip install -r requirements.txt
python main.py
```

## Firebase configuration
The app reads `firebase_config.json` which was derived from the repo's `google-services.json`.

If you want to use your own Firebase project, update:
- apiKey
- authDomain
- databaseURL
- projectId
- storageBucket
- messagingSenderId
- appId

## Android APK (optional)
Use Buildozer in a Linux environment:
```bash
pip install buildozer
buildozer init  # if you modify spec
buildozer -v android debug
```
The provided `buildozer.spec` sets required permissions and Python dependencies.

## Notes
- The Kivy UI aims to mirror the Android layouts and navigation. Some complex survey layouts are simplified while preserving functionality.
- Dashboard reads category emissions from `surveys/{category}/{uid}/annualEmissions` and user info from `Users/{uid}` like the Android app.