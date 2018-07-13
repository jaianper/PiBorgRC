# PiBorgRC
Android App to control Raspberry pi robots using the PicoBorg Reverse motor controller.

- TCP communication has been implemented to send driving commands from the mobile device to the Raspberry Pi.
- The image collected by PiCamera is streamed via UDP to the mobile device using the GStreamer library.
- The python file named MetalborgController.py must be run on the Raspberry Pi to start the socket server and the video streaming from PiCamera.

## Auto start at boot

To make the python script load on its own, do the following:

1. Open the Cron table using `crontab -e`
2. Add a cron job to the bottom of the file using the following line:

    ```@reboot sudo /<directory-path>/MetalborgController.py```
    
3. Save the file
4. Close the file

The cron table should now auto-run the script when the Raspberry Pi boots up.

### For more Info:
- [DiddyBorg Metal - Build](https://www.piborg.org/blog/build/diddyborg-metal-build)
- [Raspberry Pi Camera Module](https://www.raspberrypi.org/documentation/raspbian/applications/camera.md)
- [Python interface to the Raspberry Pi camera](https://picamera.readthedocs.io/en/release-1.13/)
- [Installing GStreamer for Android development](https://gstreamer.freedesktop.org/documentation/installing/for-android-development.html)
- [GStreamer Android tutorials](https://gstreamer.freedesktop.org/documentation/tutorials/android/index.html)

Shortly I will update the settings that you must make in the code for its correct operation.
