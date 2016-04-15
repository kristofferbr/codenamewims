
package sw805f16.codenamewims;

import android.view.View;
import android.widget.Button;

import org.apache.tools.ant.helper.ProjectHelper2;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.*;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


import static org.junit.Assert.*;
/**
 * Created by kbrod on 09/03/2016.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class LanguageTest {

    @Test
    //As a user
    //I want to choose between Danish and English language in the application
    //so that I can understand the text in the application if I am not danish speaking
    public void language_use_case(){
        // Given I am an user
        // When I am at the home screen
        //    MainActivity man = Robolectric.setupActivity(MainActivity.class);
        // And I tap the flag button
        //    Button flagButton = (Button) man.findViewById(R.id.flagknap);
        //    flagButton.performClick();
        // I can choose Danish language
            // Kode for at asserte klik på det ene sprog
        // I can choose English Language
            // Kode for at asserte klik på det andet sprog

    }









}
