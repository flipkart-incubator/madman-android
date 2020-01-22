# madman

The Madman library (Media Ads Manager) enables you to advertise video contents with video ads. 

The library is designed to 

* retrieve ads from VAST-compliant ad servers
* help handle ad playback
* collect and report metrics back to ad servers.


## Why Madman ?

* <b>Performance:</b> Initial numbers have shown madman is ~700 ms faster in loading pre-roll ads compared to other libraries such as IMA.
  
* <b>UI Customisability:</b>

  * change skip ad UI
  * change learn more UI
  * change "Ad starting in.." UI
  * custom UI layer
  
* <b>Features and Extensibility:</b> 

  * Ad starting countdown timer saying ad starting in 5,4 etc
  * Change backoff strategy for network layer
  * Change AdBreak finder strategy: It is used to determine which ad break to play given the current position of the player.


## Performance

Initial numbers for playing pre-roll ads (Test env: API-29 emulator, wifi, similar ad response, 5 iterations):

  * IMA: 2.04 seconds
  * Madman: 1.35 seconds

The madman is approximately ~700 ms faster in loading the pre-roll ad as compared to IMA.

See it for yourself: 

![Comparsion](https://github.fkinternal.com/Flipkart/madman-android/blob/master/files/comparison.gif)


## Get Madman
Add it in your root build.gradle at the end of repositories :
```kotlin
allprojects {
  repositories {
    maven { url "https://jitpack.io" }
  }
}
```

Add the dependencies :

* Library :
```kotlin
dependencies {
  implementation 'com.github.flipkart-incubator.madman:madman:1.0.0'
}
```

* Network Module :
```kotlin
dependencies {
  implementation 'com.github.flipkart-incubator.madman:madman-okhttp:1.0.0'
}
```

## How to use ?

#### Initiliase the Madman instance 
```kotlin
val madman = Madman.Builder()
             .setAdErrorListener(this)
             .setAdLoadListener(this)
             .setNetworkLayer(DefaultNetworkLayer(context))
             .setAdEventListener(this)
             .build(context)
```

#### Create AdRenderer
```kotlin
val adRenderer = DefaultAdRenderer.Builder().setPlayer(this).setContainer(adViewGroup).build(null)
```

#### Request Ads

1. <b>From Network</b>
```kotlin
val request = NetworkAdRequest()
request.setUrl(adTagUri.toString())
madman.requestAds(request, adRenderer)
```

2. <b>From Local</b>
```kotlin
val request = StringAdRequest()
request.setResponse("")
madman.requestAds(request, adRenderer)
```


## Getting Started

For more information, read <b>[Wiki](https://github.fkinternal.com/Flipkart/madman-android/wiki)</b>


## What's missing

The following features of VAST are not available in the library yet.

### VAST
* Companion ads
* Non-Linear ads
* Executable media files
* Ad Pods
* Wrapper ads (for redirection)
* Industry icon support

### VMAP
* Ad break time-offsets such as #1, x% etc. Only format supported for now is HH:MM:SS.mmm
* Increasing unit test coverage

Note: Google AdSense/AdManager Ads will not work with this library due to the absense of executable media files support


## License

    The Apache License
    
    Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
    
    Licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License.
    
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0 
       
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    
    See the License for the specific language governing permissions and 
    limitations under the License.

## Contributing

* Fork the repo
* Apply the changes
* Submit your pull request
