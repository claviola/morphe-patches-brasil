package brasil.morphe.extension.globoplay.telemetry;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Decides whether a clappr player plugin should be blocked from registering.
 * Never throws; unknown or null names are always allowed.
 */
public final class PlayerPluginFilter {
    private static final String TAG = "GloboplayTelemetry";

    private static final Set<String> BLOCKED = new HashSet<>(Arrays.asList(
            "horizonPlugin",
            "horizonEnvironmentPlugin",
            "horizonerrorplugin",
            "horizoncoreplugin",
            "horizonplaybackplugin",
            "HorizonMediaControlEventsPlugin",
            "horizonplaybackmetadataplugin",
            "horizonadsinfoplugin",
            "horizoncontainerplugin",
            "HorizonDFPPlugin",
            "horizonframeadeventsplugin",
            "horizonframeaderrorplugin",
            "horizonMosaicEventsPlugin",
            "horizonDeviceCapabilitiesPlugin",
            "youboraPlugin",
            "GAPlugin",
            "hitsPlugin"
    ));

    private static final Set<String> loggedOnce = Collections.synchronizedSet(new HashSet<String>());

    private PlayerPluginFilter() {
    }

    public static boolean shouldBlock(String name) {
        try {
            if (name != null && BLOCKED.contains(name)) {
                if (loggedOnce.add(name)) {
                    Log.i(TAG, "Blocked player plugin: " + name);
                }
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
