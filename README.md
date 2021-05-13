# Rx3Nearby
[![](https://jitpack.io/v/tdascoli/Rx3Nearby.svg)](https://jitpack.io/#tdascoli/Rx3Nearby)

[RxJava 3.0](https://github.com/ReactiveX/RxJava/tree/3.x) wrapper on Google's [Nearby](https://developers.google.com/nearby/) library.

This repository started of [Hiroshi Kurokawa](https://github.com/hkurokawa) RxNearby library. You can check his work [here](https://github.com/hkurokawa/RxNearby).

## Download

##### Gradle:

```groovy
dependencies {
    implementation 'com.github.tdascoli:Rx3Nearby:1.0.0'
}
```
```
allprojects {
    repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
```

# Usage
## Nearby Messages
### Subscribe
You can watch a sequence of the received messages as an `Observable`.
```java
RxNearby.subscribe(this, statusResolver)
        .subscribe(new Action1<Message>() {
          @Override
          public void call(Message message) {
            // do something
          }
        });
```

Note you have to provide how to resolve a resolvable status (which means `status.hasResolution()` returns `true`) is returned during a sequence of Nearby API calls with the second argument. With this argument, you can specify when the retrial of the subscription should be executed. Internally, it uses `Observable.retryWhen()` method. See [ReactiveX - Retry operator](http://reactivex.io/documentation/operators/retry.html) for more information.

```java
final Func1<Status, Observable<Void>> statusResolver = new Func1<Status, Observable<Void>>() {
  @Override
  public Observable<Void> call(Status status) {
    status.startResolutionForResult(MainActivity.this, REQ_RESOLVE_MESSEAGE_API_ERROR);
    return retry;
  }
};
```

### Publish
You have to prepare an `Observable` which emits `Message` events. Everytime it emits an event, RxNearby Message Publish API is called. 
```java
RxNearby.publish(this, sendMessageSubject, statusResolver)
        .subscribe(new Action1<PublishResult>() {
          @Override
          public void call(PublishResult publishResult) {
            // do something
          }
        });
```

## Nearby Connections
T. B. D.

# Sample
A sample app. to do a simple publish/subject task is under `/rxnearby-sample`

![rxnearby-sample](https://cloud.githubusercontent.com/assets/6446183/11506865/c5dca7d8-9894-11e5-8d0c-57952e299bd7.gif)

<a href='https://ko-fi.com/H2H32EWM1' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://cdn.ko-fi.com/cdn/kofi1.png?v=2' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

## License

	MIT License

	Copyright (c) 2021 Thomas D'Ascoli

	Permission is hereby granted, free of charge, to any person obtaining a 
	copy of this software and associated documentation files (the "Software"), 
	to deal in the Software without restriction, including without limitation 
	the rights to use, copy, modify, merge, publish, distribute, sublicense, 
	and/or sell copies of the Software, and to permit persons to whom the 
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included 
	in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
	THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
	OTHER DEALINGS IN THE SOFTWARE.
