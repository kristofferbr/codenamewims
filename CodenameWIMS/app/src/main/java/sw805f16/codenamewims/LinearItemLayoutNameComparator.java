package sw805f16.codenamewims;

import java.util.Comparator;

/**
 * Created by Kogni on 29-Apr-16.
 */
public class LinearItemLayoutNameComparator implements Comparator<LinearItemLayout> {

    public int compare(LinearItemLayout lhs, LinearItemLayout rhs){
        int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getText(), rhs.getText());
        return (res != 0) ? res : lhs.getText().compareTo(rhs.getText());
    }
}