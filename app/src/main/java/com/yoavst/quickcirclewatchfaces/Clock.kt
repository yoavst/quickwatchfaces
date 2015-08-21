package com.yoavst.quickcirclewatchfaces

import android.graphics.Color
import org.joox.JOOX
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * A Pojo for this xml file:
<clock>
<title>Example Clock</title>
<id>de.bigboot.quickcirclemod.example</id>
<author>Kevin Slaton</author>
<description>A watchface by Kevin Slaton</description>
<dateTextColor>#RRGGBB or #AARRGGBB</dateTextColor>
<dateBackgroundColor>#RRGGBB or #AARRGGBB</dateBackgroundColor>
<hideDateText>true or false</hideDateText>
<position>
<replaces>
<file>b2_quickcircle_analog_style02_hour.png</file>
<file>b2_quickcircle_analog_style02_minute.png</file>
<file>b2_quickcircle_analog_style02_second.png</file>
<file>b2_quickcircle_analog_style02_bg.png</file>
</replaces>
</clock>
 **/
public class Clock(public var title: String, public var id: String, public var author: String, public var description: String, public var hideDateText: Boolean,
                   public var dateTextColor: Int, public var dateBackgroundColor: Int, public var files: Array<String>) {

    public constructor() : this("", "", "", "", false, Color.WHITE, Color.GRAY, emptyArray())

    public override fun toString(): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<clock>\n\t<title>$title</title>\n\t<id>$id</id>\n\t<author>$author</author>\n\t<description>$description</description>\n\t<dateTextColor>$dateTextColor</dateTextColor><dateBackgroundColor>$dateBackgroundColor</dateBackgroundColor><hideDateText>${if (hideDateText) "true" else "false"}</hideDateText>\n\t<replaces>\n")
        for (file in files)
            sb.append("\t\t<file>$file</file>\n")
        sb.append("\t</replaces>\n</clock>")
        return sb.toString()
    }

    companion object {
        public fun parse(data: String): Clock {
            val document = JOOX.`$`(JOOX.`$`(ByteArrayInputStream(data.toByteArray(StandardCharsets.UTF_8))).elementAt(0)) // the clock element
            var clock = Clock()
            clock.title = document.find("title").text()
            clock.id = document.find("id").text()
            clock.author = document.find("author").text()
            clock.description = document.find("description").text()
            clock.hideDateText = document.find("hideDateText").text() == "false"
            val dayTextColor = document.find("dateTextColor").text()
            if (dayTextColor != null)
            clock.dateTextColor = Color.parseColor(dayTextColor)
            val dayBackgroundColor = document.find("dateBackgroundColor").text()
            if (dayTextColor != null)
            clock.dateBackgroundColor = Color.parseColor(dayBackgroundColor)
            clock.files = (JOOX.`$`(document.find("replaces")).find("file") map { it.getTextContent() }).toTypedArray()
            return clock
        }
    }
}