package brasil.morphe.patches.globoplay.clone

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.shared.logResolved
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.w3c.dom.Element
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("CloneGloboplayPatch")

private const val ORIGINAL_PACKAGE = "com.globo.globotv"
private const val CLONE_PACKAGE = "com.globo.globotv.mod"
private const val ORIGINAL_PACKAGE_PREFIX = "$ORIGINAL_PACKAGE."
private const val CLONE_PACKAGE_PREFIX = "$CLONE_PACKAGE."
private const val CLONE_LABEL = "Globoplay Mod"
private const val LABEL_STRING_NAME = "globoplay_application_name"
private const val SEARCH_AUTHORITY_STRING_NAME = "globoplay_global_search_authority"

/**
 * Hook map (clone) 1a — `AndroidTvApplication.onCreate` passes the hardcoded
 * package name to `zb2.c`, which compares it with `Application.getProcessName()`
 * and runs the whole SDK initialisation only when they are equal. In the clone
 * the main process is `com.globo.globotv.mod`, so the literal must follow.
 */
object MainProcessCheckFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/AndroidTvApplication;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
    strings = listOf(ORIGINAL_PACKAGE),
)

/**
 * Hook map (clone) 1c — `FireTvCapability.<clinit>` loads the package name once
 * and reuses it as the value of the Amazon Fire TV SIGNIN_INTENT_PACKAGE and
 * PLAY_INTENT_PACKAGE capability extras. These are intent targets, so they must
 * point at the clone.
 */
object FireTvCapabilityPackageFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/firetv/catalog/FireTvCapability;",
    name = "<clinit>",
    returnType = "V",
    parameters = listOf(),
    strings = listOf(ORIGINAL_PACKAGE),
)

/**
 * Replaces the exact-literal `const-string` of [ORIGINAL_PACKAGE] with
 * [CLONE_PACKAGE] inside the method, preserving the register and the
 * const-string/const-string-jumbo opcode. Exactly one occurrence is expected.
 */
private fun rewritePackageLiteral(fingerprint: Fingerprint, method: MutableMethod) {
    val instructions = method.implementation!!.instructions

    val indices = instructions.mapIndexedNotNull { index, instruction ->
        if ((instruction.opcode == Opcode.CONST_STRING || instruction.opcode == Opcode.CONST_STRING_JUMBO) &&
            ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string == ORIGINAL_PACKAGE
        ) {
            index
        } else {
            null
        }
    }
    if (indices.size != 1) {
        throw PatchException(
            "Expected exactly one \"$ORIGINAL_PACKAGE\" const-string in " +
                "${method.definingClass}->${method.name}, found ${indices.size}",
        )
    }

    val index = indices.first()
    val register = (instructions[index] as OneRegisterInstruction).registerA
    val opcode = if (instructions[index].opcode == Opcode.CONST_STRING_JUMBO) {
        "const-string/jumbo"
    } else {
        "const-string"
    }
    method.replaceInstruction(index, "$opcode v$register, \"$CLONE_PACKAGE\"")
    fingerprint.logResolved(logger, method)
}

/**
 * Renames the package in the manifest, rewrites app-defined permissions,
 * provider authorities and the launcher label.
 */
private val cloneGloboplayResourcePatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement

            val originalPackage = manifest.getAttribute("package")
            if (originalPackage != ORIGINAL_PACKAGE) {
                throw PatchException(
                    "Expected manifest package \"$ORIGINAL_PACKAGE\", found \"$originalPackage\"",
                )
            }
            manifest.setAttribute("package", CLONE_PACKAGE)
            logger.info("Renamed manifest package to $CLONE_PACKAGE")

            // App-defined permissions: rename declarations and matching
            // uses-permission entries (com.globo.globotv.ACCESS_VIDEO_DATA,
            // .ACCESS_MOVIES_DATA, .DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION).
            var renamedPermissions = 0
            listOf("permission", "uses-permission").forEach { tag ->
                val nodes = manifest.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val element = nodes.item(i) as Element
                    val name = element.getAttribute("android:name")
                    if (name.startsWith(ORIGINAL_PACKAGE_PREFIX)) {
                        element.setAttribute(
                            "android:name",
                            name.replaceFirst(ORIGINAL_PACKAGE_PREFIX, CLONE_PACKAGE_PREFIX),
                        )
                        renamedPermissions++
                        logger.info("Renamed <$tag android:name=\"$name\"> to .mod form")
                    }
                }
            }
            if (renamedPermissions == 0) {
                logger.warning("No app-defined permissions found to rename")
            }

            // Rename component-level android:permission attributes that reference
            // an app-defined permission (none in 2.358.0, kept for robustness).
            var renamedPermissionRefs = 0
            val all = document.getElementsByTagName("*")
            for (i in 0 until all.length) {
                val element = all.item(i) as Element
                val permission = element.getAttribute("android:permission")
                if (permission.startsWith(ORIGINAL_PACKAGE_PREFIX)) {
                    element.setAttribute(
                        "android:permission",
                        permission.replaceFirst(ORIGINAL_PACKAGE_PREFIX, CLONE_PACKAGE_PREFIX),
                    )
                    renamedPermissionRefs++
                    logger.info("Renamed android:permission reference $permission")
                }
                // path-permission read/write permissions as well.
                listOf("android:readPermission", "android:writePermission").forEach { attr ->
                    val value = element.getAttribute(attr)
                    if (value.startsWith(ORIGINAL_PACKAGE_PREFIX)) {
                        element.setAttribute(
                            attr,
                            value.replaceFirst(ORIGINAL_PACKAGE_PREFIX, CLONE_PACKAGE_PREFIX),
                        )
                        renamedPermissionRefs++
                        logger.info("Renamed $attr reference $value")
                    }
                }
            }
            if (renamedPermissionRefs > 0) {
                logger.info("Renamed $renamedPermissionRefs component permission references")
            }

            // Provider authorities are device-wide unique keys: every one of them
            // must follow the rename or installation alongside the stock app fails
            // with INSTALL_FAILED_CONFLICTING_PROVIDER.
            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("<application> not found in AndroidManifest.xml")
            val providers = application.getElementsByTagName("provider")
            var renamedProviders = 0
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as Element
                val authorities = provider.getAttribute("android:authorities")
                if (authorities.isEmpty()) continue
                val newAuthorities = authorities.split(';').joinToString(";") { authority ->
                    if (authority.startsWith(ORIGINAL_PACKAGE_PREFIX)) {
                        authority.replaceFirst(ORIGINAL_PACKAGE_PREFIX, CLONE_PACKAGE_PREFIX)
                    } else {
                        authority
                    }
                }
                if (newAuthorities != authorities) {
                    provider.setAttribute("android:authorities", newAuthorities)
                    renamedProviders++
                    logger.info("Renamed provider authorities \"$authorities\" to \"$newAuthorities\"")
                }
            }
            if (renamedProviders == 0) {
                logger.warning("No provider authorities found to rename")
            }
        }

        // Launcher label: globoplay_application_name is referenced only by the
        // manifest application label (verified: no smali or other resource
        // references), so rewriting the string is safe. In 2.358.0 no translated
        // values-*/strings.xml overrides it.
        document("res/values/strings.xml").use { document ->
            val strings = document.getElementsByTagName("string")
            var rewritten = false
            for (i in 0 until strings.length) {
                val element = strings.item(i) as Element
                when (element.getAttribute("name")) {
                    LABEL_STRING_NAME -> {
                        element.textContent = CLONE_LABEL
                        rewritten = true
                        logger.info("Rewrote @$LABEL_STRING_NAME to \"$CLONE_LABEL\"")
                    }

                    SEARCH_AUTHORITY_STRING_NAME -> {
                        // Holds com.globo.globotv.search.googleplaytv, referenced by
                        // res/xml/searchable.xml; must follow the provider rename.
                        val value = element.textContent
                        if (value.startsWith(ORIGINAL_PACKAGE_PREFIX)) {
                            element.textContent =
                                value.replaceFirst(ORIGINAL_PACKAGE_PREFIX, CLONE_PACKAGE_PREFIX)
                            logger.info("Rewrote @$SEARCH_AUTHORITY_STRING_NAME to \"${element.textContent}\"")
                        } else {
                            logger.warning(
                                "@$SEARCH_AUTHORITY_STRING_NAME does not start with" +
                                    " $ORIGINAL_PACKAGE_PREFIX: \"$value\"",
                            )
                        }
                    }
                }
            }
            if (!rewritten) {
                throw PatchException("@$LABEL_STRING_NAME not found in res/values/strings.xml")
            }
        }
    }
}

@Suppress("unused")
val cloneGloboplayPatch = bytecodePatch(
    name = "Clone Globoplay",
    description = "Renames the package to com.globo.globotv.mod and the launcher label to " +
        "\"Globoplay Mod\" so the patched app installs alongside the stock app with its own " +
        "data and login. Expected limits: Google sign-in, Play Billing, Firebase (including " +
        "Remote Config) and Play Integrity are keyed to the original package name and " +
        "signature and may not work in the clone.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    dependsOn(cloneGloboplayResourcePatch)

    execute {
        // 1a. Main-process gate in AndroidTvApplication.onCreate: without this the
        // whole SDK initialisation is skipped in the clone.
        rewritePackageLiteral(MainProcessCheckFingerprint, MainProcessCheckFingerprint.method)

        // 1c. Amazon Fire TV sign-in/play intent target package.
        rewritePackageLiteral(FireTvCapabilityPackageFingerprint, FireTvCapabilityPackageFingerprint.method)

        // Deliberately NOT rewritten (see the clone hook-map section):
        // - AuthenticationManagerTv$a: the identical literal is the GloboID appId,
        //   a client identity registered with Globo's own servers (X-APP-ID header,
        //   app_id query parameter, OIDC redirect scheme derivation). Renaming it
        //   would break login; keeping it is the stock behaviour of the same client
        //   and is not spoofing a Google service.
        // - The AppAuth redirect scheme globoid-sdk-connect.com.globo.globotv.globoplay
        //   and the deep-link intent filters: server-registered / user-directed.
        // - Play Store links in the forced-update flow: the clone is not on the
        //   Play Store, the original listing is the only valid target.
        // - No Context.getPackageName() spoofing: nothing in the app needs it, and
        //   using it to impersonate the original package toward Google services is
        //   out of scope.
    }
}
