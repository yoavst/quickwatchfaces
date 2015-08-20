package com.yoavst.quickcirclewatchfaces

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import java.util.concurrent.TimeUnit

/**
 * Created by yoavst.
 */
public class ClockLoader: AppWidgetProvider() {
    var hasEnabled = false
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // initialize data when the first widget is added
        initialize(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // stop update service
        startService(context, false)
    }

    private fun initialize(context: Context) {
        // start update service
        startService(context, true)
        hasEnabled = true
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (!hasEnabled)
            initialize(context)
        hasEnabled = true
    }

    private fun startService(context: Context, start: Boolean) {
        val intent = Intent(context, javaClass<ClockService>())
        if (start)
            context.startService(intent)
        else
            context.stopService(intent)
    }
}