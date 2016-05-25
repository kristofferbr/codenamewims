package sw805f16.codenamewims;

import org.json.JSONObject;
import android.content.Intent;
import android.widget.FrameLayout;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowListView;

import java.util.ArrayList;

import static org.robolectric.Shadows.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by Kogni on 25-May-16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class JSONTest {
    String jsonString;
    JSONContainer jsonContainer;
    ArrayList<WimsPoints> products;
    WimsPoints product;
    JSONObject jsonObject;
    MainActivity activity;

    @Before
    public void setup(){
        activity = Robolectric.setupActivity(MainActivity.class);
        jsonString = "{\"points\":[{\"_id\":\"1\",\"x\":0,\"y\":0,\"neighbors\":[],\"fingerprint\":[],\"probabilities\":[]},{\"_id\":\"2\",\"x\":0,\"y\":0,\"neighbors\":[],\"fingerprint\":[],\"probabilities\":[]},{\"_id\":\"3\",\"x\":0,\"y\":0,\"neighbors\":[],\"fingerprint\":[],\"probabilities\":[]}]}";
        products = new ArrayList<>();
        product = new WimsPoints(0.0f, 0.0f);
        product.ID = "1";
        products.add(product);

        product = new WimsPoints(0.0f, 0.0f);
        product.ID = "2";
        products.add(product);

        product = new WimsPoints(0.0f, 0.0f);
        product.ID = "3";
        products.add(product);
    }

    @Test
    public void constructJsonObjectTest(){
        jsonObject = activity.constructJson(products);
        assertThat(jsonObject.toString(), is(jsonString));
    }

    @Test
    public void constuctJsonObjectFromEmptyStringTest(){
        jsonObject = activity.constructJson(new ArrayList<WimsPoints>());
        assertThat(jsonObject.toString(), is("{\"points\":[]}"));
    }
}
