package sw805f16.codenamewims;

import android.content.ClipData;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Kogni on 13-Apr-16.
 */
public class LinearItemLayout extends LinearLayout {
    private Integer imageId;
    private ItemEnum status;

    LinearItemLayout(Context context){
        super(context);
    }
    LinearItemLayout(Context context, ViewGroup viewGroup ){
        super(context);
        View.inflate(context, R.layout.item_layout, viewGroup);
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public ItemEnum getStatus() {
        return status;
    }

    public void setStatus(ItemEnum status) {
        this.status = status;
    }
}

