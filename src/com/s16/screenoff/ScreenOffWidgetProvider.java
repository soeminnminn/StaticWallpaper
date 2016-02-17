package com.s16.screenoff;

import com.s16.staticwallpaper.R;
import com.s16.staticwallpaper.activity.ScreenOffActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ScreenOffWidgetProvider extends AppWidgetProvider {
	protected static final String TAG = ScreenOffWidgetProvider.class.getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		if (appWidgetIds != null) {
			for (int i = 0; i < appWidgetIds.length; i++) {
				int appWidgetId = appWidgetIds[i];
		        
		        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_screen_off);
		        
		        // Register an onClickListener
			    Intent intent = new Intent(context, ScreenOffActivity.class);
			    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			    remoteViews.setOnClickPendingIntent(R.id.imageScreenOff, pendingIntent);
				
			    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		}
	}
}
