package brasil.morphe.patches.globoplay.network

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.filePathOption
import app.morphe.patcher.patch.resourcePatch
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import java.io.File
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("TrustCustomCaPatch")

private const val CONFIG_PATH = "res/xml/network_security_config.xml"
private const val RAW_RESOURCE_NAME = "morphe_custom_ca"
private const val RAW_FILE_NAME = "$RAW_RESOURCE_NAME.pem"

/**
 * Debug/testing patch. Weakens TLS verification of the app's own traffic so a tester
 * can decrypt it with a MITM proxy (e.g. mitmproxy) on their own device.
 * Not meant for normal use.
 */
@Suppress("unused")
val trustCustomCaPatch = resourcePatch(
    name = "Trust custom certificate authority",
    description = "Debug/testing patch: trusts an additional certificate authority so the app's own " +
        "HTTPS traffic can be inspected with a proxy such as mitmproxy. Weakens TLS verification; " +
        "do not use for normal viewing. If a CA certificate file is given it is embedded in the APK " +
        "and trusted alongside the system store; otherwise the user CA store is trusted.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    val caCertificate by filePathOption(
        key = "caCertificate",
        title = "CA certificate (PEM)",
        description = "Path to a PEM-encoded CA certificate to embed and trust " +
            "(for example ~/.mitmproxy/mitmproxy-ca-cert.pem). " +
            "If unset, the user CA store is trusted instead.",
        required = false,
    )

    execute {
        val extraAnchor: String

        val caPath = caCertificate
        if (caPath != null) {
            val caFile = File(caPath)
            if (!caFile.isFile) {
                throw PatchException("CA certificate file not found: $caPath")
            }
            val pem = caFile.readText()
            if (!pem.contains("-----BEGIN CERTIFICATE-----")) {
                throw PatchException("CA certificate file does not look like a PEM certificate: $caPath")
            }

            // res/xml is guaranteed to exist (it holds the stock network_security_config.xml),
            // so resolve res/ from it instead of assuming a workspace layout.
            val resDirectory = get("res/xml").parentFile
                ?: throw PatchException("Could not resolve the decoded res/ directory")
            val rawDirectory = File(resDirectory, "raw")
            if (!rawDirectory.isDirectory && !rawDirectory.mkdirs()) {
                throw PatchException("Could not create ${rawDirectory.absolutePath}")
            }
            File(rawDirectory, RAW_FILE_NAME).writeText(pem)
            logger.info("Embedded CA certificate $caPath as res/raw/$RAW_FILE_NAME")

            extraAnchor = "<certificates src=\"@raw/$RAW_RESOURCE_NAME\"/>"
        } else {
            logger.info("No CA certificate option set; trusting the user CA store")
            extraAnchor = "<certificates src=\"user\"/>"
        }

        // The stock config is a single empty <network-security-config/> element,
        // so the file is rewritten wholesale instead of edited as a DOM.
        val configFile = get(CONFIG_PATH)
        if (!configFile.isFile) {
            throw PatchException("$CONFIG_PATH not found in the decoded APK")
        }
        configFile.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <network-security-config>
                <base-config cleartextTrafficPermitted="false">
                    <trust-anchors>
                        <certificates src="system"/>
                        $extraAnchor
                    </trust-anchors>
                </base-config>
            </network-security-config>
            """.trimIndent() + "\n",
        )
        logger.info("Rewrote $CONFIG_PATH with trust anchors: system + $extraAnchor")
    }
}
