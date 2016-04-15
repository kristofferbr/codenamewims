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
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class ShoppingListUnitTest {

    ShoppingListActivity shoppingListActivity;
    ItemEnum unmarked_item, marked_item, unavailable_item;
    ShoppingListAdapter shoppingListAdapter;
    LinearItemLayout firstLayout;
    LinearItemLayout secondLayout;
    LinearItemLayout dummyLayout;
    ListView itemListView;
    ArrayList<LinearItemLayout> itemList = new ArrayList<>();
    ShoppingListFragment testFragment;

    @Before
    public void setup(){
        unmarked_item = ItemEnum.UNMARKED;
        marked_item = ItemEnum.MARKED;
        unavailable_item = ItemEnum.UNAVAILABLE;

        shoppingListActivity = Robolectric.setupActivity(ShoppingListActivity.class);
        testFragment = (ShoppingListFragment) shoppingListActivity.getFragmentManager().findFragmentByTag("shoppingFragment");

        itemListView = (ListView) testFragment.getView().findViewById(R.id.itemList);

        firstLayout = new LinearItemLayout(testFragment.getActivity().getApplicationContext(),(ViewGroup) itemListView.getEmptyView());
        secondLayout = new LinearItemLayout(testFragment.getActivity().getApplicationContext(),(ViewGroup) itemListView.getEmptyView());
        dummyLayout = new LinearItemLayout(testFragment.getActivity().getApplicationContext(),(ViewGroup) itemListView.getEmptyView());

        firstLayout.setStatus(ItemEnum.MARKED);
        secondLayout.setStatus(ItemEnum.UNMARKED);
        itemList.add(firstLayout);
        itemList.add(secondLayout);

        shoppingListAdapter = new ShoppingListAdapter(testFragment.getActivity().getApplicationContext(),
                R.layout.simple_list_view, itemList);
    }

    @Test
    public void enumTest(){
        assertThat(unmarked_item, is(ItemEnum.UNMARKED));
        assertThat(marked_item, is(ItemEnum.MARKED));
        assertThat(unavailable_item, is(ItemEnum.UNAVAILABLE));
        ItemEnum testEnum = ItemEnum.UNMARKED;
        testEnum = testEnum.changeStatus(false);
        assertThat(testEnum, is(ItemEnum.MARKED));
        testEnum = testEnum.changeStatus(false);
        assertThat(testEnum, is(ItemEnum.UNMARKED));
        testEnum = testEnum.changeStatus(true);
        assertThat(testEnum, is(ItemEnum.UNAVAILABLE));
    }

    @Test
    public void shoppingListAdapterTest() {
        shoppingListAdapter.swap(0, 1);
        assertThat(shoppingListAdapter.getItem(0), is(secondLayout));
        assertThat(shoppingListAdapter.getItem(1), is(firstLayout));
        assertThat(shoppingListAdapter.getCount(), is(2));

        LinearItemLayout dummyItem = dummyLayout;
        dummyLayout.setStatus(ItemEnum.UNAVAILABLE);
        shoppingListAdapter.add(dummyItem);
        shoppingListAdapter.add(dummyItem);
        shoppingListAdapter.add(dummyItem);
        shoppingListAdapter.add(dummyItem);

        shoppingListAdapter.swap(5, 1);

        assertThat(shoppingListAdapter.getItem(5), is(firstLayout));
        assertThat(shoppingListAdapter.getItem(1), is(dummyItem));
        assertThat(shoppingListAdapter.getCount(), is(6));
    }

    @Test
    public void shoppingListAdapterMarkUnmarkTest(){

    }
}
