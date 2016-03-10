package sw805f16.codenamewims;

import android.view.View;

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
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExampleUnitTest {
    /*
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);

    }*/


    @Test
    public void test_activity() throws Exception {

        MainActivity man = Robolectric.setupActivity(MainActivity.class);

        //View w = man.findViewById(R.id.zoombutton);
        //w.performClick();


        assertEquals(man.addTwoNumbers(1,2),3);

    }




}