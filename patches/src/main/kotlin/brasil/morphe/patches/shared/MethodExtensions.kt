package brasil.morphe.patches.shared

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import java.util.logging.Logger

/**
 * Appends a `return-void` at index 0, stubbing a `V` method.
 */
fun MutableMethod.returnEarlyVoid() {
    addInstruction(0, "return-void")
}

/**
 * Appends `const/4 v0, 0x0` + `return v0` at index 0, stubbing a `Z` method.
 * Requires at least one free register.
 */
fun MutableMethod.returnEarlyFalse() {
    requireLocalRegister("returnEarlyFalse")
    addInstructions(
        0,
        """
            const/4 v0, 0x0
            return v0
        """,
    )
}

/**
 * Appends `const/4 v0, 0x0` + `return-object v0` at index 0, stubbing an
 * object-returning method. Requires at least one free register.
 */
fun MutableMethod.returnEarlyNull() {
    requireLocalRegister("returnEarlyNull")
    addInstructions(
        0,
        """
            const/4 v0, 0x0
            return-object v0
        """,
    )
}

private fun MutableMethod.requireLocalRegister(helper: String) {
    if (implementation!!.registerCount < 1) {
        throw PatchException("$helper requires at least 1 register in $definingClass->$name")
    }
}

private fun Fingerprint.displayName() = this::class.java.simpleName.ifEmpty { toString() }

internal fun methodSignature(method: MutableMethod): String =
    "${method.definingClass}->${method.name}(" +
        method.parameterTypes.joinToString("") + ")${method.returnType}"

/**
 * Logs the standard "resolved" INFO line for a primary hook that was patched
 * via [Fingerprint.method].
 */
fun Fingerprint.logResolved(logger: Logger, method: MutableMethod) {
    logger.info("${displayName()} resolved: ${methodSignature(method)}")
}

/**
 * Hooks a secondary (best-effort) target: applies [block] to the method if the
 * fingerprint matched, otherwise logs a WARNING and continues.
 */
fun Fingerprint.hookOrLog(logger: Logger, block: MutableMethod.() -> Unit) {
    val method = methodOrNull
    if (method != null) {
        block(method)
        logger.info("${displayName()} resolved: ${methodSignature(method)}")
    } else {
        logger.warning("${displayName()} not found, hook skipped")
    }
}
