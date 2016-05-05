package sw805f16.codenamewims;

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

import static org.robolectric.Shadows.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by Kogni on 14-Apr-16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class CurrentItemTest {

    StoreMapActivity activity;
    ShoppingListFragment fragment;
    ShadowListView shadowItemList;
    ListView itemList;
    FrameLayout currentItem;
    TextView search;
    ShadowListView suggestionList;

    @Before
    public void setup(){
        Intent intent=new Intent();
        intent.putExtra("storeId","56e6a28a28c3e3314a6849df");
        activity= Robolectric.buildActivity(StoreMapActivity.class).withIntent(intent).create().get();
        fragment=(ShoppingListFragment)activity.getFragmentManager().findFragmentByTag("shoppingFragment");
        search=(TextView)fragment.getView().findViewById(R.id.item_textfield);
        suggestionList=shadowOf((ListView)fragment.getView().findViewById(R.id.suggestions));
        shadowItemList=shadowOf((ListView)fragment.getView().findViewById(R.id.itemList));
        itemList=(ListView)fragment.getView().findViewById(R.id.itemList);
        currentItem=(FrameLayout)fragment.getView().findViewById(R.id.currentItem);

        try{
           String jsonString=activity.getResources().getString(R.string.shop_json);
            JSONObject dummyJson=new JSONObject(jsonString);
            JSONContainer.extractProductInformationFromJson(dummyJson, activity);
        }
        catch(JSONException e){
            e.printStackTrace();
        }

        search.setText("Milk");
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowItemList.populateItems();

        search.setText("Ost");
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowItemList.populateItems();
    }

    @Test
    public void currentItemMark(){
        RelativeItemLayout item = (RelativeItemLayout) currentItem.getChildAt(0);
        ImageView actual = (ImageView) item.getChildAt(1);
        currentItem.performClick();
        assertThat(actual.getDrawable(), is(activity.getResources().getDrawable(R.drawable.checkmark)));
    }

    public void currentItemSkip(){
        RelativeItemLayout item = (RelativeItemLayout) currentItem.getChildAt(0);
        ImageView actual = (ImageView) item.getChildAt(1);
        currentItem.performLongClick();
        assertThat(actual.getDrawable(), is(activity.getResources().getDrawable(R.drawable.skip)));
    }
}
