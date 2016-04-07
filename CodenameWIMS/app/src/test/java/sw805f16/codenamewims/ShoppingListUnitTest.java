package sw805f16.codenamewims;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.*;;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by Kogni on 06-Apr-16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ShoppingListUnitTest {

    ShoppingListActivity shoppingListActivity;
    ItemEnum unmarked_item, marked_item, unavailable_item;
    ShoppingListAdapter shoppingListAdapter;
    LinearLayout firstLayout;
    LinearLayout secondLayout;
    Pair<LinearLayout, ItemEnum> firstItem;
    Pair<LinearLayout, ItemEnum> secondItem;
    ListView itemListView;
    ArrayList<Pair<LinearLayout, ItemEnum>> itemList = new ArrayList<>();
    ShoppingListFragment testFragment;

    @Before
    public void setup(){
        unmarked_item = ItemEnum.UNMARKED;
        marked_item = ItemEnum.MARKED;
        unavailable_item = ItemEnum.UNAVAILABLE;

        shoppingListActivity = Robolectric.setupActivity(ShoppingListActivity.class);
        testFragment = (ShoppingListFragment) shoppingListActivity.getFragmentManager().findFragmentByTag("shoppingFragment");

        itemListView = (ListView) testFragment.getView().findViewById(R.id.itemList);

        firstLayout = (LinearLayout) LinearLayout.inflate(testFragment.getActivity().getApplicationContext(),
                R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());
        secondLayout = (LinearLayout) LinearLayout.inflate(testFragment.getActivity().getApplicationContext(),
                R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());

        firstItem = new Pair<LinearLayout, ItemEnum>(firstLayout, ItemEnum.MARKED);
        secondItem = new Pair<LinearLayout, ItemEnum>(secondLayout, ItemEnum.UNMARKED);
        itemList.add(firstItem);
        itemList.add(secondItem);

        shoppingListAdapter = new ShoppingListAdapter(testFragment.getActivity().getApplicationContext(),
                R.layout.simple_list_view, itemList);
    }

    @Test
    public void enumTest(){
        assertThat(unmarked_item, is(ItemEnum.UNMARKED));
        assertThat(marked_item, is(ItemEnum.MARKED));
        assertThat(unavailable_item, is(ItemEnum.UNAVAILABLE));
    }

    @Test
    public void shoppingListAdapterTest(){
        shoppingListAdapter.swap(0,1);
        assertThat(shoppingListAdapter.getItem(0), is(secondItem));
        assertThat(shoppingListAdapter.getItem(1), is(firstItem));
    }
}
