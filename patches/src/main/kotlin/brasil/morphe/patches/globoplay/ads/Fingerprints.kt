package brasil.morphe.patches.globoplay.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Hook map 3.1 — AdManager "ads available" check. The method name is
 * obfuscated, so it is matched by signature and the booleanValue() call.
 */
object AdManagerAdsAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/ad/AdManager;",
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
)

/**
 * Hook map 3.2 — builds the DoubleClick pubads URL for the player
 * (obfuscated class com/globo/video/player/internal/j.smali).
 */
object AdTagUrlBuilderFingerprint : Fingerprint(
    strings = listOf("https://pubads.g.doubleclick.net/gampad/ads?sz=1280x720&iu="),
    returnType = "Ljava/lang/String;",
    parameters = listOf("L", "Lio/clappr/player/base/Options;"),
)

/**
 * Hook map 3.3 — ad-tag URL getter on the ExoPlayer playback.
 */
object ExoPlayerAdTagUrlFingerprint : Fingerprint(
    definingClass = "Lio/clappr/player/playback/ExoPlayerPlayback;",
    name = "getAdTagUrl",
    returnType = "Ljava/lang/String;",
)

/**
 * Hook map 4.1 — source of the player's DAI flag for each stream resource
 * (copied from the video-session JSON "dai"). Its only caller is the player's
 * resource mapper, so false here makes every DAI consumer take its non-DAI path.
 */
object VideoResourceHasDaiFingerprint : Fingerprint(
    definingClass = "Lcom/globo/video/sdk/model/VideoInfo\$Resource;",
    name = "getHasDai",
    returnType = "Z",
    parameters = listOf(),
)
