package mini.app.orbis;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by Jan on 21/01/2017.
 */

public class VersionManager {

    public static String versionName = "1.0.2";
    public static int versionCode = 8;


    public static int[] previouslyInstalledVersions;
    private static boolean initialized;

    private static synchronized void init(Context context) {
        if(!initialized) {
            initialized = true;
            SharedPreferences usageStats = context.getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
            String currentRaw = usageStats.getString(GlobalVars.KEY_PREV_VERSIONS, "");
            previouslyInstalledVersions = decodeCSV(currentRaw);
            if(getLastInstalledVersion(context) != versionCode) {
                int[] updated = new int[previouslyInstalledVersions.length + 1];
                System.arraycopy(previouslyInstalledVersions, 0, updated, 0, previouslyInstalledVersions.length);
                updated[previouslyInstalledVersions.length] = versionCode;
                previouslyInstalledVersions = updated;
                SharedPreferences sharedPref = context.getSharedPreferences(GlobalVars.USAGE_STATS_PREFERENCE_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(GlobalVars.KEY_PREV_VERSIONS, encodeCSV(previouslyInstalledVersions));
                editor.apply();
            }
        }
    }

    private static int[] decodeCSV(String csv) {
        if(csv.length() == 0) {
            return new int[0];
        } else if(csv.contains(",")) {
            String[] parts = csv.split(",");
            int[] versions = new int[parts.length];
            for(int i=0;i<parts.length;i++) {
                versions[i] = Integer.valueOf(parts[i]);
            }
            return versions;
        } else {
            int[] versions = new int[1];
            versions[0] = Integer.valueOf(csv);
            return versions;
        }
    }

    private static String encodeCSV(@NonNull int[] input) {
        StringBuilder stringBuilder = new StringBuilder("");
        for(int i=0;i<input.length;i++) {
            stringBuilder.append(input[i]);
            if(i < (input.length - 1)) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Only to be called after init is done
     */
    private static int getLastInstalledVersion(Context context) {
        if(initialized && previouslyInstalledVersions.length > 0)
            return previouslyInstalledVersions[previouslyInstalledVersions.length - 1];
        return -1;
    }

    /**
     * To be used when actions have to be performed when upgrading between specific versions
     */
    public static void run(Context context) {
        init(context);
    }

}
