package my.poc.demo.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import com.huamai.poc.PocEngine;
import com.huamai.poc.PocEngineFactory;

import java.util.List;

import my.poc.demo.R;

public class SettingsActivity extends PreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String key = preference.getKey();
            PocEngine.Configure configure = PocEngineFactory.get().getConfig();

            //fragment1
            if ("resolution_list".equals(key)) {
                configure.videoResolution = Integer.valueOf(value + "");
            } else if ("fps_list".equals(key)) {
                configure.videoFps = Integer.valueOf(value + "");
            } else if ("bitrate_list".equals(key)) {
                configure.bitRate = Integer.valueOf(value + "");
            } else if ("qos_fec_switch".equals(key)) {
                //configure.qosFec = (boolean) value;
            } else if ("fec_rate_list".equals(key)) {
                configure.videoFecRate = Integer.valueOf(value + "");
            } else if ("agc_level_list".equals(key)) {
                configure.audioAgcLevel = Integer.valueOf(value + "");
            } else if ("ns_level_list".equals(key)) {
                configure.audioNsLevel = Integer.valueOf(value + "");
            } else if ("video_landscape".equals(key)) {
                //configure.videoLandscape = (boolean) value;
            }

            //fragment 2
            else if ("do_not_disturb".equals(key)) {
                configure.callNotDisturb = (boolean) value;
            }

            PocEngineFactory.get().config(configure);
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        //显示已设置参数
        PocEngine.Configure configure = PocEngineFactory.get().getConfig();
        //测试默认数据
        configure.qosFec = true;

        String key = preference.getKey();
        if ("resolution_list".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.videoResolution);
            preference.setDefaultValue(configure.videoResolution);
        } else if ("fps_list".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.videoFps);
            preference.setDefaultValue(configure.videoFps);
        } else if ("bitrate_list".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.bitRate);
            preference.setDefaultValue(configure.bitRate);
        } else if ("qos_fec_switch".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.qosFec);
            preference.setDefaultValue(configure.qosFec);
        } else if ("fec_rate_list".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.videoFecRate);
            preference.setDefaultValue(configure.videoFecRate);
        } else if ("agc_level_list".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.audioAgcLevel);
            preference.setDefaultValue(configure.audioAgcLevel);
        } else if ("ns_level_list".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.audioNsLevel);
            preference.setDefaultValue(configure.audioNsLevel);
        } else if ("video_landscape".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.videoFecRate);
//            preference.setDefaultValue(configure.videoLandscape);
        }  else if ("qos_fec_switch".equals(key)) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, configure.qosFec);
            preference.setDefaultValue(configure.qosFec);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            ((SwitchPreference) findPreference("video_landscape")).setChecked(true);

            bindPreferenceSummaryToValue(findPreference("resolution_list"));
            bindPreferenceSummaryToValue(findPreference("fps_list"));
            bindPreferenceSummaryToValue(findPreference("bitrate_list"));
            bindPreferenceSummaryToValue(findPreference("qos_fec_switch"));
            bindPreferenceSummaryToValue(findPreference("fec_rate_list"));
            bindPreferenceSummaryToValue(findPreference("agc_level_list"));
            bindPreferenceSummaryToValue(findPreference("ns_level_list"));
            bindPreferenceSummaryToValue(findPreference("video_landscape"));



            PreferenceManager.getDefaultSharedPreferences(null).getBoolean("", false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OtherPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_other);

            bindPreferenceSummaryToValue(findPreference("do_not_disturb"));
        }
    }
}