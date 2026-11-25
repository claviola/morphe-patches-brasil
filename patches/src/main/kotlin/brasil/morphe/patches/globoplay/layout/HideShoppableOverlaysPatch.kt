package brasil.morphe.patches.globoplay.layout

import app.morphe.patcher.patch.bytecodePatch
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.globoplay.shared.IsShoppableEnabledFingerprint
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("HideShoppableOverlaysPatch")

@Suppress("unused")
val hideShoppableOverlaysPatch = bytecodePatch(
    name = "Hide shoppable overlays",
    description = "Disables shoppable product overlays via the remote-config flag.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    execute {
        // 6.1 Shoppable remote-config flag.
        IsShoppableEnabledFingerprint.method.also {
            it.returnEarlyFalse()
            IsShoppableEnabledFingerprint.logResolved(logger, it)
        }
    }
}
