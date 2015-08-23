package com.yoavst.quickcirclewatchfaces

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.chibatching.kotpref.Kotpref
import com.google.android.gms.ads.AdRequest
import com.yoavst.kotlin.*
import kotlinx.android.synthetic.clock_activity.*
import java.io.*
import java.util.ArrayList
import java.util.HashSet
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.properties.Delegates

public class ClockActivity : AppCompatActivity() {
    var clocks: MutableList<Clock> by Delegates.notNull()
    var activeClock: Clock? = null
    var currentClock: Clock? = null
    var dialog: ProgressDialog? = null
    val gravities by stringArrayResource(R.array.gravities)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clock_activity)
        toolbar.setTitle("")
        toolbar.setBackgroundColor(Color.parseColor("#607D8B"))
        setSupportActionBar(toolbar)
        pager.setAdapter(ClockAdapter(this, emptyList()))
        indicator.setViewPager(pager)
        Kotpref.init(getApplicationContext())
        indicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                showPageData(position)
            }

        })
        initUI()
        toolbar.setNavigationOnClickListener {
            drawer.openDrawer(Gravity.START)
        }
        toolbar.setOnMenuItemClickListener {
            when (it.getItemId()) {
                R.id.action_delete -> {
                    if (currentClock != null) {
                        val path = getFilesDir().path + "/" + currentClock!!.id + "/"
                        val file = File(path)
                        if (file.exists()) {
                            val deleteCmd = "rm -r " + path
                            val runtime = Runtime.getRuntime()
                            try {
                                runtime.exec(deleteCmd)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        val newSet = HashSet(Prefs.clocks)
                        newSet.remove(currentClock.toString())
                        Prefs.clocks = newSet
                        if (currentClock?.id == activeClock?.id) {
                            activeClock = null
                            Prefs.activeClock = null
                        }
                        currentClock = null
                        initUI()
                    }
                    true
                }
                R.id.action_import -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/zip")
                    startActivityForResult(intent, IMPORT_FILE_REQUEST_CODE)
                    true
                }
                R.id.action_download -> {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://qcthemer.net/theme-gallery")))
                    true
                }
                R.id.action_credit -> {
                    val textView = TextView(this)
                    val padding = 8.toPx(this)
                    textView.setPadding(padding, padding, padding, padding)
                    val s = Html.fromHtml(getString(R.string.credit_text))
                    textView.setText(s)
                    textView.setMovementMethod(LinkMovementMethod.getInstance())
                    AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                            .setTitle(R.string.credits)
                            .setView(textView)
                            .setPositiveButton(android.R.string.ok) { a, b -> }
                            .show()

                    true
                }
                else -> false
            }
        }
        apply.setOnClickListener {
            activeClock = currentClock
            Prefs.activeClock = activeClock
            applied.show()
            apply.hide()
            sendBroadcast(Intent(ClockService.BROADCAST_CLOCK_CHANGED))
        }
        secondsSwitch.setChecked(!Prefs.forceMinute)
        secondsSwitch.setOnCheckedChangeListener { compoundButton, b ->
            Prefs.forceMinute = !b
            sendBroadcast(Intent(ClockService.BROADCAST_CLOCK_CHANGED))
        }
        dateSwitch.setChecked(!Prefs.forceHideDate)
        dateSwitch.setOnCheckedChangeListener { compoundButton, b ->
            Prefs.forceHideDate = !b
            sendBroadcast(Intent(ClockService.BROADCAST_CLOCK_CHANGED))
        }
        val forced = Prefs.forceDateGravity
        when (forced) {
            "right" -> gravitySpinner.setSelection(1)
            "left" -> gravitySpinner.setSelection(2)
            "top" -> gravitySpinner.setSelection(3)
            "bottom" -> gravitySpinner.setSelection(4)
            else -> gravitySpinner.setSelection(0)
        }
        gravitySpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) Prefs.forceDateGravity = ""
                else Prefs.forceDateGravity = gravities[position]
            }

        })

        val adRequest = AdRequest.Builder().addKeyword("LG G4, Quick Circle, LG, Android").build()
        adView.loadAd(adRequest)
    }

    fun showPageData(position: Int) {
        currentClock = clocks[position]
        val temp = currentClock!!
        title.setText(temp.title)
        author.setText(temp.author)
        description.setText(temp.description)
        if (temp.id == activeClock!!.id) {
            applied.show()
            apply.hide()
        } else {
            applied.hide()
            apply.show()
        }

    }

    fun initUI() {
        clocks = ArrayList(Prefs.clocks map { Clock.parse(it) })
        activeClock = Prefs.activeClock
        if (activeClock == null && clocks.size() > 0) {
            activeClock = clocks[0]
            Prefs.activeClock = activeClock
            sendBroadcast(Intent(ClockService.BROADCAST_CLOCK_CHANGED))
        }

        if (clocks.size() != 0) {
            showUI()
            pager.setAdapter(ClockAdapter(this, clocks))
            showPageData(0)
        } else {
            hideUI()
            emptyLabel.show()
        }
    }

    fun hideUI() {
        pager.hide()
        indicator.hide()
        title.hide()
        author.hide()
        authorLabel.hide()
        description.hide()
        descriptionLabel.hide()
        applied.hide()
        apply.hide()
    }

    fun showUI() {
        pager.show()
        indicator.show()
        title.show()
        author.show()
        authorLabel.show()
        description.show()
        descriptionLabel.show()
        emptyLabel.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        getMenuInflater().inflate(R.menu.activity, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMPORT_FILE_REQUEST_CODE && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                runOnUiThread {
                    dialog = ProgressDialog.show(this, "Please wait ...", "Importing Zip", true)
                    dialog!!.setCancelable(false)
                    var exception: ImportClockException? = null
                    var clock: Clock? = null
                    async {
                        try {
                            val stream = getContentResolver().openInputStream(data.getData())
                            clock = importZip(stream)
                        } catch (e: ImportClockException) {
                            exception = e
                        } catch (e: FileNotFoundException) {
                            exception = ImportClockException(ImportClockException.Error.READ_ERROR)
                        }
                        runOnUiThread {
                            dialog?.dismiss()
                            if (exception != null) {
                                longToast(exception!!.errorMessage)
                            } else {
                                val newSet = HashSet(Prefs.clocks)
                                newSet.add(clock.toString())
                                Prefs.clocks = newSet
                                initUI()
                                indicator.setCurrentItem(clocks.size() - 1)
                                showPageData(clocks.size() - 1)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
        dialog = null
    }

    private fun importZip(zip: InputStream): Clock {
        try {
            val outputDir = getCacheDir()
            val outputFile = File.createTempFile("zip", ".zip", outputDir)

            val inputStream = BufferedInputStream(zip)
            val out = BufferedOutputStream(FileOutputStream(outputFile))

            val buffer = ByteArray(1024)
            var r = inputStream.read(buffer, 0, buffer.size())
            while ((r) != -1) {
                out.write(buffer, 0, r)
                r = inputStream.read(buffer, 0, buffer.size())
            }
            out.close()
            inputStream.close()

            val c = importZip(outputFile.getAbsolutePath())
            outputFile.delete()
            return c
        } catch (e: IOException) {
            throw ImportClockException(ImportClockException.Error.READ_ERROR)
        }

    }

    private fun importZip(zip: String): Clock {
        try {
            val zipFile = ZipFile(zip)

            val clockEntry = zipFile.getEntry("clock.xml") ?: throw ImportClockException(ImportClockException.Error.NO_CLOCK_XML)

            val clock = Clock.parse(zipFile.getInputStream(clockEntry).reader(Charsets.UTF_8).readText())

            val path = getFilesDir().path + "/" + clock.id + "/"
            val clockDir = File(path)
            clockDir.mkdirs()
            setFilePermission(clockDir, "0755")

            val clockFile = File(path + clockEntry.getName())
            writeFile(zipFile, clockEntry, clockFile)
            setFilePermission(clockFile, "0644")

            for (filename in clock.files) {
                val fileEntry = zipFile.getEntry(filename) ?: throw ImportClockException(ImportClockException.Error.MISSING_FILE)
                val f = File(path + filename)
                writeFile(zipFile, fileEntry, f)
                setFilePermission(f, "0644")
            }
            val previewEntry = zipFile.getEntry("preview.png") ?: zipFile.getEntry("Preview.png")
            if (previewEntry != null) {
                val f = File(path + "preview.png")
                writeFile(zipFile, previewEntry, f)
                setFilePermission(f, "0644")
            }
            return clock
        } catch (e: IOException) {
            e.printStackTrace()
            throw ImportClockException(ImportClockException.Error.READ_ERROR)
        }
    }

    private fun setFilePermission(f: File, permission: String): Boolean {
        val command = "chmod " + permission + " " + f.getAbsolutePath()

        val runtime = Runtime.getRuntime()
        val process: Process
        var error = false
        try {
            process = runtime.exec(command)
            try {
                process.waitFor()
                val stdError = BufferedReader(InputStreamReader(process.getErrorStream()))
                while (stdError.readLine() != null) {
                    error = true
                }
                process.getInputStream().close()
                process.getOutputStream().close()
                process.getErrorStream().close()
            } catch (e: InterruptedException) {
                error = true
            }

        } catch (exception: IOException) {
            error = true
        }

        return !error
    }

    private fun writeFile(file: ZipFile, entry: ZipEntry, out: File) {
        val fout = BufferedOutputStream(FileOutputStream(out))
        val inputStream = BufferedInputStream(file.getInputStream(entry))

        val buffer = ByteArray(1024)
        var count: Int = inputStream.read(buffer)
        while (count != -1) {
            fout.write(buffer, 0, count)
            count = inputStream.read(buffer)
        }

        fout.close()
        inputStream.close()
    }

    companion object {
        private val IMPORT_FILE_REQUEST_CODE = 42
    }
}