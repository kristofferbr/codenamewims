package sw805f16.codenamewims;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Kogni on 13-Apr-16.
 */
public class LinearItemLayout extends LinearLayout {
    private Integer imageId = null;
    private ItemEnum status;

    public LinearItemLayout(Context context){
        super(context);
    }
    public LinearItemLayout(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public LinearItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    LinearItemLayout(Context context, ViewGroup viewGroup ){
        super(context);
        LinearLayout.inflate(context, R.layout.item_layout, this);
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

