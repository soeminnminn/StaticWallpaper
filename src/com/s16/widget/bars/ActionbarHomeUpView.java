package com.s16.widget.bars;

import com.s16.staticwallpaper.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ActionbarHomeUpView extends RelativeLayout implements View.OnClickListener {

	private ImageView mBackIndicator;
	private ImageView mLogoView;
	
	public static interface OnHomeUpClickListener {
        public boolean onHomeUpClick(View v);
    }
	private OnHomeUpClickListener mOnHomeUpClickListener;
	
	public ActionbarHomeUpView(Context context) {
		super(context);
		initialize(context);
	}
	
	public ActionbarHomeUpView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}
	
	public ActionbarHomeUpView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}
	
	private void initialize(Context context) {
		if (isInEditMode()) {
			return;
		}
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View parent = inflater.inflate(R.layout.action_bar_home, this, true);
        parent.setOnClickListener(this);
        mBackIndicator = (ImageView)parent.findViewById(R.id.up);
        mLogoView = (ImageView)parent.findViewById(R.id.home);
        
        ApplicationInfo applicationInfo = context.getApplicationInfo();
		mLogoView.setImageResource(applicationInfo.icon);
	}

	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void onClick(View v) {
		if (mOnHomeUpClickListener != null) {
			mOnHomeUpClickListener.onHomeUpClick(v);
		}
	}
	
	/* Emulating Honeycomb, setdisplayHomeAsUpEnabled takes a boolean
     * and toggles whether the "home" view should have a little triangle
     * indicating "up" */
    public void setDisplayHomeAsUpEnabled(boolean show) {
        mBackIndicator.setVisibility(show? View.VISIBLE : View.GONE);
    }
    
    /**
     * Shows the provided logo to the left in the action bar.
     * 
     * This is meant to be used instead of the setHomeAction and does not draw
     * a divider to the left of the provided logo.
     * 
     * @param resId The drawable resource id
     */
    public void setHomeLogo(int resId) {
        mLogoView.setImageResource(resId);
    }
    
    public void setOnHomeUpClickListener(OnHomeUpClickListener listener) {
    	mOnHomeUpClickListener = listener;
    }
}
