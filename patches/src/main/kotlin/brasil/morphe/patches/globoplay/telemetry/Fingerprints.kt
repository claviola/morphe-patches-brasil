package brasil.morphe.patches.globoplay.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

private const val HORIZON_CLIENT = "Lcom/globo/horizonclient/HorizonClient;"
private const val GA_MANAGER = "Lcom/globo/globotv/googleanalytics/GoogleAnalyticsManager;"

/**
 * Hook map 1.1 — Horizon client single-event send.
 */
object HorizonSendEventFingerprint : Fingerprint(
    definingClass = HORIZON_CLIENT,
    name = "send",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/Object;",
        "Ljava/lang/String;",
        "Ljava/util/Map;",
        "Ljava/lang/String;",
        "L",
    ),
)

/**
 * Hook map 1.2 — Horizon client batch send.
 */
object HorizonSendBatchFingerprint : Fingerprint(
    definingClass = HORIZON_CLIENT,
    name = "send",
    returnType = "V",
    parameters = listOf(
        "Ljava/util/List;",
        "Ljava/lang/String;",
        "L",
    ),
)

/**
 * Hook map 1.3 — Firebase Analytics wrapper event sink.
 */
object GaLogEventFingerprint : Fingerprint(
    definingClass = GA_MANAGER,
    name = "logEvent\$googleanalytics_productionRelease",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;"),
)

/**
 * Hook map 1.4 — Firebase Analytics wrapper keyed-event registration.
 */
object GaRegisterKeysEventFingerprint : Fingerprint(
    definingClass = GA_MANAGER,
    name = "registerGoogleAnalyticsEvent",
    returnType = "V",
    parameters = listOf(
        "Lcom/globo/globotv/googleanalytics/Keys;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Lcom/globo/globotv/googleanalytics/ActionType;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Lcom/globo/globotv/googleanalytics/Content;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/Integer;",
        "Ljava/lang/Integer;",
        "Z",
        "Ljava/util/List;",
        "Ljava/lang/String;",
        "Ljava/lang/Long;",
        "Ljava/lang/String;",
    ),
)

/**
 * Hook map 1.5 — Firebase Analytics wrapper user-property setter.
 */
object GaAddUserPropertyFingerprint : Fingerprint(
    definingClass = GA_MANAGER,
    name = "addUserProperty",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
)

/**
 * Hook map 1.6 — Firebase Analytics wrapper event-property setter.
 */
object GaAddEventPropertyFingerprint : Fingerprint(
    definingClass = GA_MANAGER,
    name = "addEventProperty",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

/**
 * Hook map 1.8 — AppsFlyer SDK start inside the configure coroutine.
 */
object AppsFlyerConfigureFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/appsflyer/AppsFlyerManager\$Companion\$configure\$1;",
    name = "invokeSuspend",
    filters = listOf(
        methodCall(
            smali = "Lcom/appsflyer/AppsFlyerLib;->start(Landroid/content/Context;Ljava/lang/String;)V",
        ),
    ),
)

/**
 * Hook map 1.10 — GloboID TrackID tracking service.
 */
object TrackIdTrackFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globoid/connect/analytics/tracking/TrackingServiceV2Impl;",
    name = "track",
    returnType = "V",
    parameters = listOf("Lcom/globo/globoid/connect/analytics/tracking/model/TrackingEvent;"),
)

/**
 * Hook map 1.9 — clappr plugin loader gate.
 */
object PlayerPluginRegisterFingerprint : Fingerprint(
    definingClass = "Lio/clappr/player/plugin/Loader;",
    name = "register",
    returnType = "Z",
    parameters = listOf("Lio/clappr/player/plugin/PluginEntry;"),
)

/**
 * Hook map 2.1 — R8 lambda that force-enables Firebase Crashlytics in
 * AndroidTvApplication.onCreate (obfuscated class u20).
 */
object CrashlyticsForceEnableFingerprint : Fingerprint(
    name = "invoke",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
    strings = listOf(
        "firebase_crashlytics_collection_enabled",
        "https://cwapi.video.globo.com",
    ),
)

/**
 * Hook map 1.15 — Globo AB SDK metrics sender (`SendMetricsKt.sendMetrics`,
 * POSTs to sdk-metrics.g.globo/api/v1/performance over a Ktor client on the
 * Android engine, which the OkHttp DNS hook 1.13 does not cover).
 *
 * The class and method names are obfuscated (`b`), so the fingerprint matches
 * the suspend-function signature: the intact `ChooseType` parameter type makes
 * the candidate set exactly the two sendMetrics file-facade methods, and the
 * constructor call of the intact continuation class `SendMetricsKt$sendMetrics$1`
 * is present only in `sendMetrics` itself, not in `mapHttpResponseToMetrics`.
 */
object AbSdkMetricsSenderFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "L",
        "J",
        "Lcom/globo/ab/client/sdk/util/model/metrics/ChooseType;",
        "L",
    ),
    filters = listOf(
        methodCall(
            smali = "Lcom/globo/ab/client/sdk/util/model/metrics/SendMetricsKt\$sendMetrics\$1;->" +
                "<init>(Lkotlin/coroutines/Continuation;)V",
        ),
    ),
)

/**
 * Hook map 1.16 — AB SDK Ktor client logging configuration lambda. It installs
 * a System.out logger and sets `LogLevel.ALL`, printing every request URL to
 * logcat. The class name is intact (Kotlin inner-class naming, not R8).
 * Within the class only one `invoke` returns `V` (the other is the bridge).
 */
object AbSdkKtorLoggingConfigFingerprint : Fingerprint(
    definingClass = "Lcom/globo/ab/client/sdk/http/KtorHttpClientKt\$httpClient\$1\$2;",
    name = "invoke",
    returnType = "V",
    parameters = listOf("L"),
)

/**
 * Hook map 2.3 — Firebase Performance remote-config fetch trigger. Every Perf
 * config read calls this private method, which re-fetches
 * firebaseremoteconfig.googleapis.com whenever the last fetch failed (the
 * failure handler resets the timestamp to 0, so failures loop forever).
 * Implemented in the "Disable telemetry" patch. Class and method names are
 * intact Firebase SDK names.
 */
object FirebasePerfRcFetchTriggerFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/perf/config/RemoteConfigManager;",
    name = "triggerRemoteConfigFetchIfNecessary",
    returnType = "V",
    parameters = emptyList(),
)

/**
 * Hook map 1.17 — app A/B manager experiment sorting; its own empty-list path
 * is already a plain return-void.
 */
object AbManagerSortingAllFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/ab/AbManager;",
    name = "sortingAll",
    returnType = "V",
    parameters = listOf(
        "Z",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    ),
)

/**
 * Hook map 1.18 — app A/B manager impression sender (10-parameter leaf; the
 * 9-parameter overload delegates to it and is not hooked).
 */
object AbManagerSendImpressionFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/ab/AbManager;",
    name = "sendImpression",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/util/Map;",
    ),
)

/**
 * Hook map 1.19 — app A/B manager conversion sender (String leaf; the
 * Experiment overload delegates to it and is not hooked).
 */
object AbManagerSendConversionFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/ab/AbManager;",
    name = "sendConversion",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/util/Map;",
    ),
)
