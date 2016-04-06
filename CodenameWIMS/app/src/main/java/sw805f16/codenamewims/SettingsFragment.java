package sw805f16.codenamewims;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Flækker-Maskinen on 05/04/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
