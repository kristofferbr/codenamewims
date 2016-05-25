package sw805f16.codenamewims;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import static org.robolectric.Shadows.*;

/**
 * Created by kbrod on 10/03/2016.
 * I'be tried so hard, and got so far
 * But in the eeend it doesn't even matter
 *
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class StoreMapActivityTest {

    StoreMapActivity activity;

    @Before
    public void setup(){
        activity = Robolectric.setupActivity(StoreMapActivity.class);
    }


    @Test
    public void NNTP1_1(){
        WimsPoints start = new WimsPoints(0,0);
        WimsPoints first = new WimsPoints(1,2);
        WimsPoints second = new WimsPoints(2,3);
        WimsPoints third = new WimsPoints(5,4);
        WimsPoints fourth = new WimsPoints(4,4);
        WimsPoints fifth = new WimsPoints(4,2);

        ArrayList<WimsPoints> locations = new ArrayList<>();
        locations.add(first); locations.add(second); locations.add(third);
        locations.add(fourth); locations.add(fifth);

        assertEquals(first, activity.nearestNeighbour(start, locations));
    }

    @Test
    public void NNTP1_2(){
        WimsPoints start = new WimsPoints(0,0);
        ArrayList<WimsPoints> locations = new ArrayList<>();

        assertNull(activity.nearestNeighbour(start, locations));
    }

    @Test
    public void NNTP1_3(){
        WimsPoints start = new WimsPoints(-5,0);
        WimsPoints first = new WimsPoints(1,2);
        WimsPoints second = new WimsPoints(2,3);
        WimsPoints third = new WimsPoints(5,4);
        WimsPoints fourth = new WimsPoints(4,4);
        WimsPoints fifth = new WimsPoints(4,2);

        ArrayList<WimsPoints> locations = new ArrayList<>();
        locations.add(first); locations.add(second); locations.add(third);
        locations.add(fourth); locations.add(fifth);

        assertNull(activity.nearestNeighbour(start, locations));
    }

    @Test
    public void NNTP1_4(){
        WimsPoints start = new WimsPoints(0,0);
        WimsPoints first = new WimsPoints(1,-2);
        WimsPoints second = new WimsPoints(2,3);
        WimsPoints third = new WimsPoints(5,4);
        WimsPoints fourth = new WimsPoints(4,4);
        WimsPoints fifth = new WimsPoints(4,2);

        ArrayList<WimsPoints> locations = new ArrayList<>();
        locations.add(first); locations.add(second); locations.add(third);
        locations.add(fourth); locations.add(fifth);

        assertEquals(second, activity.nearestNeighbour(start, locations));
    }

    @Test
    public void NNTP1_5(){
        WimsPoints start = new WimsPoints(0,0);
        WimsPoints first = new WimsPoints(1,2);
        WimsPoints second = new WimsPoints(2,1);
        WimsPoints third = new WimsPoints(5,4);
        WimsPoints fourth = new WimsPoints(4,4);
        WimsPoints fifth = new WimsPoints(4,2);

        ArrayList<WimsPoints> locations = new ArrayList<>();
        locations.add(first); locations.add(second); locations.add(third);
        locations.add(fourth); locations.add(fifth);

        assertEquals(first, activity.nearestNeighbour(start, locations));
    }
}
