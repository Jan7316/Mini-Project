package mini.app.orbis;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Jan on 14/12/2016.
 */

public class OrbisSettings {

    private static boolean initialized = false;
    private static HashMap<OrbisSetting, Object> settings;

    public static String getStringSetting(Context context, OrbisSetting setting) {
        return (String) getSetting(context, setting);
    }

    public static int getIntSetting(Context context, OrbisSetting setting) {
        return (int) getSetting(context, setting);
    }

    public static boolean getBoolSetting(Context context, OrbisSetting setting) {
        return (boolean) getSetting(context, setting);
    }

    public static Object getObjectSetting(Context context, OrbisSetting setting) {
        return getSetting(context, setting);
    }

    private static Object getSetting(Context context, OrbisSetting setting) {
        if(!initialized)
            initialiseSettings(context);
        return settings.get(setting);
    }

    static Setting[] availableSettings = {
            new Setting(OrbisSetting.diashowStartDelay, "d_start_delay", 10, ValueType.integer), // TODO: find useful default values for these properties
            new Setting(OrbisSetting.diashowTPI, "d_tpi", 5, ValueType.integer),
            new Setting(OrbisSetting.diashowDirection, "d_dir", true, ValueType.bool),
            new Setting(OrbisSetting.diashowMode, "d_mode", 2, ValueType.integer),
            new Setting(OrbisSetting.diashowNumberOfImages, "d_nimg", 10, ValueType.integer)
    };

    private static void initialiseSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GlobalVars.SETTINGS_PREFERENCE_FILE, Context.MODE_PRIVATE);
        settings = new HashMap<>();
        for(Setting setting : availableSettings) {
            switch(setting.type) {
                case string:
                    settings.put(setting.key, prefs.getString(setting.name, (String) setting.dafaultValue));
                    break;
                case integer:
                    settings.put(setting.key, prefs.getInt(setting.name, (int) setting.dafaultValue));
                    break;
                case bool:
                    settings.put(setting.key, prefs.getBoolean(setting.name, (boolean) setting.dafaultValue));
                    break;
            }
        }
        initialized = true;
    }

    public enum OrbisSetting {
        diashowStartDelay, // in s
        diashowTPI, // Time per image, in s
        diashowDirection, // true = forwards, false = backwards
        diashowMode, // Num of imgs = 0; to end = 1; continuous = 2
        diashowNumberOfImages // For mode 0
    }

    public static class Setting {
        public OrbisSetting key;
        public String name;
        Object dafaultValue;
        public ValueType type;
        public Setting(OrbisSetting setting, String name, Object def, ValueType type) {
            this.key = setting;
            this.name = name;
            this.type = type;
            this.dafaultValue = def;
        }
    }

    public enum ValueType {
        string, integer, bool
    }
}
