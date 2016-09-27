BLog is an Android LogCat extended Utility. It can simplify the way you use
{@link android.util.Log}, as well as write our log message into file for after support.

**BLog is not pronounced 'Blog[blɑg]', but '[bi:bɑg]'.**

> LOG 是任何一种编程语言的第一个API，通常被初学者用来打印 `Hello, World!`。 有研究显示，
不使用 LOG 或者使用姿势错误的人，感情路都走得很辛苦，有七成的比例会在 34 岁的时候跟
自己不爱的人结婚，而其馀叁成的人最後只能把遗产留给自己的猫。毕竟爱情需要书写，不能是一整张白纸。


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
  BLog.v(TAG, "log verbose");
}
```

Please try `best performance` in any case. :)


### Usage
#### Dependency
```java
    compile 'moe.kaede:blog:0.1.5'
```

#### Basic
Print log message
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

Print event message
```java
BLog.event(TAG, "event A");
BLog.event("event B");
BLog.event("Excited!");
```


#### Advanced
Print exception
```java
Exception exception = new RuntimeException("...");

BLog.v(TAG, exception, "runtime exception");
// or
BLog.v(exception, null);
```

Print String with format
```java
// use log format, you must offer a tag, even it's null(use default tag)
BLog.vfmt(TAG, "log %s with format string", "verbose");
BLog.dfmt(null, "log %s with format string", "debug");
BLog.ifmt(TAG, "log %s with format string", "info");
BLog.wfmt(null, "log %s with format string", "warning");
BLog.efmt(TAG, "log %s with format string", "error");
BLog.wtffmt(null, "log %s with format string", "wtf");

// test error format args
// 1. error format msg
BLog.dfmt(null, "error format msg", "debug");
// 2. error format args
BLog.dfmt(null, "%s format msg", "error", "error", "error");
BLog.dfmt(null, "%s %s %s format msg", "error");
```


#### Setting
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
        .build();

BLog.initialize(setting);
```
