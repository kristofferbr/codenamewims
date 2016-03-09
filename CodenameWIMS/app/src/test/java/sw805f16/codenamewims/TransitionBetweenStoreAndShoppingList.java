package sw805f16.codenamewims;

import android.view.View;
import android.widget.Button;

import org.apache.tools.ant.helper.ProjectHelper2;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.*;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by kbrod on 09/03/2016.
 * Acceptance test class for use case Transition Between store and shopping list
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TransitionBetweenStoreAndShoppingList {

    @Test
    // As a User
    // I want to be able to enter a Shopping list based on the chosen store
    // so that I can construct a shopping list based on the products available in the chosen store
    public void transition_between_store_and_shoppinglist(){
        // Give I am a user
        // When I have chosen a store

        // Then I want to be able to make a shopping list based on the store by clicking
        // the shopping button

        // And the items that I can choose between is only items that belong to the store


    }

}
