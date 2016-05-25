package sw805f16.codenamewims;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by Kogni on 25-May-16.
 */
public class JSONTest {
    String jsonString;
    JSONContainer jsonContainer;
    ArrayList<WimsPoints> products;
    WimsPoints product;

    @Before
    public void setup(){
        jsonString = "[{\"name\": \"Minced Beef\",\"description\":\"Slaughtered cow cut into tiny pieces\"},{\"name\": \"Milk\",\"description\": \"Put it on cereal, drink it, or take a bath. Do whatever you want with it, you baught it!\"},{\"name\": \"Cheese\",\"description\": \"\",}]";
        products = new ArrayList<>();
        product = new WimsPoints();
        product.setProductName("Minced Beef");
        products.add(product);

        product = new WimsPoints();
        product.setProductName("Milk");
        products.add(product);

        product = new WimsPoints();
        product.setProductName("Cheese");
        products.add(product);
    }

    @Test
    public void correctJsonStringTest(){
        JSONContainer.populateWithStringInformation(jsonString);
        for(WimsPoints w : JSONContainer.getProducts()){
            assertThat(products.contains(w), is(true));
        }
    }

    @Test
    public void emptyJsonStringTest(){
        JSONContainer.forceClearProducts();
        JSONContainer.populateWithStringInformation("");
        assertThat(JSONContainer.getProducts().isEmpty(), is(true));
    }
}
