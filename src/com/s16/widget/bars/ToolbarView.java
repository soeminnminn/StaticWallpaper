package com.s16.widget.bars;

import java.util.ArrayList;
import java.util.List;

import com.s16.staticwallpaper.BuildConfig;
import com.s16.staticwallpaper.R;
import com.s16.widget.popupmenu.PopupMenuCompat;
import com.s16.widget.popupmenu.internal.MenuHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ToolbarView extends LinearLayout 
		implements PopupMenuCompat.OnMenuItemClickListener, MenuHelper.OnMenuChangedListener {

	protected static final String TAG = ToolbarView.class.getSimpleName();
	protected static final boolean DEBUG = BuildConfig.DEBUG;
	protected static final int OVERFLOW_ID = 0x1024;
	
	public static final int ICON_COLOR_DARK = 0x333333;
	public static final int ICON_COLOR_LIGHT = 0xffffff;
	
	// protected Drawable mItemBackground;
	protected int mMoreIconColor;
	protected boolean mAllowTextWithIcon;
	private boolean mItemCreated;
	
	/**
     * Listener for item click
     */
    public static interface OnToolbarItemClickListener {
        public boolean onToolbarItemClick(MenuItem item);
    }
    
    private View.OnClickListener mActionClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!v.isEnabled()) return;
			
			int id = v.getId();
			if (id == OVERFLOW_ID) {
				showOverFlow(v);
			} else {
				MenuItem item = mMenu.findItem(id);
				if (item != null) {
					onMenuItemClick(item);
				}
			}
		}
	};
	
	private class OverFlowIconDrawable extends Drawable {

		private final Paint mPaint;
		
		public OverFlowIconDrawable(int color) {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(color);
			mPaint.setAlpha(154);
			mPaint.setAntiAlias(true);
		}
		
		@Override
		public void draw(Canvas canvas) {
 			int rectSize = (int)Math.ceil(getBounds().height() / 14);
 			int x = (int)Math.ceil((getBounds().width() / 2) - rectSize);
 			final Paint paint = mPaint;
 			
 			int y = rectSize * 3;
 			for (int i = 0; i < 3; i++) {
 				Rect rect = new Rect();
 				rect.left = x;
 				rect.right = x + (rectSize * 3);
 				rect.top = y;
 				rect.bottom = rect.top + (rectSize * 3);
 				
 				canvas.drawRect(rect, paint);
 				y += (rectSize * 4);
 			}
		}
		
		public void setColor(int color) {
			mPaint.setColor(color);
			invalidateSelf();
		}

		@Override
		public void setAlpha(int alpha) {
			mPaint.setAlpha(alpha);
			invalidateSelf();
		}

		@Override
		public void setColorFilter(ColorFilter filter) {
			mPaint.setColorFilter(filter);
			invalidateSelf();
		}

		@Override
		public int getOpacity() {
			return mPaint.getAlpha() < 255 ? android.graphics.PixelFormat.TRANSLUCENT : android.graphics.PixelFormat.OPAQUE;
		}
	}
	
    private Menu mMenu;
    private Menu mOverflowMenu; 
    private PopupMenuCompat mPopupMenu;
    private OverFlowIconDrawable mOverFlowIcon;
    private OnToolbarItemClickListener mItemClickListener;
	
    public ToolbarView(Context context) {
		this(context, null);
	}
    
	public ToolbarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ToolbarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, attrs, defStyle);
	}
	
	private void initialize(Context context, AttributeSet attrs, int defStyle) {
		if (isInEditMode()) {
			return;
		}
		
		// mItemBackground = getResources().getDrawable(R.drawable.action_button);
		mMoreIconColor = ICON_COLOR_LIGHT;
		mAllowTextWithIcon = false;
		
		if (attrs != null) {
	        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToolbarView, defStyle, R.style.tbvToolbarStyle);
	        //mItemBackground = a.getDrawable(R.styleable.ToolbarView_tbvItemBackground);
	        mAllowTextWithIcon = a.getBoolean(R.styleable.ToolbarView_tbvAllowTextWithIcon, false);
	        
	        int iconTheme = a.getInteger(R.styleable.ToolbarView_tbvIconTheme, 0);
	        if (iconTheme > 0) {
	        	mMoreIconColor = (iconTheme == 1) ?  ICON_COLOR_DARK : ICON_COLOR_LIGHT;
	        } else {
	        	mMoreIconColor = a.getColor(R.styleable.ToolbarView_tbvMoreIconColor, ICON_COLOR_LIGHT);	
	        }
	        a.recycle();
		}
		
		mMenu = MenuHelper.createMenu(context, this);
		mOverflowMenu = MenuHelper.createMenu(context);
		mOverFlowIcon = new OverFlowIconDrawable(mMoreIconColor);
	}
	
	protected List<MenuItem> getVisibleItems() {
		List<MenuItem> list = new ArrayList<MenuItem>();
		if (mMenu.hasVisibleItems()) {
			for(int i = 0; i < mMenu.size(); i++) {
				MenuItem item = mMenu.getItem(i);
				if (item.isVisible()) list.add(item);
			}
		}
		return list;
	}
	
	protected void showOverFlow(View view) {
		if (mOverflowMenu.size() > 0) {			
			mPopupMenu = new PopupMenuCompat(getContext(), view);
			mPopupMenu.setMenu(mOverflowMenu);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();
		}
	}
	
	protected void createItems(int width, int height) {
		if (mItemCreated) return;
		mItemCreated = true;
		
		if (width == 0 || height == 0) return;
		final List<MenuItem> list = getVisibleItems();
		final int numItems = list.size();
		removeAllViews();
		
		if (numItems > 0) {
			int itemCount = numItems;
			
			if (width > height) {
				setLayoutDirection(LinearLayout.HORIZONTAL);
				itemCount = (int)Math.floor(width / height);
			} else {
				setLayoutDirection(LinearLayout.VERTICAL);
				itemCount = (int)Math.floor(height / width);
			}
			
			boolean hasOverflow = (itemCount < numItems);
			int actCount = Math.min(numItems, itemCount);
			setWeightSum(actCount);
			if (DEBUG) {
	            Log.v(TAG, "itemCount= " + actCount);
	        }
			
			if (hasOverflow) {
				if (mPopupMenu == null || !mPopupMenu.isShowing()) {
					mOverflowMenu.clear();
					for(int i = itemCount - 1; i < numItems; i++) {
						MenuItem item = list.get(i);
						if (item.isVisible()) {
							MenuItem itemNew = mOverflowMenu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle());
							itemNew.setIcon(item.getIcon());
						}
					}
				}
			}
			
			int itemMinWidth = (width > height) ? (width / actCount) : width;
			LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			viewParams.weight = 1;
			
			for(int i = 0; i < actCount; i++) {
				View actionItem = null;
				if (i == (actCount - 1) && hasOverflow) {
					ImageButton button = new ImageButton(getContext());
					button.setId(OVERFLOW_ID);
					button.setImageDrawable(mOverFlowIcon);
					actionItem = button;
				} else {
					ToolbarItemView button = new ToolbarItemView(getContext());
					MenuItem item = list.get(i);
					button.setMenuItem(item);
					button.setMinWidth(itemMinWidth);
					actionItem = button; 
				}
				
				actionItem.setLayoutParams(viewParams);
				//actionItem.setBackground(mItemBackground);
				actionItem.setBackgroundResource(R.drawable.action_button);
				actionItem.setOnClickListener(mActionClickListener);
				addView(actionItem);
			}
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}
	
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (!isInEditMode()) {
			createItems(getMeasuredWidth(), getMeasuredHeight());
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public MenuItem getItemById(int id) {
		if (mMenu.size() == 0) return null;
		for(int i = 0; i < mMenu.size(); i++) {
			if (mMenu.getItem(i).getItemId() == id) {
				return mMenu.getItem(i); 
			}
		}
		return null;
	}
	
	public int findIndexById(int id) {
		if (mMenu.size() == 0) return -1;
		for(int i = 0; i < mMenu.size(); i++) {
			if (mMenu.getItem(i).getItemId() == id) {
				return i; 
			}
		}
		return -1;
	}
	
	public void setOverFlowIconColor(int color) {
		mOverFlowIcon.setColor(color);
	}

	public Menu getMenu() {
        return mMenu;
    }
	
	public void setMenu(final Menu menu) {
        if (DEBUG) {
            Log.v(TAG, "setMenu()");
        }
        mMenu = menu;
    }
	
	public View getItemView(MenuItem item) {
		if (item == null) return null;
		if (mMenu.size() == 0) return null;
		return findViewById(item.getItemId());
	}

	protected MenuInflater getMenuInflater() {
        return new MenuInflater(getContext());
    }

    public void inflate(final int menuRes) {
        if (DEBUG) {
            Log.v(TAG, "inflate() menuRes=" + menuRes);
        }
        getMenuInflater().inflate(menuRes, mMenu);
    }
    
    public void setOnToolbarItemClickListener(final OnToolbarItemClickListener listener) {
        mItemClickListener = listener;
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		onToolbarItemClick(item);
		return false;
	}
	
	protected void onToolbarItemClick(MenuItem item) {
		if (mItemClickListener != null) {
			mItemClickListener.onToolbarItemClick(item);
		}
		
		if (getContext() instanceof Activity) {
			Activity activity = (Activity)getContext();
			activity.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMenuChanged(Menu menu) {
	}

	@Override
	public void onMenuItemChanged(MenuItem menuItem) {
		View view = getItemView(menuItem);
		if (view != null && view instanceof ToolbarItemView) {
			((ToolbarItemView)view).setMenuItem(menuItem);
		}
	}
}
