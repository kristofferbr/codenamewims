package sw805f16.codenamewims;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Kogni on 29-Apr-16.
 */
public class LinearItemLayoutStatusComparator implements Comparator <LinearItemLayout> {
    ArrayList<WimsPoints> compareList;

    public LinearItemLayoutStatusComparator(ArrayList<WimsPoints> objects){
        this.compareList = objects;
    }

    public int compare(LinearItemLayout lhs, LinearItemLayout rhs){
        if(lhs.getStatusInt() - rhs.getStatusInt() == 0 && !compareList.isEmpty()){
            WimsPoints left = compareList.get(0);
            WimsPoints right = compareList.get(0);
            for(int i = 0; i < compareList.size();i++){
                if(lhs.getText().equals(compareList.get(i).getProductName())){
                    left = compareList.get(i);
                }
                if(rhs.getText().equals(compareList.get(i).getProductName())){
                    right = compareList.get(i);
                }
            }
            return compareList.indexOf(left) - compareList.indexOf(right);
        }
        else
            return lhs.getStatusInt() - rhs.getStatusInt();
    }
}
