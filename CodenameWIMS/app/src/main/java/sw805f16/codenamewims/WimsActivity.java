package sw805f16.codenamewims;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Netray on 10/04/2016.
 */
public class WimsActivity extends FragmentActivity {

    private final float ACTION_BAR_SPACING_LEFT_RIGHT = 5; // Spacing in the action bar (left & right)
    private final float ACTION_BAR_SPACING_TOP_BOTTOM = 6; // Spacing in the action bar (top & bottom)
    private final float ACTION_BAR_TEXT_PADDING = 4 * ACTION_BAR_SPACING_TOP_BOTTOM; // Padding on the text
    private ActionBar actionBar; // The action bar
    private RelativeLayout actionBarCustomView; // The custom action bar view
    private LeftActionBarLayout actionBarCustomViewLeft; // The left side of the action bar
    private RightActionBarLayout actionBarCustomViewRight; // The right side of the action bar
    private TextView actionBarTitleView; // The view which contains the title of the action bar

    // Constants to use when inserting buttons to the action bar
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    // Layout placed to the right in the action bar.
    private class RightActionBarLayout extends LinearLayout {

        public RightActionBarLayout(Context context) {
            super(context);
            init();
        }

        public RightActionBarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public RightActionBarLayout(Context context, AttributeSet attrs, int defStyle){
            super(context, attrs, defStyle);
            init();
        }

        private void init() {
            this.setGravity(Gravity.RIGHT);
        }

        public void addWimsButton(final WimsButton wimsButton) {
            final LinearLayout.LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            buttonParams.rightMargin = (int) WimsScalingUtilities.convertDpToPixel(getContext(), ACTION_BAR_SPACING_LEFT_RIGHT);
            this.addView(wimsButton, buttonParams);
        }
    }

    // Layout placed to the left in the action bar
    private class LeftActionBarLayout extends LinearLayout {

        public LeftActionBarLayout(Context context) {
            super(context);
        }

        public LeftActionBarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LeftActionBarLayout(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public void addWimsButton(final WimsButton wimsButton) {
            final LinearLayout.LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            buttonParams.leftMargin = (int) WimsScalingUtilities.convertDpToPixel(getContext(), ACTION_BAR_SPACING_LEFT_RIGHT);
            this.addView(wimsButton, buttonParams);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch the action bar
        actionBar = this.getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(createActionBarView());
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    // Method to add a button to the action bar
    public void addWimsButtonToActionBar(WimsButton wimsButton, int side) {

        if (side == LEFT) {
            actionBarCustomViewLeft.addWimsButton(wimsButton);
        } else if (side == RIGHT) {
            actionBarCustomViewRight.addWimsButton(wimsButton);
        } else {
            throw new IllegalArgumentException("You have to give LEFT or RIGHT side when adding a button to the actionbar");
        }
    }

    // Method to set the action bar title
    public void setActionBarTitle(String title) {
        actionBarTitleView.setText(title);
    }

    // Creates the view of the action bar
    private View createActionBarView() {

        actionBarCustomView = new RelativeLayout(this);

        actionBarCustomView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final int paddingLeftRight = (int) WimsScalingUtilities.convertDpToPixel(this, ACTION_BAR_SPACING_LEFT_RIGHT);
        final int paddingTopBottom = (int) WimsScalingUtilities.convertDpToPixel(this, ACTION_BAR_SPACING_TOP_BOTTOM);

        // Added double padding to the left.
        actionBarCustomView.setPadding(paddingLeftRight + paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);

        actionBarCustomView.setBackgroundResource(R.color.colorPrimary);
        actionBarCustomView.setId(R.id.wims_action_bar_primary_view);

        // Creates the back button which have to be there.
        final WimsButton backButton = new WimsButton(this, this.getResources().getDrawable(R.drawable.back_icon));

        backButton.setId(R.id.wims_action_bar_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WimsActivity.this.onBackPressed();

            }
        });


        final RelativeLayout.LayoutParams backButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        backButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        actionBarCustomView.addView(backButton, backButtonParams);

        actionBarTitleView = new TextView(this);

        // To set default title of the action bar
        String title;
        if (this.getTitle() != null) {
            title = this.getTitle().toString();
        } else if (this.getString(this.getApplicationInfo().labelRes) != null) {
            title = this.getString(this.getApplicationInfo().labelRes);
        } else {
            title = "Unknown title";
        }

        // Set params for custom title view.
        actionBarTitleView.setText(this.getTitle());
        actionBarTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, calculateActionBarTextSize());
        actionBarTitleView.setGravity(Gravity.CENTER);
        actionBarTitleView.setTextColor(getResources().getColor(R.color.White));
        actionBarTitleView.setSingleLine(true);
        actionBarTitleView.setEllipsize(TextUtils.TruncateAt.END);


        actionBarTitleView.setIncludeFontPadding(false);

        final RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        actionBarCustomView.addView(actionBarTitleView, titleParams);

        actionBarCustomViewLeft = new LeftActionBarLayout(this);
        RelativeLayout.LayoutParams leftActionBarLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        leftActionBarLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.wims_action_bar_back_button);
        actionBarCustomView.addView(actionBarCustomViewLeft, leftActionBarLayoutParams);

        actionBarCustomViewRight = new RightActionBarLayout(this);
        RelativeLayout.LayoutParams rightActionBarLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rightActionBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        actionBarCustomView.addView(actionBarCustomViewRight, rightActionBarLayoutParams);

        return actionBarCustomView;
    }

    private int calculateActionBarTextSize() {

        int actionBarHeight = 0;

        final TypedValue tv = new TypedValue();

        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        return (int) (actionBarHeight - WimsScalingUtilities.convertDpToPixel(this, ACTION_BAR_TEXT_PADDING));
    }
}
