package sw805f16.codenamewims;

import java.util.Comparator;

/**
 * Created by Kogni on 29-Apr-16.
 */
public class LinearItemLayoutStatusComparator implements Comparator <LinearItemLayout> {

    public int compare(LinearItemLayout lhs, LinearItemLayout rhs){
        return lhs.getStatusInt() - rhs.getStatusInt();
    }
}
