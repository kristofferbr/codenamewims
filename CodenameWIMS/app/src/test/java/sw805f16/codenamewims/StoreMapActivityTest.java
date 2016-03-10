package sw805f16.codenamewims;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by kbrod on 10/03/2016.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class StoreMapActivityTest {

    public void tests(){

        StoreMapActivity storemap = Robolectric.setupActivity(StoreMapActivity.class);

        


    }
}
