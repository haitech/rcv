RCV Client
===
The RCV Client is a Android based client to control the RCV.

#Build RCV Client
Before can get started you need Android SDK and ANT.

Get the latest RCVClient:
    `git clone git://github.com/haitech/rcv.git`
Build a debug version
```bash
cd rcv/RCVClient
ant debug
ant debug install
```
Start the application
    `adb shell am start -a android.intent.action.MAIN -n no.haitech.rcvclient/.MainActivity`
