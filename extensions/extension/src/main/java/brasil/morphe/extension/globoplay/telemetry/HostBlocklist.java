package brasil.morphe.extension.globoplay.telemetry;

import android.util.Log;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Hostname blocklist consulted by the hooked OkHttp Dns implementation.
 * A host is blocked if it equals an entry or is a subdomain of an entry.
 * Any unexpected error falls through and the host is allowed.
 */
public final class HostBlocklist {
    private static final String TAG = "GloboplayTelemetry";

    private static final Set<String> BLOCKED = new HashSet<>(Arrays.asList(
            "horizon.globo.com",
            "horizon-track.globo.com",
            "horizon-schemas.globo.com",
            "hzp.globo.com",
            "stats.video.globo.com",
            "trackid.globoid.globo.com",
            "browserreport.globo.com",
            "www.google-analytics.com",
            "api.permutive.app",
            "cdn.permutive.app",
            "lma.npaw.com",
            "appsflyersdk.com",
            "youboranqs01.com"
    ));

    private HostBlocklist() {
    }

    public static void checkHost(String host) throws UnknownHostException {
        try {
            if (host == null) {
                return;
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            while (normalized.endsWith(".")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            for (String entry : BLOCKED) {
                if (normalized.equals(entry) || normalized.endsWith("." + entry)) {
                    Log.i(TAG, "Blocked DNS lookup: " + host);
                    throw new UnknownHostException("Blocked by patch: " + host);
                }
            }
        } catch (UnknownHostException blocked) {
            throw blocked;
        } catch (Throwable ignored) {
        }
    }
}
