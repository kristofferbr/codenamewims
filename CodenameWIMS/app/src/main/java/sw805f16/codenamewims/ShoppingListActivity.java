package sw805f16.codenamewims;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShoppingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ShoppingListFragment fragment = new ShoppingListFragment();
        fragmentTransaction.add(R.id.shoppingParent, fragment, "shoppingFragment");
        fragmentTransaction.commit();
    }
}
