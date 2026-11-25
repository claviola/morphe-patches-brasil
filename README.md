# 🇧🇷🧩 Patches for Brazilian apps

Patches for Brazilian Android and Android TV apps, for use with
[Morphe](https://morphe.software). This is an independent project and is not affiliated
with or endorsed by the Morphe project or by the publishers of the apps it targets.

## ❓ About

The patches remove telemetry, ads and features unrelated to what each app is for, without
unlocking paid content or bypassing DRM.

Supported apps so far:

- Globoplay (Android TV)

More apps will be added over time.

### How to use these patches

Click the button below, or manually add this repo url to Morphe Manager: https://github.com/claviola/morphe-patches-brasil

<a href="https://morphe.software/add-source?github=claviola/morphe-patches-brasil">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

## 🩹 Patches list

<!-- PATCHES_START -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release. -->

#### The list of patches is shown here after the first release.

<!-- PATCHES_END -->

### 🛠️ Building locally

- Requires JDK 21, the Android SDK, and a GitHub token with the `read:packages` scope
  (`gpr.user` / `gpr.key` Gradle properties, or the `GITHUB_ACTOR` / `GITHUB_TOKEN`
  environment variables).
- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

### 🧑‍💻 Development

- Make all changes on the `dev` branch and merge to `main` (no squash) for a stable release.
- Use semantic commit messages: `feat:`, `fix:`, `chore:`.
- Releases are created only by `release.yml`. Do not edit generated files by hand:
  `patches-list.json`, `patches-bundle.json`, `PATCHES.md`, `CHANGELOG.md`.
- Each app has its own package under `patches/src/main/kotlin/brasil/morphe/patches/<app>/`
  and its own notes under `docs/<app>/`.

## 📜 License

These patches are licensed under the [GNU General Public License v3.0](LICENSE).
See the [NOTICE](NOTICE) for the naming terms inherited from the Morphe project.
