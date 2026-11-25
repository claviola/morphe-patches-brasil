package brasil.morphe.patches.globoplay.ads

import app.morphe.patcher.patch.bytecodePatch
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.globoplay.shared.IsWelcomeAdsTvEnabledFingerprint
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("HideWelcomeAdsPatch")

@Suppress("unused")
val hideWelcomeAdsPatch = bytecodePatch(
    name = "Hide welcome ads",
    description = "Disables the welcome/splash ad on Android TV via the remote-config flag.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    execute {
        // 5.1 Welcome-ads remote-config flag.
        IsWelcomeAdsTvEnabledFingerprint.method.also {
            it.returnEarlyFalse()
            IsWelcomeAdsTvEnabledFingerprint.logResolved(logger, it)
        }
    }
}
