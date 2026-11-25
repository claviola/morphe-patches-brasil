package brasil.morphe.patches.globoplay.shared

import app.morphe.patcher.Fingerprint

private const val REMOTE_CONFIG_CLASS =
    "Lcom/globo/globotv/remoteconfig/model/ConfigurationRemoteConfig;"

/**
 * Hook map 1.7 — remote-config gate for NPAW app analytics.
 */
object IsAppAnalyticsEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "isAppAnalyticsEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 5.1 — welcome-ads flag for Android TV.
 */
object IsWelcomeAdsTvEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "isWelcomeAdsTvEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 3.5 — pause-ad flag.
 */
object GetPauseAdEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "getPauseAdEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 3.5 — pause-ads takeover flag.
 */
object GetPauseAdsTakeoverEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "getPauseAdsTakeoverEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 3.5 — pause-ads takeover flag for live content.
 */
object GetPauseAdsTakeoverLiveEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "getPauseAdsTakeoverLiveEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 3.5 — frame-ads flag.
 */
object GetEnableFrameAdsFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "getEnableFrameAds",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 3.5 — binge-ads flag.
 */
object GetEnableBingeAdsFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "getEnableBingeAds",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 6.1 — shoppable overlays flag.
 */
object IsShoppableEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "isShoppableEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 7.1 — poll panel flag.
 */
object IsPanelPollEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "isPanelPollEnabled",
    returnType = "Z",
    parameters = listOf(),
)

/**
 * Hook map 1.13 — OkHttp Dns.SYSTEM lookup; the only other Dns
 * implementation delegates to this one. Also used by Hide ads 3.9.
 */
object OkHttpDnsLookupFingerprint : Fingerprint(
    strings = listOf("Broken system behaviour for dns lookup of "),
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/lang/String;"),
)

/**
 * Hook map 9.1 — screensaver flag. definingClass is required because a method
 * with the same name exists in another class.
 */
object IsScreenSaverEnabledFingerprint : Fingerprint(
    definingClass = REMOTE_CONFIG_CLASS,
    name = "isScreenSaverEnabled",
    returnType = "Z",
    parameters = listOf(),
)
