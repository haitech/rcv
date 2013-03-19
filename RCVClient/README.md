RCV Client
===
The RCV Client is a Android based client to control the RCV.

<img src="https://raw.github.com/haitech/rcv/gh-pages/screenshots/rcvclient_main-v0.1.png" height="250px" width="180px" />
<img src="https://raw.github.com/haitech/rcv/gh-pages/screenshots/rcvclient_vehicle-v0.1.png" height="180px" width="250px" />

#Build RCV Client
Before can get started you need Android SDK and ANT.

Get the latest RCVClient:
    
`git clone git://github.com/haitech/rcv.git`

Browser the directory.
```bash
cd rcv/RCVClient
```

To build a debug version you will need to define the location of your Android SDK.

`echo "sdk.dir=/dir/to/your/android-sdk-linux" > local.properties`

The local.properties should be located in rcv/RCVClient.

Now you can build a debug version
```bash
ant debug
ant debug install
```
Start the application:

`adb shell am start -a android.intent.action.MAIN -n no.haitech.rcvclient/.MainActivity`
