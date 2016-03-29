package sw805f16.codenamewims;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShoppingListActivity extends AppCompatActivity {

    ShoppingListFragment fragment = new ShoppingListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

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
