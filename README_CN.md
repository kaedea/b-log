## BLog - Android Log Utils
[![](https://img.shields.io/hexpm/l/plug.svg)](#) [![](https://img.shields.io/badge/minSdk-9-brightgreen.svg)](#) [![Download](https://api.bintray.com/packages/kaedea/moe-studio/b-log/images/download.svg)](https://bintray.com/kaedea/moe-studio/b-log/_latestVersion)

BLog 是 Android SDK 的 LOG 工具 {@link android.util.Log} 的加强版，以方便在开发时用来
操作调试日志。

> LOG 是任何一种编程语言的第一个API，通常被初学者用来打印 `Hello, World!`。 有研究显示，
不使用 LOG 或者使用姿势错误的人，感情路都走得很辛苦，有七成的比例会在 34 岁的时候跟
自己不爱的人结婚，而其馀叁成的人最後只能把遗产留给自己的猫。毕竟爱情需要书写，不能是一整张白纸。


### 特点
 1. 简单易用的API；
 2. 支持输出线程信息；
 3. 支持设置LogLevel，方便在生产环境关闭调试用的LOG；
 4. 支持将LOG内容写入文件，以便通过文件LOG定位用户反馈的问题；

注意，尽管BLog支持关闭Log的输出，但是在你调用 `BLog.v(String)` 的时候，其实已经造成了性能
丢失，所以请尽量使用正确的姿势来使用BLog，比如
```java
if (BuildConfig.DEBUG) {
  BLog.v(TAG, "log verbose");
}
```


### 开始使用
#### 依赖和初始化
添加依赖
```java
    compile 'moe.kaede:blog:0.3.0'  // 记得使用最新版本
```
初始化
```java
BLog.initialize(context);
```

#### 基本用法
打印Log
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

打印Event

```java
BLog.event(TAG, "event A");
BLog.event("event B");
BLog.event("Excited!");
```


#### 进阶用法
打印异常信息
```java
Exception exception = new RuntimeException("...");

BLog.v(TAG, exception, "runtime exception");
// or
BLog.v(exception, null);
```

打印Format格式的字符串
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


#### 自定义
使用BLog的API之前，必须进行初始化
```java
 BLog.initialize(Context);
```

进行一些自定义设置
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
