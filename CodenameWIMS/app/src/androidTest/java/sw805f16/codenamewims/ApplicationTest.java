package sw805f16.codenamewims;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.junit.Test;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {


    public void test() throws Exception {
        final int expected = 1;
        final int reality = 1;
        assertEquals(expected, reality);
    }

    @Test
    public void testVectorRotation(){
        double givenX =0;
        double givenY=1;
        double res[];

        MainActivity main = new MainActivity();

        res = main.rotatevector(givenX,givenY,90);
        assertEquals(-1d,res[0]);
        assertEquals(0d,(double)Math.round(res[1]));
    }

    public void testVectorclockwise(){
        double givenX =0;
        double givenY=1;
        double res[];

        MainActivity main = new MainActivity();

        res = main.rotatevector(givenX,givenY,-90);
        assertEquals(1d,res[0]);
        assertEquals(0d,(double)Math.round(res[1]));

    }

    public ApplicationTest() {
        super(Application.class);
    }
}





