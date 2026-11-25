package brasil.morphe.patches.globoplay.layout

import app.morphe.patcher.patch.bytecodePatch
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.globoplay.shared.IsPanelPollEnabledFingerprint
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("HidePollsPatch")

@Suppress("unused")
val hidePollsPatch = bytecodePatch(
    name = "Hide polls",
    description = "Disables interactive poll panels via the remote-config flag.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    execute {
        // 7.1 Poll panel remote-config flag.
        IsPanelPollEnabledFingerprint.method.also {
            it.returnEarlyFalse()
            IsPanelPollEnabledFingerprint.logResolved(logger, it)
        }
    }
}
