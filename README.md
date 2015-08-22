#Quick Circle Watchfaces

Quick Circle Watchfaces is an Android app which allows you to easily change the watchfaces of the LG G4 Quick Circle Case.
**It is still in alpha version.**

![Screenshot](http://i.imgur.com/IvA4TXMl.png)

##Credit
All the credit goes to [BigBoot](https://github.com/BigBoot) for the [original QCThemer](https://github.com/BigBoot/qcthemer).
I've just redesigned it and added a support the G4 based on [this](https://lyingdragonblog.wordpress.com/2015/08/06/how-to-create-quick-circle-clock-faces-using-other-apps/?preview_id=50) great guide.

##Download
Download of the app is still not available, you can compile it if you want.

You can find some themes [here](https://qcthemer.net/) or [here](http://forum.xda-developers.com/lg-g3/themes-apps/quick-circle-watches-t2906614)

##Theme creation
See Getting the original resources to get an Idea about the files to modify.
Once you've got your modified files, you need to package them for the app.

1. You need a clock.xml, here's an example:

```xml
  <?xml version="1.0" encoding="utf-8"?>
  <clock>
      <!--Title of the clock-->
      <title>Awesome clock</title>
      <!--some identifier. Use something unique-->
      <id>com.coolguy.awesome</id>
      <!--Name of the Author-->
      <author>CoolGuy42</author>
      <!--Description of the clock-->
      <description>Some description</description>
      <!-- Optional - the color of the date text - Default BLACK -->
      <dateTextColor>#RRGGBB or #AARRGGBB</dateTextColor>
      <!-- Optional - the background color of the date text - Default GRAY -->
      <dateBackgroundColor>#RRGGBB or #AARRGGBB</dateBackgroundColor>
      <!-- Optional - hide the date - Default show date -->
      <hideDateText>true or false</hideDateText>
      <!-- Optional - SimpleDateFormat for the date - Default EEE d (E.G: Fri 21)-->
      <dateFormat>MMM d</dateFormat>
      <!-- Optional - gravity for date - Default right -->
      <dateGravity>left or right or top or bottom</dateGravity>
      <!-- Optional - text size for date - Default 16 (all units are in sp)-->
      <dateTextSize>18</dateTextSize>
      <replaces>
		  <!--List all files, you want to replace-->
          <file>b2_quickcircle_analog_style03_hour.png</file>
          <file>b2_quickcircle_analog_style03_minute.png</file>
          <file>b2_quickcircle_analog_style03_second.png</file>
          <file>b2_quickcircle_analog_style03_bg.png</file>
      </replaces>
  </clock>
 ```
2. You should optionally provide a preview.png which will be shown in the watchface chooser
3. Now zip up all your files, in the example you would zip the following files:
```
clock.xml
preview.png
b2_quickcircle_analog_style03_hour.png
b2_quickcircle_analog_style03_minute.png
b2_quickcircle_analog_style03_second.png
b2_quickcircle_analog_style03_bg.png
```

## Version
0.1 ALPHA

## Acknowledgements
* Excilys team for [Android annotations](https://github.com/excilys/androidannotations/wiki)
* Jake Wharton for [viewpagerindicator](http://viewpagerindicator.com/).
* Kevin Slaton for the example watchface
