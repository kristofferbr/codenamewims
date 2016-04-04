package sw805f16.codenamewims;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ShoppingListActivity extends AppCompatActivity {

    private Toolbar toolbar;

    ShoppingListFragment fragment = new ShoppingListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
      //  ActionBar ab = getSupportActionBar();

        // Enable the Up button
        //ab.setDisplayHomeAsUpEnabled(true);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (getIntent().getStringExtra("storeId") != null) {
            fragment = ShoppingListFragment.newInstance(getIntent().getStringExtra("storeId"));
        }
        if (getIntent().getParcelableExtra("state") != null) {
            fragment.setInitialSavedState((Fragment.SavedState) getIntent().getParcelableExtra("state"));
        }

        fragmentTransaction.add(R.id.shoppingParent, fragment, "shoppingFragment");
        fragmentTransaction.commit();
    }

    /**
     * Inflate the menu
     * @param menu main_menu
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        //inflate the menu: this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method responsible for the transition between this activity and {@link MainActivity}
     */
    public void transitionToStartScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //We save the state of the fragment and put it in the intent
        Parcelable state = getFragmentManager().saveFragmentInstanceState(fragment);
        intent.putExtra("state", state);
        intent.putExtra("storeId", fragment.getStoreId());
        startActivity(intent);
    }

    /**
     * This method is responsible for the transition between this activity and {@link StoreMapActivity}
     */
    public void transitionToStoreMap() {
        Intent intent = new Intent(getApplicationContext(), StoreMapActivity.class);
        Parcelable state = getFragmentManager().saveFragmentInstanceState(fragment);
        intent.putExtra("state", state);
        intent.putExtra("storeId", fragment.getStoreId());
        startActivity(intent);
    }

}
