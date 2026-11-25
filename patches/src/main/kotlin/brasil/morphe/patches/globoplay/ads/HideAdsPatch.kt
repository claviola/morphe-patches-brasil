package brasil.morphe.patches.globoplay.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.smali.ExternalLabel
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.globoplay.shared.GetEnableBingeAdsFingerprint
import brasil.morphe.patches.globoplay.shared.GetEnableFrameAdsFingerprint
import brasil.morphe.patches.globoplay.shared.GetPauseAdEnabledFingerprint
import brasil.morphe.patches.globoplay.shared.GetPauseAdsTakeoverEnabledFingerprint
import brasil.morphe.patches.globoplay.shared.GetPauseAdsTakeoverLiveEnabledFingerprint
import brasil.morphe.patches.globoplay.shared.OkHttpDnsLookupFingerprint
import brasil.morphe.patches.shared.hookOrLog
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import brasil.morphe.patches.shared.returnEarlyNull
import org.w3c.dom.Element
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("HideAdsPatch")

/**
 * Removes the Google Mobile Ads init provider so no ads SDK boots.
 */
// 3.4
private val removeMobileAdsInitProviderPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as? Element
            if (application == null) {
                logger.warning("<application> not found in AndroidManifest.xml")
                return@use
            }
            val providers = application.getElementsByTagName("provider")
            var removed = false
            for (i in providers.length - 1 downTo 0) {
                val provider = providers.item(i) as Element
                if (provider.getAttribute("android:name") == "com.google.android.gms.ads.MobileAdsInitProvider") {
                    application.removeChild(provider)
                    removed = true
                    logger.info("Removed MobileAdsInitProvider from manifest")
                }
            }
            if (!removed) {
                logger.warning("MobileAdsInitProvider not found in manifest")
            }
        }
    }
}

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Disables preroll/midroll, pause, takeover, frame and binge ads, " +
        "removes the Google Mobile Ads init provider " +
        "and blocks the ad-configuration API (multicloud-ads-api.globo.com).",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    dependsOn(removeMobileAdsInitProviderPatch)

    execute {
        // 3.1 "Ads available" check and 3.2 ad-tag URL builder (primary).
        AdManagerAdsAvailableFingerprint.method.also {
            it.returnEarlyFalse()
            AdManagerAdsAvailableFingerprint.logResolved(logger, it)
        }
        AdTagUrlBuilderFingerprint.method.also {
            it.returnEarlyNull()
            AdTagUrlBuilderFingerprint.logResolved(logger, it)
        }

        // 3.3 ExoPlayer playback ad-tag URL.
        ExoPlayerAdTagUrlFingerprint.hookOrLog(logger) { returnEarlyNull() }

        // 3.5 Remote-config ad flags.
        GetPauseAdEnabledFingerprint.hookOrLog(logger) { returnEarlyFalse() }
        GetPauseAdsTakeoverEnabledFingerprint.hookOrLog(logger) { returnEarlyFalse() }
        GetPauseAdsTakeoverLiveEnabledFingerprint.hookOrLog(logger) { returnEarlyFalse() }
        GetEnableFrameAdsFingerprint.hookOrLog(logger) { returnEarlyFalse() }
        GetEnableBingeAdsFingerprint.hookOrLog(logger) { returnEarlyFalse() }

        // 3.9 Ad-configuration API host (multicloud-ads-api.globo.com).
        OkHttpDnsLookupFingerprint.hookOrLog(logger) {
            addInstructionsWithLabels(
                0,
                """
                    const-string v0, "multicloud-ads-api.globo.com"
                    invoke-virtual {v0, p1}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :lookup
                    new-instance v0, Ljava/net/UnknownHostException;
                    invoke-direct {v0, p1}, Ljava/net/UnknownHostException;-><init>(Ljava/lang/String;)V
                    throw v0
                """,
                ExternalLabel("lookup", getInstruction(0)),
            )
        }
    }
}
