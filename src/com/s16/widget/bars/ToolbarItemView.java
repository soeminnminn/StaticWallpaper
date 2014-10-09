package com.s16.widget.bars;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

public class ToolbarItemView extends TextView implements View.OnLongClickListener {
	
	private MenuItem mItemData;
    private CharSequence mTitle;
    private Drawable mIcon;

    private boolean mAllowTextWithIcon;
    private int mMinWidth;
    private int mSavedPaddingLeft;
	
	public ToolbarItemView(Context context) {
        this(context, null);
    }

    public ToolbarItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolbarItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setOnLongClickListener(this);
        setClickable(true);
        
        mSavedPaddingLeft = -1;
    }
    
    public void setMenuItem(MenuItem itemData) {
        mItemData = itemData;

        setIcon(itemData.getIcon());
        setTitle(itemData.getTitle()); // Title only takes effect if there is no icon
        setId(itemData.getItemId());

        setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
        setEnabled(itemData.isEnabled());
    }
    
    private void updateTextButtonVisibility() {
        boolean visible = !TextUtils.isEmpty(mTitle);
        visible &= mIcon == null || mAllowTextWithIcon;

        setText(visible ? mTitle : null);
    }
    
    @Override
    public void setMinWidth(int minpixels) {
    	mMinWidth = minpixels;
    	super.setMinWidth(minpixels);
    }
    
    @Override
    public void setPadding(int l, int t, int r, int b) {
        mSavedPaddingLeft = l;
        super.setPadding(l, t, r, b);
    }
    
    public void setAllowTextWithIcon(boolean allowTextWithIcon) {
    	mAllowTextWithIcon = allowTextWithIcon;
    	updateTextButtonVisibility();
    	invalidate();
    }
    
    public void setIcon(Drawable icon) {
        mIcon = icon;
        setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        updateTextButtonVisibility();
        invalidate();
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(getText());
    }
    
    public void setTitle(CharSequence title) {
        mTitle = title;

        setContentDescription(mTitle);
        updateTextButtonVisibility();
        invalidate();
    }
    
    public boolean needsDividerBefore() {
        return hasText() && mItemData.getIcon() == null;
    }

    public boolean needsDividerAfter() {
        return hasText();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	final boolean textVisible = hasText();
        if (textVisible && mSavedPaddingLeft >= 0) {
            super.setPadding(mSavedPaddingLeft, getPaddingTop(),
                    getPaddingRight(), getPaddingBottom());
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int oldMeasuredWidth = getMeasuredWidth();
        final int targetWidth = widthMode == MeasureSpec.AT_MOST ? Math.min(widthSize, mMinWidth) : mMinWidth;

        if (widthMode != MeasureSpec.EXACTLY && mMinWidth > 0 && oldMeasuredWidth < targetWidth) {
            // Remeasure at exactly the minimum width.
            super.onMeasure(MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
        }

        if (!textVisible && mIcon != null) {
            // TextView won't center compound drawables in both dimensions without
            // a little coercion. Pad in to center the icon after we've measured.
            final int w = getMeasuredWidth();
            final int dw = mIcon.getIntrinsicWidth();
            super.setPadding((w - dw) / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

	@Override
	public boolean onLongClick(View v) {
		if (hasText()) {
            // Don't show the cheat sheet for items that already show text.
            return false;
        }

        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        getLocationOnScreen(screenPos);
        getWindowVisibleDisplayFrame(displayFrame);

        final Context context = getContext();
        final int width = getWidth();
        final int height = getHeight();
        final int midy = screenPos[1] + height / 2;
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        Toast cheatSheet = Toast.makeText(context, mItemData.getTitle(), Toast.LENGTH_SHORT);
        if (midy < displayFrame.height()) {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT,
                    screenWidth - screenPos[0] - width / 2, height);
        } else {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
        }
        cheatSheet.show();
        return true;
	}
}
