package sw805f16.codenamewims;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Kogni on 13-Apr-16.
 */
public class LinearItemLayout extends RelativeLayout implements Comparator {
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
        if(imageId != 0) {
            this.imageId = imageId;
            ImageView mark = (ImageView) getChildAt(1);
            mark.setImageDrawable(getContext().getResources().getDrawable(imageId));
            removeView(mark);
            addView(mark, 1);
        }
        else{
            this.imageId = imageId;
            ImageView mark = (ImageView) getChildAt(1);
            mark.setImageDrawable(null);
            removeView(mark);
            addView(mark, 1);
        }
    }

    public ItemEnum getStatus() {
        return status;
    }

    public int getStatusInt(){
        return status.getMark();
    }

    public void setStatus(ItemEnum status) {
        this.status = status;
    }

    public String getText(){
        TextView text = (TextView) this.getChildAt(0);
        return (String) text.getText();
    }

    public void setText(String text){
        TextView textView = (TextView) this.getChildAt(0);
        textView.setText(text);
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        LinearItemLayout lhs = (LinearItemLayout)obj1;
        LinearItemLayout rhs = (LinearItemLayout)obj2;

        return lhs.getStatusInt() - rhs.getStatusInt();
    }
}

