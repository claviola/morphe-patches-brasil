package brasil.morphe.patches.globoplay.telemetry

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.shared.logResolved
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.w3c.dom.Element
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("DisableFirebaseReportingPatch")

private const val CRASHLYTICS_KEY = "firebase_crashlytics_collection_enabled"
private const val BOOLEAN_TRUE_FIELD = "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;"
private const val BOOLEAN_FALSE_FIELD = "Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
private const val PUT_BOOLEAN_METHOD =
    "Landroid/content/SharedPreferences\$Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences\$Editor;"

private val META_DATA_TO_ADD = mapOf(
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_deactivated" to "true",
    "firebase_sessions_enabled" to "false",
)

private val COMPONENTS_TO_REMOVE = setOf(
    "com.google.android.gms.measurement.AppMeasurementJobService",
    "com.google.android.gms.measurement.AppMeasurementService",
    "com.google.android.gms.measurement.AppMeasurementReceiver",
)

private val PERMISSIONS_TO_REMOVE = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
    "android.permission.AD_SERVICES_CONFIG",
)

/**
 * Adds Firebase opt-out meta-data and removes measurement components and
 * ad-id/ad-services permissions from the manifest.
 */
private val disableFirebaseReportingManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("<application> not found in AndroidManifest.xml")

            // Add (or update) the opt-out meta-data entries.
            val existingMetaData = mutableMapOf<String, Element>()
            val metaDataNodes = application.getElementsByTagName("meta-data")
            for (i in 0 until metaDataNodes.length) {
                val element = metaDataNodes.item(i) as Element
                existingMetaData[element.getAttribute("android:name")] = element
            }
            META_DATA_TO_ADD.forEach { (name, value) ->
                val existing = existingMetaData[name]
                if (existing != null) {
                    existing.setAttribute("android:value", value)
                    logger.info("Updated manifest meta-data: $name=$value")
                } else {
                    val metaData = document.createElement("meta-data")
                    metaData.setAttribute("android:name", name)
                    metaData.setAttribute("android:value", value)
                    application.appendChild(metaData)
                    logger.info("Added manifest meta-data: $name=$value")
                }
            }

            // Remove Firebase measurement service/receiver components.
            listOf("service", "receiver").forEach { tag ->
                val nodes = application.getElementsByTagName(tag)
                var removed = false
                for (i in nodes.length - 1 downTo 0) {
                    val element = nodes.item(i) as Element
                    if (element.getAttribute("android:name") in COMPONENTS_TO_REMOVE) {
                        application.removeChild(element)
                        removed = true
                        logger.info("Removed <$tag android:name=\"${element.getAttribute("android:name")}\"/>")
                    }
                }
                if (!removed) {
                    logger.warning("No Firebase measurement <$tag> elements found to remove")
                }
            }

            // Remove ad-id / ad-services permissions.
            val manifest = document.documentElement
            val permissions = manifest.getElementsByTagName("uses-permission")
            var removedCount = 0
            for (i in permissions.length - 1 downTo 0) {
                val element = permissions.item(i) as Element
                if (element.getAttribute("android:name") in PERMISSIONS_TO_REMOVE) {
                    manifest.removeChild(element)
                    removedCount++
                    logger.info("Removed <uses-permission android:name=\"${element.getAttribute("android:name")}\"/>")
                }
            }
            if (removedCount < PERMISSIONS_TO_REMOVE.size) {
                logger.warning(
                    "Removed $removedCount of ${PERMISSIONS_TO_REMOVE.size} ad permissions; some were not present",
                )
            }
        }
    }
}

@Suppress("unused")
val disableFirebaseReportingPatch = bytecodePatch(
    name = "Disable Firebase reporting",
    description = "Stops the app force-enabling Crashlytics collection and disables " +
        "Firebase Analytics, Crashlytics and Performance reporting via manifest flags.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    dependsOn(disableFirebaseReportingManifestPatch)

    execute {
        // 2.1 Lambda that force-enables Crashlytics collection on startup.
        CrashlyticsForceEnableFingerprint.method.also { method ->
            val instructions = method.implementation!!.instructions

            val keyIndex = instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.CONST_STRING &&
                    ((instruction as? ReferenceInstruction)?.reference as? StringReference)
                        ?.string == CRASHLYTICS_KEY
            }
            if (keyIndex < 0) {
                throw PatchException("\"$CRASHLYTICS_KEY\" const-string not found in ${method.definingClass}->${method.name}")
            }

            // Force the crashlytics settings flag to FALSE instead of TRUE.
            val trueIndex = instructions.subList(0, keyIndex).indexOfFirst { instruction ->
                instruction.opcode == Opcode.SGET_OBJECT &&
                    (instruction as? ReferenceInstruction)?.reference?.toString() == BOOLEAN_TRUE_FIELD
            }
            if (trueIndex < 0) {
                throw PatchException("Boolean.TRUE sget-object before \"$CRASHLYTICS_KEY\" not found")
            }
            val trueRegister = (instructions[trueIndex] as OneRegisterInstruction).registerA
            method.replaceInstruction(trueIndex, "sget-object v$trueRegister, $BOOLEAN_FALSE_FIELD")

            // Write false to the preference, then restore the register to true
            // for the later use that requires the original value.
            val putBooleanIndex = instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.INVOKE_INTERFACE &&
                    ((instruction as? ReferenceInstruction)?.reference as? MethodReference)
                        ?.toString() == PUT_BOOLEAN_METHOD
            }
            if (putBooleanIndex < 0 || putBooleanIndex <= keyIndex) {
                throw PatchException("putBoolean invoke after \"$CRASHLYTICS_KEY\" not found")
            }
            val nextOpcode = instructions[putBooleanIndex + 1].opcode
            if (nextOpcode in setOf(Opcode.MOVE_RESULT, Opcode.MOVE_RESULT_WIDE, Opcode.MOVE_RESULT_OBJECT)) {
                throw PatchException("putBoolean is followed by $nextOpcode; edit order assumptions broken")
            }
            val boolRegister = (instructions[putBooleanIndex] as FiveRegisterInstruction).registerE
            val const = if (boolRegister < 16) "const/4" else "const/16"
            method.addInstructions(putBooleanIndex, "$const v$boolRegister, 0x0")
            method.addInstructions(putBooleanIndex + 2, "$const v$boolRegister, 0x1")

            CrashlyticsForceEnableFingerprint.logResolved(logger, method)
        }
    }
}
