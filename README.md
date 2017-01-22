
## BLog - Android Log Extended Utility
[中文](/README_CN.md)
<img align="right" src="https://img.shields.io/badge/minSdk-9-brightgreen.svg"/>
<img align="right" src="https://api.bintray.com/packages/kaedea/moe-studio/b-log/images/download.svg" href = "https://bintray.com/kaedea/moe-studio/b-log/_latestVersion"/>
<img align="right" src="https://img.shields.io/hexpm/l/plug.svg"/>


BLog is an Android LogCat extended Utility. It can simplify the way you use
{@link android.util.Log}, as well as write our log message into file for after support.

**BLog is not pronounced 'Blog[blɒɡ]', but '[bi:lɒɡ]'.**


### Feature
 1. Simplified Api for logging message.
 2. Print `Thread Info`.
 3. Set `LogLevel` to control whether to print log or not.
 4. Write log message to `file` in order to trace bugs from release app.

**Though BLog support using LogLevel to control whether to print log message or
not, it is recommended to use `if statement with a constant as condition` to
control the Log Block as the following snippet.**

```java
if (BuildConfig.DEBUG) {
  BLog.v(TAG, "Log verbose");
}
```

Please try `best performance` in any case. :)


### Getting Started
---

#### Dependency & Initialization
Add dependency.
```java
compile 'moe.studio:b-log:1.0.0'  // Please use the latest version
```
Initialization.
```java
BLog.initialize(context);
```

#### Basic
Print log message.
```java
BLog.v(TAG, "log verbose");
BLog.v("log verbose with default tag");

BLog.d(TAG, "log debug");
BLog.d("log debug with default tag");

BLog.i(TAG, "log info");
BLog.i("log info with default tag");

BLog.w(TAG, "log warning");
BLog.w("log warning with default tag");

BLog.e(TAG, "log error");
BLog.e("log error with default tag");

BLog.wtf(TAG, "log wtf");
BLog.wtf("log wtf with default tag");
```

Print event message.
```java
BLog.event(TAG, "event A");
BLog.event("event B");
BLog.event("Excited!");
```

Get log files.
```java
// Get log files;
File all = BLog.zippingLogFiles(LogSetting.LOG, null);
// Get log & event files.
File all = BLog.zippingLogFiles(LogSetting.LOG | LogSetting.EVENT, null);

// Get logs with addiction files.
List<File> attaches = new ArrayList<>();
attaches.add(outDate1);
attaches.add(outDate2);
File attach = BLog.zippingLogFiles(LogSetting.LOG | LogSetting.EVENT, attaches);
```

#### Advanced
Print exception.
```java
Exception exception = new RuntimeException("...");

BLog.v(TAG, "runtime exception", exception);
BLog.v(exception);
```

Print String with format.
```java
BLog.vfmt(TAG, "log %s with format string", "verbose");
BLog.dfmt(null, "log %s with format string", "debug");
BLog.ifmt(TAG, "log %s with format string", "info");
BLog.wfmt(null, "log %s with format string", "warning");
BLog.efmt(TAG, "log %s with format string", "error");
BLog.wtffmt(null, "log %s with format string", "wtf");
```

In general, BLog uses a worker thread to write log messages into file. If you want to log message synchronously into file, you'd better use the following api.
```java
BLog.syncLog(LogPriority.VERBOSE, "TEST", "Sync Log.");
BLog.syncLog(LogPriority.DEBUG, "TEST", "Sync Log.");
```

Besides, you can set a custom LogAdapter to do some addiction jobs when executing a log.
```java
LogSetting setting = new LogSetting.Builder(context)
                .setAdapter(new Log() {
                    @Override
                    public void log(int priority, String tag, String msg) {
                        // Do something.
                    }

                    @Override
                    public void onShutdown() {
                        // Do something.
                    }
                })
                .build();

BLog.initialize(setting);
```

#### Custom Setting
Initialize BLog
```java
BLog.initialize(Context);
```

Initialize BLog with custom setting
```java
LogSetting setting = new LogSetting.Builder(context)
        .setDefaultTag("TEST")
        .setLogDir(logDir.getPath())
        .setExpiredDay(1)
        .setLogcatLevel(LogLevel.DEBUG)
        .setLogfileLevel(LogLevel.INFO)
        .setEventLevel(LogLevel.VERBOSE)
        .setFormatter(new LogFormatterImpl())
        .setAdapter(new Log())
        .build();

BLog.initialize(setting);
```

In general, BLog will shutdown itself when the application is terminated, but you can use `BLog#shutdown()` to shutdown BLog.

For more usage showcases, please check out the [test codes](https://github.com/kaedea/b-log/tree/release/bintray/library/src/androidTest/java/moe/studio/log).

