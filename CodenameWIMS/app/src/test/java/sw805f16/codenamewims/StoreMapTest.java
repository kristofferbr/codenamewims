package sw805f16.codenamewims;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by kbrod on 09/03/2016.
 * Another one does a test...
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class StoreMapTest {


    @Test
    //As a user
    // I want to be able to see the entire map of the store I have chosen
    // so that I can get an overview of the store
    public void store_map_test(){
        // Given that I am a user
        // And i Have chosen a store
        StoreMapActivity storemap = Robolectric.setupActivity(StoreMapActivity.class);
        storemap.store_id = "et id";
        // I want to be able to see the map of the store
        ImageView map = (ImageView) storemap.findViewById(R.id.storemap);
        // And then I want to be able to drag and zoom in the map


    }

}
