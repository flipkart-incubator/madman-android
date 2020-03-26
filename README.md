# madman

[![](https://jitpack.io/v/flipkart-incubator/madman-android.svg)](https://jitpack.io/#flipkart-incubator/madman-android)
[![Build Status](https://travis-ci.org/flipkart-incubator/madman-android.svg?branch=master)](https://travis-ci.org/flipkart-incubator/madman-android) 
[![codecov.io](https://codecov.io/github/flipkart-incubator/madman-android/branch/master/graph/badge.svg)](https://codecov.io/github/flipkart-incubator/madman-android)

The Madman library (Media Ads Manager) enables you to advertise video contents with video ads. If you have your own VAST server and want to render video ads on Android and have full control over the UI, then this library is for you.

The library is designed to 

* retrieve ads from VAST-compliant ad servers
* help handle ad playback
* collect and report metrics back to ad servers.

## `Note: It is in alpha stage`

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

#### Load time comparison: 

![Comparsion](https://github.com/flipkart-incubator/madman-android/blob/master/files/comparison.gif)


## Madman Integration

If you need to integrate directly with the madman library, follow the below steps

### 1. Get Madman

Add the jitpack dependency in your root build.gradle

```kotlin
allprojects {
  repositories {
    maven { url "https://jitpack.io" }
  }
}
```

Add the madman dependency

```kotlin
dependencies {
   implementation 'com.github.flipkart-incubator.madman:madman:1.0.0' // core madman
   implementation 'com.github.flipkart-incubator.madman:madman-okhttp:1.0.0' // optional network layer module
}
```

### 2. Initialise Madman

```kotlin
val madman = Madman.Builder()
              .setAdErrorListener(this) // ad error callbacks
              .setAdLoadListener(this) // ad load callbacks
              .setNetworkLayer(DefaultNetworkLayer(context)) // use the default network layer, override if necessary
              .setAdEventListener(this) // ad event callbacks
              .build(context)
```

Read the [doc](https://github.com/flipkart-incubator/madman-android/wiki/Madman) to understand the components in detail.

### 3. Create AdRenderer

AdRenderer houses the logic for rendering the UI components on the top of the ad. The library provides a default layout for the ad overlay, which can be overriden if required. 

```kotlin
val adRenderer = DefaultAdRenderer.Builder()
		  .setPlayer(this) // player interface
		  .setContainer(adViewGroup) // parent view to which the overlay gets added
		  .build(null) // passing null will fallback to default UI layout
```

To override the default layout, create a new `AdViewBinder` and pass it while calling `build` on the AdRenderer

```kotlin
val adViewBinder = AdViewBinder.Builder()
	            .setLayoutId(R.layout.ad_overlay) // layout of custom ad overlay
		    .setSkipViewId(R.id.skip_button) // specify the skip view id
		    .setClickThroughViewId(R.id.click_throught_button) // specify the learn more view id
		    .build()

val adRenderer = DefaultAdRenderer.Builder()
		  .setPlayer(this) // player interface
		  .setContainer(adViewGroup) // parent view to which the overlay gets added
		  .build(adViewBinder) // passing custom ad view binder
```

### 4. Request Ads

* From network:

```kotlin
val request = NetworkAdRequest()
request.setUrl(adTagUri.toString())
madman.requestAds(request, adRenderer)
```

* From local cache:

```kotlin
val request = StringAdRequest()
request.setResponse(adResponse)
madman.requestAds(request, adRenderer)
```


## Madman Exo-Player Integration

If you are using exo-player, you can directly use the MadmanAdLoader plugin which acts as a glue between the madman library and exo-player instance.

```kotlin
dependencies {
   implementation 'com.github.flipkart-incubator.madman:madman-exoplayer-extension:1.0.0'
}
```

Checkout the demo app and [MadmanPlayerManager](https://github.com/flipkart-incubator/madman-android/blob/master/app/src/main/java/com/flipkart/mediaads/demo/madman/MadmanPlayerManager.java) to understand the integration process in detail.


## Documentation

For more information, read <b>[Wiki](https://github.com/flipkart-incubator/madman-android/wiki)</b>


## What's missing ?

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
    
    Copyright (c) 2020 Flipkart Internet Pvt. Ltd.
    
    Licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License.
    
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0 
       
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    
    See the License for the specific language governing permissions and 
    limitations under the License.
