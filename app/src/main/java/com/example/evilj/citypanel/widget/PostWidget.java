package com.example.evilj.citypanel.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.example.evilj.citypanel.Models.Post;
import com.example.evilj.citypanel.R;

/**
 * Implementation of App Widget functionality.
 */
public class PostWidget extends AppWidgetProvider {
    public static Post sPost;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.post_widget);
        if (sPost == null) {
            views.setTextViewText(R.id.widget_post, context.getString(R.string.no_post));
            views.setViewVisibility(R.id.widget_name, View.GONE);
        } else {
            views.setTextViewText(R.id.widget_post, sPost.getMessage());
            views.setViewVisibility(R.id.widget_name, View.VISIBLE);
            views.setTextViewText(R.id.widget_name, sPost.getCreadorName());
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        WidgetPostService.startActionPost(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateWidget(Post post, AppWidgetManager appWidgetManager, int[] appWidgetsId, Context context) {
        sPost = post;
        for (int appWidgetId : appWidgetsId) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}

