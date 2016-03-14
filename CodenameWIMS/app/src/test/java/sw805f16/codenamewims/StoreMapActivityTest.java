package sw805f16.codenamewims;

import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by kbrod on 10/03/2016.
 * I'be tried so hard, and got so far
 * But in the eeend it doesn't even matter
 *
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class StoreMapActivityTest {

    @Test
    public void tests(){

        StoreMapActivity storemap = Robolectric.setupActivity(StoreMapActivity.class);

        storemap.getMapLayout();

        android.os.SystemClock.sleep(5000);
        ImageView image = (ImageView)storemap.findViewById(R.id.storemap);

        Assert.assertNotNull(image.getDrawable());


    }
}
