# Passport Photo 8 (Android project)

Offline-first Android app source project. It lets a user select an image, manually crop/reposition it to a 7:9 portrait ratio, and save a PDF with eight 35 × 45 mm copies on a 10 × 15 cm landscape sheet.

## Build the APK in the cloud with GitHub Actions (no Android Studio required)

1. Download and extract this ZIP.
2. Sign in to https://github.com/ and create a **new repository**. A private repository is fine.
3. On the new repository page, choose **Add file → Upload files**.
4. Upload the *contents* of this `PassportPhoto8` folder, including the hidden `.github` folder. The repository root should contain `build.gradle`, `settings.gradle`, `gradle.properties`, `app/`, and `.github/`.
5. Commit the upload to the `main` branch. The workflow in `.github/workflows/build-apk.yml` should start automatically.
6. Open the repository's **Actions** tab, select the latest **Build Android APK** run, and wait for it to finish with a green check.
7. Open that run and find **Artifacts** near the bottom. Download `PassportPhoto8-debug-apk` and extract the downloaded artifact ZIP. It contains `app-debug.apk`.
8. Transfer `app-debug.apk` to your Android phone and tap it to install. If prompted, allow your browser or file manager to install unknown apps.

If the workflow does not start automatically, open **Actions → Build Android APK → Run workflow** and choose `main`.

The first cloud build downloads Android SDK components and may take several minutes. GitHub Actions builds this project using Java 17 and Gradle 8.7. No photos are uploaded by the app; image processing is local. The GitHub build itself needs internet access.

## Build locally (optional)

Open this folder as an existing project in Android Studio and build the debug APK. The output path is `app/build/outputs/apk/debug/app-debug.apk`.

## Use

- Tap **Select and crop photo**.
- Drag the image inside the crop frame and use zoom controls to position the face.
- Tap **Use this crop**.
- Tap **Save print-ready PDF** and choose where to save it.
- Print the PDF at **Actual size / 100%**, with **Fit to page disabled**. Confirm the paper is 10 × 15 cm (4 × 6 in) and landscape.

## Layout and dimensions

- Sheet: 150 × 100 mm, landscape.
- Photos: 35 × 45 mm each.
- Arrangement: 4 columns × 2 rows, centered, with approximately 5 mm margins on each side.
- No internet permission is requested; image processing is local.

Important: Indian photo requirements can vary by application route or receiving authority. The Passport Seva instruction booklet specifies a 4.5 cm length × 3.5 cm width colour photo for certain paper applications. Verify the requirements for the specific office/service before printing. Source: https://passportindia.gov.in/pdf/ApplicationformInstructionBooklet-V3.0.pdf
