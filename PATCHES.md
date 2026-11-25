# Patches

> Generated from `patches-list.json` — **v1.0.0-dev.1** (`dev`) · **11 patches** across **1 app** · back to [README](README.md)

---

## Globoplay (Android TV) (com.globo.globotv)

**Supported versions:** `2.358.0`

| Patch | Details |
|---|---|
| **Clone Globoplay** | Renames the package to com.globo.globotv.mod and the launcher label to "Globoplay Mod" so the patched app installs alongside the stock app with its own data and login. Expected limits: Google sign-in, Play Billing, Firebase (including Remote Config) and Play Integrity are keyed to the original package name and signature and may not work in the clone. |
| **Disable Firebase reporting** | Stops the app force-enabling Crashlytics collection and disables Firebase Analytics, Crashlytics and Performance reporting via manifest flags. |
| **Disable live ad insertion** | Keeps live channels on the plain broadcast stream instead of a Google Dynamic Ad Insertion session, so no ads are substituted and no viewer identifiers go to Google for it. Commercials that are part of the broadcast itself remain. |
| **Disable screensaver** | Disables the in-app screensaver and its background content worker. |
| **Disable telemetry** | Disables Horizon, Firebase Analytics wrapper, NPAW, AppsFlyer, GloboID TrackID and player telemetry plugins, blocks telemetry hosts in OkHttp, and stops the app's A/B-test requests that carry account identifiers.<br><sub>Options: Block player telemetry plugins</sub> |
| **Hide ads** | Disables preroll/midroll, pause, takeover, frame and binge ads, removes the Google Mobile Ads init provider and blocks the ad-configuration API (multicloud-ads-api.globo.com). |
| **Hide polls** | Disables interactive poll panels via the remote-config flag. |
| **Hide shoppable overlays** | Disables shoppable product overlays via the remote-config flag. |
| **Hide welcome ads** | Disables the welcome/splash ad on Android TV via the remote-config flag. |
| **Set region** | Overrides the coordinates used for Globo's regional affiliate lookup and for the video-session location. Choose one of the 117 known affiliates or custom coordinates. Confirmed on a device: the programme guide and the live regional stream both followed the injected coordinates (tested for two affiliates from one exit location).<br><sub>Options: Regional affiliate, Custom latitude (only for custom coordinates), Custom longitude (only for custom coordinates)</sub> |
| **Trust custom certificate authority** | Debug/testing patch: trusts an additional certificate authority so the app's own HTTPS traffic can be inspected with a proxy such as mitmproxy. Weakens TLS verification; do not use for normal viewing. If a CA certificate file is given it is embedded in the APK and trusted alongside the system store; otherwise the user CA store is trusted.<br><sub>Options: CA certificate (PEM)</sub> |

---
