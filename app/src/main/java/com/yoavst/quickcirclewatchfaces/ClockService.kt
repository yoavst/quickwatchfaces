package com.yoavst.quickcirclewatchfaces

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.view.View
import android.widget.RemoteViews
import com.chibatching.kotpref.Kotpref
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by yoavst.
 */
public class ClockService : Service() {
    val MSG_UPDATE_TIME = 0
    private var hourHand: BitmapDrawable? = null
    private var minuteHand: BitmapDrawable? = null
    private var secondHand: BitmapDrawable? = null
    private var background: Bitmap? = null
    private var hour_degree: Float = 0.toFloat()
    private var min_degree: Float = 0.toFloat()
    private var second_degree: Float = 0.toFloat()
    private var clock: Clock? = null
    private var isRunning = false
    private val bitmap = Bitmap.createBitmap(1110, 1110, Bitmap.Config.ARGB_4444)
    private val canvas = Canvas(bitmap)

    // timer for the second hand
    val mUpdateTimeHandler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            when (message.what) {
                MSG_UPDATE_TIME -> {
                    draw(this@ClockService) // draw clock in every second
                    if (isRunning) {
                        val timeMs = System.currentTimeMillis()
                        val delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS)
                        mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // get extras
        Kotpref.init(getApplicationContext())
        clock = Prefs.activeClock
        if (clock != null) {
            isRunning = true
            updateTimer()
            // register screen status receiver
            setScreenOnOffReceiver(true)
        }
        setClockChangedReceiver(true)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        // unregister screen status receiver
        setScreenOnOffReceiver(false)
        setClockChangedReceiver(false)
    }

    private fun draw(context: Context): RemoteViews {
        val views = RemoteViews(context.getPackageName(), R.layout.clock_widget)

        // get drawables and set day & date
        if (!setDefaultClock(views))
            return views // do not draw hands if getting drawables failed

        computeTimeDegree()

        // create a paint object for drawing
        val p = Paint()
        p.setAntiAlias(true)
        p.setStyle(Paint.Style.STROKE)
        p.setStrokeWidth(8f)

        views.setImageViewBitmap(R.id.dial, background)

        // create a bitmap for canvas to draw
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // hour
        canvas.save()
        canvas.rotate(hour_degree, 555f, 555f)
        hourHand!!.setBounds(501, 0, 609, 1110)
        hourHand!!.draw(canvas)
        canvas.restore()

        // minute
        canvas.save()
        canvas.rotate(min_degree, 555f, 555f)
        minuteHand!!.setBounds(507, 0, 603, 1110)
        minuteHand!!.draw(canvas)
        canvas.restore()

        // second
        canvas.save()
        canvas.rotate(second_degree, 555f, 555f)
        secondHand!!.setBounds(507, 0, 603, 1110)
        secondHand!!.draw(canvas)
        canvas.restore()

        views.setImageViewBitmap(R.id.hands, bitmap)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val me = ComponentName(context, javaClass<ClockLoader>())
        appWidgetManager.updateAppWidget(me, views)
        return views
    }

    private fun updateTimer() {
        mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
        mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
    }

    private fun computeTimeDegree() {
        val calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        second_degree = second / 60.0f * 360.0f
        min_degree = (minute + second / 60.0f) / 60.0f * 360.0f
        hour_degree = (hour + (minute / 60.0f)) / 12.0f * 360.0f

    }

    private fun setDefaultClock(views: RemoteViews): Boolean {
        var result = false
        val files = clock!!.files
        // get drawables for hands and draw background
        try {
            if (hourHand == null) {
                // hour
                hourHand = BitmapDrawable(getResources(), "/data/data/" + "com.yoavst.quickcirclewatchfaces" + "/files/" + clock!!.id + "/" + files[0])
                hourHand!!.setTargetDensity(640)
            }
            if (minuteHand == null) {
                // minute
                minuteHand = BitmapDrawable(getResources(), "/data/data/" + "com.yoavst.quickcirclewatchfaces" + "/files/" + clock!!.id + "/" + files[1])
                minuteHand!!.setTargetDensity(640)
            }
            if (secondHand == null) {
                // second
                secondHand = BitmapDrawable(getResources(), "/data/data/" + "com.yoavst.quickcirclewatchfaces" + "/files/" + clock!!.id + "/" + files[2])
                secondHand!!.setTargetDensity(640)
            }

            //background
            if (background == null)
                background = BitmapDrawable(getResources(), "/data/data/" + "com.yoavst.quickcirclewatchfaces" + "/files/" + clock!!.id + "/" + files[3]).getBitmap()
            result = true
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }


        // set day & date
        if (!clock!!.hideHourText && result) {
            // views.setViewPadding(R.id.additional_text, mXPos, mYPos, 0, 0);
            val calendar = Calendar.getInstance()

            views.setTextViewText(R.id.additional_text, calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH) + " " + calendar.get(Calendar.DATE))

            views.setTextColor(R.id.additional_text, clock!!.dayTextColor)
            views.setInt(R.id.additional_text, "setBackgroundColor", clock!!.dayBackgroundColor)
            views.setViewVisibility(R.id.additional_text, View.VISIBLE)
        } else views.setViewVisibility(R.id.additional_text, View.GONE)
        return result
    }

    private fun setScreenOnOffReceiver(on: Boolean) {
        // create a receiver for handling screen on/off

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)

        if (on)
            this.getApplicationContext().registerReceiver(screenOffReceiver, filter)
        else
            try {
                this.getApplicationContext().unregisterReceiver(screenOffReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
    }

    val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_SCREEN_OFF == intent.getAction()) {
                // stop updating when screen is off
                isRunning = false
                // Log.d("test", "ACTION_SCREEN_OFF");
            } else if (Intent.ACTION_SCREEN_ON == intent.getAction()) {
                // start updating when screen is on
                isRunning = true
                updateTimer()
                // Log.d("test", "ACTION_SCREEN_ON");
            }
        }
    }

    private fun setClockChangedReceiver(on: Boolean) {
        // create a receiver for handling screen on/off

        val filter = IntentFilter(BROADCAST_CLOCK_CHANGED)
        if (on)
            this.getApplicationContext().registerReceiver(clockChangeReceiver, filter)
        else
            try {
                this.getApplicationContext().unregisterReceiver(clockChangeReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
    }


    val clockChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            hourHand = null
            minuteHand = null
            secondHand = null
            background = null
            if (clock != null) {
                clock = Prefs.activeClock
                updateTimer()
            } else clock = Prefs.activeClock
        }
    }

    companion object {
        private val INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1)
        public val BROADCAST_CLOCK_CHANGED: String = "broadcast_clock_changed"
    }
}