package com.yoavst.quickcirclewatchfaces

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.chibatching.kotpref.Kotpref
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by yoavst.
 */
public class ClockService : Service() {
    val MSG_UPDATE_TIME = 0
    private var hourHand: Drawable? = null
    private var minuteHand: Drawable? = null
    private var secondHand: Drawable? = null
    private var background: Bitmap? = null
    private var dateFormatter: SimpleDateFormat? = null
    private var hour_degree: Float = 0.toFloat()
    private var min_degree: Float = 0.toFloat()
    private var second_degree: Float = 0.toFloat()
    private var clock: Clock? = null
    private var isRunning = false
    private val bitmap = Bitmap.createBitmap(1110, 1110, Bitmap.Config.ARGB_4444)
    private val canvas = Canvas(bitmap)
    private var shouldUpdateEverySecond: Boolean = true

    // timer for the second hand
    val mUpdateTimeHandler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            when (message.what) {
                MSG_UPDATE_TIME -> {
                    draw(this@ClockService) // draw clock in every second
                    if (isRunning) {
                        val rate = if (shouldUpdateEverySecond) UPDATE_RATE_SECOND else UPDATE_RATE_MINUTE
                        val timeMs = System.currentTimeMillis()
                        val delayMs = rate - (timeMs % rate)
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
        isRunning = true
        updateTimer()
        // register screen status receiver
        setScreenOnOffReceiver(true)
        setClockChangedReceiver(true)
        setQuickCircleStateReceiver(true)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        // unregister screen status receiver
        setScreenOnOffReceiver(false)
        setClockChangedReceiver(false)
        setQuickCircleStateReceiver(false)
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
        if (shouldUpdateEverySecond) {
            canvas.save()
            canvas.rotate(second_degree, 555f, 555f)
            secondHand!!.setBounds(507, 0, 603, 1110)
            secondHand!!.draw(canvas)
            canvas.restore()
        }

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

    private fun findFile(name: String): String? {
        val files = clock!!.files
        if ("b2_quickcircle_analog_style01_" + name + ".png" in files) {
            return "b2_quickcircle_analog_style01_" + name + ".png"
        } else if ("b2_quickcircle_analog_style02_" + name + ".png" in files) {
            return "b2_quickcircle_analog_style02_" + name + ".png"
        } else if ("b2_quickcircle_analog_style03_" + name + ".png" in files) {
            return "b2_quickcircle_analog_style03_" + name + ".png"
        } else if (name + ".png" in files) {
            return name + ".png"
        }
        return null
    }

    private fun setDefaultClock(views: RemoteViews): Boolean {
        try {
            if (clock != null) {
                val id = clock!!.id
                val packageName = getPackageName()
                if (hourHand == null) {
                    hourHand = BitmapDrawable(getResources(), "/data/data/$packageName/files/$id/${findFile("hour")}")
                }
                if (minuteHand == null) {
                    minuteHand = BitmapDrawable(getResources(), "/data/data/$packageName/files/$id/${findFile("minute")}")
                }
                if (secondHand == null) {
                    secondHand = BitmapDrawable(getResources(), "/data/data/$packageName/files/$id/${findFile("second")}")
                }
                if (background == null)
                    background = BitmapFactory.decodeFile("/data/data/" + "com.yoavst.quickcirclewatchfaces" + "/files/" + id + "/" + (findFile("bg") ?: findFile("background")))
                if (dateFormatter == null) {
                    dateFormatter = SimpleDateFormat(clock!!.dateFormat)
                }
                // set day & date
                if (!clock!!.hideDateText && !Prefs.forceHideDate) {
                    var gravity = Prefs.forceDateGravity
                    if (gravity.isEmpty()) gravity = clock!!.dateGravity
                    val textViewId = getId(gravity)
                    hideAllTextViews(views)
                    views.setTextViewText(textViewId, dateFormatter!!.format(Date()))
                    views.setTextColor(textViewId, clock!!.dateTextColor)
                    views.setTextViewTextSize(textViewId, TypedValue.COMPLEX_UNIT_SP, clock!!.dateTextSize)
                    views.setInt(textViewId, "setBackgroundColor", clock!!.dateBackgroundColor)
                    views.setViewVisibility(textViewId, View.VISIBLE)
                } else hideAllTextViews(views)
            } else {
                // set default clock
                hourHand = getDrawable(R.drawable.hour)
                minuteHand = getDrawable(R.drawable.minute)
                secondHand = getDrawable(R.drawable.second)
                background = (getDrawable(R.drawable.background) as BitmapDrawable).getBitmap()
                hideAllTextViews(views)
                views.setTextColor(R.id.additional_text_bottom, Color.BLACK)
                views.setInt(R.id.additional_text_bottom, "setBackgroundColor", Color.TRANSPARENT)
                val calendar = Calendar.getInstance()
                views.setTextViewText(R.id.additional_text_bottom, calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH) + " " + calendar.get(Calendar.DATE))
                views.setViewVisibility(R.id.additional_text_bottom, View.VISIBLE)
            }
            shouldUpdateEverySecond = secondHand == null || !Prefs.forceMinute
            return true
        } catch(e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun hideAllTextViews(views: RemoteViews) {
        views.setViewVisibility(R.id.additional_text_right, View.GONE)
        views.setViewVisibility(R.id.additional_text_left, View.GONE)
        views.setViewVisibility(R.id.additional_text_top, View.GONE)
        views.setViewVisibility(R.id.additional_text_bottom, View.GONE)
    }

    fun getId(gravity: String): Int {
        return when (gravity) {
            "left" -> R.id.additional_text_left
            "top" -> R.id.additional_text_top
            "bottom" -> R.id.additional_text_bottom
            else -> R.id.additional_text_right
        }
    }

    private fun setScreenOnOffReceiver(on: Boolean) {
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
            } else if (Intent.ACTION_SCREEN_ON == intent.getAction()) {
                // start updating when screen is on
                isRunning = true
                updateTimer()
            }
        }
    }

    private fun setClockChangedReceiver(on: Boolean) {
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
            if (clock != null || shouldUpdateEverySecond != !Prefs.forceMinute) {
                clock = Prefs.activeClock
                updateTimer()
            } else clock = Prefs.activeClock
        }
    }

    private fun setQuickCircleStateReceiver(on: Boolean) {
        val filter = IntentFilter(ACTION_ACCESSORY_COVER_EVENT)
        if (on)
            this.getApplicationContext().registerReceiver(quickCircleStateReceiver, filter)
        else
            try {
                this.getApplicationContext().unregisterReceiver(quickCircleStateReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
    }


    val quickCircleStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val quickCoverState = intent?.getIntExtra(EXTRA_ACCESSORY_COVER_STATE,
                    EXTRA_ACCESSORY_COVER_OPENED) ?: EXTRA_ACCESSORY_COVER_OPENED
            if (quickCoverState == EXTRA_ACCESSORY_COVER_CLOSED) {
                // Let's do call stuff!
                isRunning = true
                updateTimer()
            } else {
                // stop doing cool stuff.
                isRunning = false
            }
        }
    }

    companion object {
        private val UPDATE_RATE_SECOND = TimeUnit.SECONDS.toMillis(1)
        private val UPDATE_RATE_MINUTE = TimeUnit.MINUTES.toMillis(1)

        public val BROADCAST_CLOCK_CHANGED: String = "broadcast_clock_changed"

        private val EXTRA_ACCESSORY_COVER_STATE = "com.lge.intent.extra.ACCESSORY_COVER_STATE"
        private val ACTION_ACCESSORY_COVER_EVENT = "com.lge.android.intent.action.ACCESSORY_COVER_EVENT"
        private val EXTRA_ACCESSORY_COVER_OPENED = 0
        private val EXTRA_ACCESSORY_COVER_CLOSED = 1
    }
}