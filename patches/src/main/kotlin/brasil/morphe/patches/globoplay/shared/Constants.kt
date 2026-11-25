package brasil.morphe.patches.globoplay.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    /**
     * Globoplay for Android TV (leanback build of com.globo.globotv).
     *
     * APKMirror distributes it as an APKM bundle, so the file type must stay APKM
     * or Morphe Manager will not send users to the right download.
     */
    val COMPATIBILITY_GLOBOPLAY_TV = Compatibility(
        name = "Globoplay (Android TV)",
        packageName = "com.globo.globotv",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xF40034,
        targets = listOf(
            // Version the patches were developed and verified against.
            AppTarget(version = "2.358.0"),
        ),
    )
}
