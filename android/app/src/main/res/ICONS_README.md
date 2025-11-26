# Launcher Icons Placeholder

These are placeholder icons. Replace with actual app icons before release.

## Required Icon Sizes

| Density | Launcher Icon | Round Icon |
|---------|--------------|------------|
| mdpi | 48x48 px | 48x48 px |
| hdpi | 72x72 px | 72x72 px |
| xhdpi | 96x96 px | 96x96 px |
| xxhdpi | 144x144 px | 144x144 px |
| xxxhdpi | 192x192 px | 192x192 px |

## Additional Required Assets

1. **Play Store Icon**: 512x512 px PNG
2. **Feature Graphic**: 1024x500 px PNG
3. **Adaptive Icon** (optional but recommended):
   - Foreground: 108x108 dp (432x432 px at xxxhdpi)
   - Background: Can be color or image

## Icon Generation Tool

Consider using Android Asset Studio or Flutter launcher_icons package:

```yaml
# pubspec.yaml
dev_dependencies:
  flutter_launcher_icons: ^0.13.1

flutter_icons:
  android: true
  ios: false
  image_path: "assets/icon/app_icon.png"
```

Run: `flutter pub run flutter_launcher_icons`
