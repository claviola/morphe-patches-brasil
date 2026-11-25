package brasil.morphe.patches.globoplay.telemetry

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.globoplay.shared.IsAppAnalyticsEnabledFingerprint
import brasil.morphe.patches.globoplay.shared.OkHttpDnsLookupFingerprint
import brasil.morphe.patches.shared.hookOrLog
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import brasil.morphe.patches.shared.returnEarlyVoid
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import java.util.logging.Logger

private const val EXTENSION_PACKAGE = "Lbrasil/morphe/extension/globoplay/telemetry"

private const val KTOR_LOG_LEVEL_ALL =
    "Lio/ktor/client/plugins/logging/LogLevel;->ALL:Lio/ktor/client/plugins/logging/LogLevel;"
private const val KTOR_LOG_LEVEL_NONE =
    "Lio/ktor/client/plugins/logging/LogLevel;->NONE:Lio/ktor/client/plugins/logging/LogLevel;"

private val logger: Logger = Logger.getLogger("DisableTelemetryPatch")

/**
 * In the original method, find the first SGET_OBJECT whose field reference
 * type is `Lkotlin/Unit;` (the Unit singleton this coroutine returns) and
 * prepend a load + return of it. Throws if the anchor is missing.
 */
private fun MutableMethod.returnUnitEarly() {
    if (implementation!!.registerCount < 1) {
        throw PatchException("returnUnitEarly requires at least 1 register in $definingClass->$name")
    }
    val unitGet = implementation!!.instructions.firstOrNull { instruction ->
        instruction.opcode == Opcode.SGET_OBJECT &&
            ((instruction as? ReferenceInstruction)?.reference as? FieldReference)
                ?.type == "Lkotlin/Unit;"
    } as? ReferenceInstruction
        ?: throw PatchException("Unit sget-object anchor not found in $definingClass->$name")

    val unitField = unitGet.reference.toString()
    addInstructions(
        0,
        """
            sget-object v0, $unitField
            return-object v0
        """,
    )
}

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables Horizon, Firebase Analytics wrapper, NPAW, AppsFlyer, " +
        "GloboID TrackID and player telemetry plugins, blocks telemetry hosts in OkHttp, " +
        "and stops the app's A/B-test requests that carry account identifiers.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    extendWith("extensions/extension.mpe")

    val blockPlayerPlugins by booleanOption(
        key = "blockPlayerPlugins",
        default = true,
        title = "Block player telemetry plugins",
        description = "Prevent the player from registering the Horizon, Youbora, GA and hits " +
            "telemetry plugins. The traffic is already silenced by the other hooks; " +
            "turn this off if playback misbehaves.",
    )

    execute {
        // 1.1 Horizon single-event send (primary).
        HorizonSendEventFingerprint.method.also {
            it.returnEarlyVoid()
            HorizonSendEventFingerprint.logResolved(logger, it)
        }

        // 1.2 Horizon batch send.
        HorizonSendBatchFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.3 Firebase Analytics wrapper event sink.
        GaLogEventFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.4 Firebase Analytics wrapper keyed-event registration.
        GaRegisterKeysEventFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.5 Firebase Analytics wrapper user-property setter.
        GaAddUserPropertyFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.6 Firebase Analytics wrapper event-property setter.
        GaAddEventPropertyFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.8 AppsFlyer SDK start coroutine.
        AppsFlyerConfigureFingerprint.hookOrLog(logger) { returnUnitEarly() }

        // 1.7 Remote-config gate that enables NPAW app analytics.
        IsAppAnalyticsEnabledFingerprint.hookOrLog(logger) { returnEarlyFalse() }

        // 1.10 GloboID TrackID tracking service.
        TrackIdTrackFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.9 Clappr plugin loader: refuse to register telemetry plugins.
        if (blockPlayerPlugins != false) {
            PlayerPluginRegisterFingerprint.hookOrLog(logger) {
                addInstructionsWithLabels(
                    0,
                    """
                        invoke-virtual {p0}, Lio/clappr/player/plugin/PluginEntry;->getName()Ljava/lang/String;
                        move-result-object v0
                        invoke-static {v0}, $EXTENSION_PACKAGE/PlayerPluginFilter;->shouldBlock(Ljava/lang/String;)Z
                        move-result v0
                        if-eqz v0, :register
                        const/4 v0, 0x0
                        return v0
                    """,
                    ExternalLabel("register", getInstruction(0)),
                )
            }
        } else {
            logger.info(
                "PlayerPluginRegisterFingerprint hook skipped by option: blockPlayerPlugins=false",
            )
        }

        // 1.13 OkHttp Dns blocklist.
        OkHttpDnsLookupFingerprint.hookOrLog(logger) {
            addInstructions(
                0,
                """
                    invoke-static {p1}, $EXTENSION_PACKAGE/HostBlocklist;->checkHost(Ljava/lang/String;)V
                """,
            )
        }

        // 1.15 Globo AB SDK metrics sender (sdk-metrics.g.globo). The Ktor client
        // uses the Android engine, so the OkHttp DNS hook does not cover it.
        // Returning Unit at once is the suspend function's already-completed result;
        // the only caller (Api.choose) discards the value.
        AbSdkMetricsSenderFingerprint.hookOrLog(logger) { returnUnitEarly() }

        // 1.16 AB SDK Ktor client logging: demote LogLevel.ALL to LogLevel.NONE so
        // the installed System.out logger is never invoked. Same register, nothing
        // else in the method changes.
        AbSdkKtorLoggingConfigFingerprint.hookOrLog(logger) {
            val instructions = implementation!!.instructions
            val allIndex = instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.SGET_OBJECT &&
                    (instruction as? ReferenceInstruction)?.reference?.toString() == KTOR_LOG_LEVEL_ALL
            }
            if (allIndex < 0) {
                throw PatchException("LogLevel.ALL sget-object not found in $definingClass->$name")
            }
            val register = (instructions[allIndex] as OneRegisterInstruction).registerA
            replaceInstruction(allIndex, "sget-object v$register, $KTOR_LOG_LEVEL_NONE")
        }

        // 2.3 Firebase Performance remote-config fetch trigger. Without this stub every
        // Perf config read re-fetches firebaseremoteconfig.googleapis.com after any
        // fetch failure (the failure handler resets the last-fetch timestamp to 0).
        // Perf then runs on its hardcoded defaults, which is the designed behaviour
        // for apps without Firebase Remote Config.
        FirebasePerfRcFetchTriggerFingerprint.hookOrLog(logger) { returnEarlyVoid() }

        // 1.17–1.19 App A/B manager (ab.g.globo with GloboID/anonymous id and
        // Horizon session id). The player's own /choose is not touched.
        AbManagerSortingAllFingerprint.hookOrLog(logger) { returnEarlyVoid() }
        AbManagerSendImpressionFingerprint.hookOrLog(logger) { returnEarlyVoid() }
        AbManagerSendConversionFingerprint.hookOrLog(logger) { returnEarlyVoid() }
    }
}
