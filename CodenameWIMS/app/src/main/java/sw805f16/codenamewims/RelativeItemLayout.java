package sw805f16.codenamewims;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Comparator;

/**
 * Created by Kogni on 13-Apr-16.
 */
public class RelativeItemLayout extends RelativeLayout implements Comparator {
    private Integer imageId = null;
    private ItemEnum status;

    public RelativeItemLayout(Context context){
        super(context);
    }
    public RelativeItemLayout(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public RelativeItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    RelativeItemLayout(Context context, ViewGroup viewGroup){
        super(context);
        LinearLayout.inflate(context, R.layout.item_layout, this);
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        if(imageId != null && imageId != 0) {
            this.imageId = imageId;
            ImageView mark = (ImageView) getChildAt(1);
            mark.setImageDrawable(getContext().getResources().getDrawable(imageId));
            removeView(mark);
            addView(mark, 1);
        }
        else{
            this.imageId = 0;
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
        RelativeItemLayout lhs = (RelativeItemLayout)obj1;
        RelativeItemLayout rhs = (RelativeItemLayout)obj2;

        return lhs.getStatusInt() - rhs.getStatusInt();
    }
}

