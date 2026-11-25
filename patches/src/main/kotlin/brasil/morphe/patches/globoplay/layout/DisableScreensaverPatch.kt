package brasil.morphe.patches.globoplay.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.globoplay.shared.IsScreenSaverEnabledFingerprint
import brasil.morphe.patches.shared.hookOrLog
import brasil.morphe.patches.shared.logResolved
import brasil.morphe.patches.shared.returnEarlyFalse
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("DisableScreensaverPatch")

/**
 * Hook map 9.2 — screensaver worker; stubbed to return a success Result
 * immediately so it never schedules screensaver content.
 */
object ScreenSaverRequestWorkerFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/screensaver/ScreenSaverRequestWorker;",
    name = "doWork",
)

/**
 * Finds the `new-instance` of the success Result returned by the method —
 * identified by an invoke-direct `<init>()V` on the same register followed by
 * a `return-object` of that register — and prepends construct+return of it.
 */
private fun MutableMethod.returnSuccessResultEarly() {
    if (implementation!!.registerCount < 1) {
        throw PatchException("returnSuccessResultEarly requires at least 1 register in $definingClass->$name")
    }
    val instructions = implementation!!.instructions
    val resultType = instructions.indices.firstNotNullOfOrNull { i ->
        val newInstance = instructions[i]
        if (newInstance.opcode != Opcode.NEW_INSTANCE) return@firstNotNullOfOrNull null
        val register = (newInstance as? OneRegisterInstruction)?.registerA ?: return@firstNotNullOfOrNull null
        val init = instructions.getOrNull(i + 1)
        val ret = instructions.getOrNull(i + 2)
        val initRef = (init as? ReferenceInstruction)?.reference as? MethodReference
        if (init?.opcode == Opcode.INVOKE_DIRECT &&
            initRef?.name == "<init>" &&
            initRef.returnType == "V" &&
            (init as? FiveRegisterInstruction)?.let { it.registerCount == 1 && it.registerC == register } == true &&
            ret?.opcode == Opcode.RETURN_OBJECT &&
            (ret as? OneRegisterInstruction)?.registerA == register
        ) {
            ((newInstance as? ReferenceInstruction)?.reference as? TypeReference)?.type
        } else {
            null
        }
    } ?: throw PatchException("Returned Result constructor not found in $definingClass->$name")

    addInstructions(
        0,
        """
            new-instance v0, $resultType
            invoke-direct {v0}, $resultType-><init>()V
            return-object v0
        """,
    )
}

@Suppress("unused")
val disableScreensaverPatch = bytecodePatch(
    name = "Disable screensaver",
    description = "Disables the in-app screensaver and its background content worker.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    execute {
        // 9.1 Screensaver remote-config flag.
        IsScreenSaverEnabledFingerprint.method.also {
            it.returnEarlyFalse()
            IsScreenSaverEnabledFingerprint.logResolved(logger, it)
        }

        // 9.2 Screensaver content worker.
        ScreenSaverRequestWorkerFingerprint.hookOrLog(logger) { returnSuccessResultEarly() }
    }
}
