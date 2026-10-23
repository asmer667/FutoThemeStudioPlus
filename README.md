# FutoThemeStudioPlus (Kotlin)

Android-native theme editor starter built with Kotlin + Jetpack Compose.

## Features in this project
- Live keyboard preview and preset color palette
- Key shape selector (preview) and theme name/author fields
- Import a button image and background image from Android's document picker
- Export a ZIP with `theme.txt`, referenced FUTO assets, and README
- GitHub Actions workflow to build a debug APK

## Build on GitHub
1. Upload this project folder's contents to a GitHub repository.
2. Open **Actions** and run **Build FUTO Theme Studio Plus** (or push to `main`).
3. Download the `FutoThemeStudioPlus-debug-apk` artifact from the completed workflow.

## Important compatibility limitation
The app bundles the reference FUTO Theme Configuration Format 1.0 template and assets from the supplied Aurora Glass example. It replaces the theme name, author, primary/press colors, optional default-key image and optional background image. It does **not** yet implement per-key selector generation for every key class, nor does selecting a geometric shape redraw every referenced border asset. Therefore 100% compatibility cannot honestly be guaranteed until the exported ZIP is imported and tested in the exact FUTO Keyboard version; use the official theme editor/docs to validate schema changes.

## References
- FUTO theme editor: https://keyboard.futo.tech/theme-editor/
- FUTO advanced theme docs: https://docs.keyboard.futo.tech/theme/advanced
