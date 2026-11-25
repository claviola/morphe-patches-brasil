package brasil.morphe.patches.globoplay.region

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.Option
import app.morphe.patcher.patch.OptionException
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import brasil.morphe.patches.globoplay.shared.Constants.COMPATIBILITY_GLOBOPLAY_TV
import brasil.morphe.patches.shared.logResolved
import com.android.tools.smali.dexlib2.AccessFlags
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("SetRegionPatch")

/**
 * Hook map 10.1 — repository method that chooses between the coordinate-aware
 * affiliates endpoint and the by-IP fallback.
 */
object SecurityRepositoryAffiliateFingerprint : Fingerprint(
    definingClass = "Lcom/globo/globotv/repository/security/SecurityRepository;",
    name = "affiliate",
    returnType = "Lio/reactivex/rxjava3/core/m;",
    parameters = listOf("Ljava/lang/Double;", "Ljava/lang/Double;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/**
 * Hook map 10.2 — player-plugin method whose result is serialized as lat/long
 * in the playback video-session request.
 */
object IdPluginGetLocationFingerprint : Fingerprint(
    definingClass = "Lcom/globo/video/player/plugin/container/IdPlugin;",
    name = "getLocation",
    returnType = "Lcom/globo/video/sdk/service/params/Location;",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
)

/**
 * Formats the raw IEEE754 bits of a double as a smali long literal. For negative
 * long values this uses the canonical sign-plus-magnitude hexadecimal form emitted
 * by baksmali (for example -0x3fc917dbf487fcb9L), because an unsigned hexadecimal
 * long above 0x7fffffffffffffff is not accepted as a positive decimal/hex literal.
 */
private fun Double.toSmaliWideLiteral(): String {
    val bits = java.lang.Double.doubleToRawLongBits(this)
    return when {
        bits >= 0L -> "0x${java.lang.Long.toHexString(bits)}L"
        bits == Long.MIN_VALUE -> "-0x8000000000000000L"
        else -> "-0x${java.lang.Long.toHexString(-bits)}L"
    }
}

private fun isValidLatitude(value: String?): Boolean =
    value?.toDoubleOrNull()?.let { it in -90.0..90.0 } == true

private fun isValidLongitude(value: String?): Boolean =
    value?.toDoubleOrNull()?.let { it in -180.0..180.0 } == true

private fun requireCoordinate(
    option: Option<String>,
    range: ClosedFloatingPointRange<Double>,
): Double {
    val value = option.value
    val coordinate = value?.toDoubleOrNull()
    if (coordinate == null || coordinate !in range) {
        throw PatchException(
            "Custom coordinates selected but ${option.title} is missing or invalid " +
                "(value: $value). Open the options of the \"Set region\" patch and enter " +
                "the custom latitude and longitude, or choose an affiliate instead of " +
                "\"Custom coordinates\".",
        )
    }
    return coordinate
}

private fun parseRegionCoordinates(value: String, option: Option<String>): Pair<Double, Double> {
    val parts = value.split(',')
    if (parts.size != 2) {
        throw OptionException.ValueValidationException(value, option)
    }

    val latitude = parts[0].toDoubleOrNull()
    val longitude = parts[1].toDoubleOrNull()
    if (latitude == null || latitude !in -90.0..90.0 ||
        longitude == null || longitude !in -180.0..180.0
    ) {
        throw OptionException.ValueValidationException(value, option)
    }

    return latitude to longitude
}

/**
 * SecurityRepository.affiliate has .locals 2 in the target build, so v0/v1 are
 * free at method entry. Box the latitude and longitude as java.lang.Double and
 * overwrite both reference parameters before the original null checks run.
 */
private fun MutableMethod.overwriteAffiliateCoordinateParameters(latitude: Double, longitude: Double) {
    if ((implementation?.registerCount ?: 0) < 5) {
        throw PatchException("SecurityRepository.affiliate does not have the expected v0/v1 free registers")
    }

    addInstructions(
        0,
        """
            const-wide v0, ${latitude.toSmaliWideLiteral()}
            invoke-static {v0, v1}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;
            move-result-object p1
            const-wide v0, ${longitude.toSmaliWideLiteral()}
            invoke-static {v0, v1}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;
            move-result-object p2
        """,
    )
}

/**
 * IdPlugin.getLocation has .locals 6 in the target build, so v0..v5 are free at
 * method entry. Construct and return Location(latitude, longitude) directly.
 * Both doubles are wide values, so the invoke-direct register list must include
 * v2/v3 and v4/v5, not only their low halves.
 */
private fun MutableMethod.returnFixedPlayerLocation(latitude: Double, longitude: Double) {
    if ((implementation?.registerCount ?: 0) < 7) {
        throw PatchException("IdPlugin.getLocation does not have the expected v0..v5 free registers")
    }

    addInstructions(
        0,
        """
            new-instance v0, Lcom/globo/video/sdk/service/params/Location;
            const-wide v2, ${latitude.toSmaliWideLiteral()}
            const-wide v4, ${longitude.toSmaliWideLiteral()}
            invoke-direct {v0, v2, v3, v4, v5}, Lcom/globo/video/sdk/service/params/Location;-><init>(DD)V
            return-object v0
        """,
    )
}

@Suppress("unused")
val setRegionPatch = bytecodePatch(
    name = "Set region",
    description = "Overrides the coordinates used for Globo's regional affiliate lookup and for " +
        "the video-session location. Choose one of the 117 known affiliates or custom coordinates. " +
        "Confirmed on a device: the programme guide and the live regional stream both followed the " +
        "injected coordinates (tested for two affiliates from one exit location).",
    default = false,
) {
    compatibleWith(COMPATIBILITY_GLOBOPLAY_TV)

    val regionOption = stringOption(
        key = "region",
        default = DEFAULT_REGION_VALUE,
        values = REGION_OPTION_VALUES,
        title = "Regional affiliate",
        description = "Regional affiliate whose representative coordinates are injected, or custom coordinates.",
        required = true,
    ) {
        it in REGION_OPTION_VALUES.values
    }

    val customLatitudeOption = stringOption(
        key = "customLatitude",
        title = "Custom latitude (only for custom coordinates)",
        description = "Used only when the regional affiliate option is set to Custom coordinates; " +
            "ignored otherwise. Latitude of the place whose affiliate you want, -90 to 90.",
        required = false,
    ) {
        // Null means "not supplied"; execute() turns that into a validation error only
        // when custom coordinates were selected. Non-null values must always be valid.
        it == null || isValidLatitude(it)
    }

    val customLongitudeOption = stringOption(
        key = "customLongitude",
        title = "Custom longitude (only for custom coordinates)",
        description = "Used only when the regional affiliate option is set to Custom coordinates; " +
            "ignored otherwise. Longitude of the place whose affiliate you want, -180 to 180.",
        required = false,
    ) {
        it == null || isValidLongitude(it)
    }

    execute {
        val regionValue = regionOption.value!!
        val (latitude, longitude) = if (regionValue == CUSTOM_REGION_VALUE) {
            requireCoordinate(customLatitudeOption, -90.0..90.0) to
                requireCoordinate(customLongitudeOption, -180.0..180.0)
        } else {
            parseRegionCoordinates(regionValue, regionOption)
        }

        logger.info("Set region using latitude=$latitude longitude=$longitude")

        // 10.1 Affiliate lookup used by EPG, broadcast, search, offers and other callers.
        SecurityRepositoryAffiliateFingerprint.method.also {
            it.overwriteAffiliateCoordinateParameters(latitude, longitude)
            SecurityRepositoryAffiliateFingerprint.logResolved(logger, it)
        }

        // 10.2 Location sent in the playback video-session request.
        IdPluginGetLocationFingerprint.method.also {
            it.returnFixedPlayerLocation(latitude, longitude)
            IdPluginGetLocationFingerprint.logResolved(logger, it)
        }
    }
}
