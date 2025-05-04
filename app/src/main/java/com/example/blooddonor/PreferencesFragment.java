package com.example.blooddonor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.blooddonor.R;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesFragment extends Fragment {

    private static final String PREF_NAME = "BloodDonorAppPrefs";
    private static final String KEY_DARK_MODE = "darkMode";

    private Switch switchTheme;
    private SharedPreferences prefs;

    public PreferencesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_preferences_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        switchTheme = view.findViewById(R.id.switch_theme);

        prefs = requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        switchTheme.setChecked(prefs.getBoolean(KEY_DARK_MODE, false));

        switchTheme.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_DARK_MODE, isChecked);
            editor.apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });



    }
}
