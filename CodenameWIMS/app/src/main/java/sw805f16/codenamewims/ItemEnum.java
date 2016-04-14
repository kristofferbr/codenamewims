package sw805f16.codenamewims;

import android.content.ClipData;

/**
 * Created by Kogni on 06-Apr-16.
 */
public enum ItemEnum {
    UNMARKED, MARKED, UNAVAILABLE;

    public ItemEnum changeStatus(boolean longclicked){
        ItemEnum test = this;
        switch(this.ordinal()){
            case 0:
                if(longclicked)
                    return UNAVAILABLE;
                else
                    return MARKED;
            case 1:
                if(longclicked)
                    return UNAVAILABLE;
                else
                    return UNMARKED;
            default:
                return UNMARKED;
        }

    }
}


