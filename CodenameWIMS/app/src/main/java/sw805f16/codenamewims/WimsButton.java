package sw805f16.codenamewims;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Netray on 10/04/2016.
 */
public class WimsButton extends LinearLayout {
    private TextView textView;
    private LinearLayout.LayoutParams textViewParams;

    private Drawable icon;
    private String buttonText;
    private ImageView iconView;

    private final int ICON_MAX_WIDTH = (int) WimsScalingUtilities.convertDpToPixel(this.getContext(), 45);
    private final int ICON_MAX_HEIGHT = (int) WimsScalingUtilities.convertDpToPixel(this.getContext(), 45);

    private final int SUBVIEW_SPACING = (int) WimsScalingUtilities.convertDpToPixel(this.getContext(), 10);
    private final int BUTTON_PADDING = (int) WimsScalingUtilities.convertDpToPixel(this.getContext(), 10); // This one makes the icon in a button smaller and larger

    public WimsButton(Context context, Drawable icon) {
        this(context, icon, null);
    }

    public WimsButton(Context context, String buttonText) {
        this(context, null, buttonText);
    }

    public WimsButton(Context context, Drawable icon, String buttonText) {
        super(context);
        this.icon = icon;
        this.buttonText = buttonText;
        initializeButton(null);
    }

    public WimsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeButton(attrs);
    }

    public WimsButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeButton(attrs);
    }

    private boolean firstTimeLayout = true;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (firstTimeLayout) {

            if (buttonText != null) {

                if(icon != null) {

                    textViewParams.setMargins(SUBVIEW_SPACING, 0, 0, 0);
                }

                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.getHeight() - (int)(2.5 * BUTTON_PADDING));
                textView.setIncludeFontPadding(false);
            }
            firstTimeLayout = !firstTimeLayout;
        }
    }

    private void initializeButton(AttributeSet attrs) {

        this.setOrientation(LinearLayout.HORIZONTAL);

        this.setPadding(BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING);
        this.setGravity(Gravity.CENTER);

        iconView = new ImageView(this.getContext());

        iconView.setAdjustViewBounds(true);

        iconView.setMaxWidth(ICON_MAX_WIDTH);
        iconView.setMaxHeight(ICON_MAX_HEIGHT);

        if (attrs != null) {

            TypedArray wimsButtonAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.WimsButton);
            if(icon == null) {
                icon = wimsButtonAttributes.getDrawable(R.styleable.WimsButton_button_icon);
            }
            if(buttonText == null) {
                buttonText = wimsButtonAttributes.getString(R.styleable.WimsButton_button_text);
            }
            wimsButtonAttributes.recycle();
        }

        if(icon == null && buttonText == null) {
            throw new IllegalArgumentException("A WimsButton must have an icon or some text");
        }

        if(icon == null)
        {
            iconView.setVisibility(View.GONE);
        }
        else {
            iconView.setImageDrawable(icon);
        }

        textView = new TextView(this.getContext());

        textView.setText(buttonText);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 0, 0, 0);

        textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // This is custom buttom style
        this.setBackgroundResource(R.drawable.start_button);
        
        if (!this.isEnabled()) {
            iconView.setAlpha(0x59);
        }

        this.addView(textView, textViewParams);

        this.addView(iconView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
    public void setIcon(Drawable icon){
        iconView.setImageDrawable(icon);
    }
}
