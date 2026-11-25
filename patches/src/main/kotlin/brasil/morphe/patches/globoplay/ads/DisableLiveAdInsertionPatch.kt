package brasil.morphe.patches.globoplay.ads

import app.morphe.patcher.patch.bytecodePatch
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("DisableLiveAdInsertionPatch")

@Suppress("unused")
val disableLiveAdInsertionPatch = bytecodePatch(
    name = "Disable live ad insertion",
    description = "Keeps live channels on the plain broadcast stream instead of a Google " +
        "Dynamic Ad Insertion session, so no ads are substituted and no viewer identifiers " +
        "go to Google for it. Commercials that are part of the broadcast itself remain.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    execute {
        // 4.1 Stream DAI flag: report every source as non-DAI.
        VideoResourceHasDaiFingerprint.method.also {
            it.returnEarlyFalse()
            VideoResourceHasDaiFingerprint.logResolved(logger, it)
        }
    }
}
