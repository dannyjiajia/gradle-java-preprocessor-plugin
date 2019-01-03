# gradle-java-preprocessor-plugin

[![Build Status](https://travis-ci.org/dannyjiajia/gradle-java-preprocessor-plugin.svg?branch=master)](https://travis-ci.org/dannyjiajia/gradle-java-preprocessor-plugin)
[ ![Download](https://api.bintray.com/packages/dannyjiajia/gradle-plugins/plugin/images/download.svg) ](https://bintray.com/dannyjiajia/gradle-plugins/plugin/_latestVersion)



Chinese version [README_CN.md](README_CN.md)


## Features

The plugin is a gradle adaptation of the `Antenna` [preprocessor](http://antenna.sourceforge.net/wtkpreprocess.php) task.

* We can add custom task to handle the java source code.
* The plugin expand the `productFlavors` config in Android plugin,it's auto add process tasks before android build tasks.

## Installation

Add the following to your `build.gradle`:

~~~
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'wang.dannyhe.tools:plugin:latest.release' //use the latest version 
    }
}
apply plugin: 'com.android.application' //'com.android.library'
apply plugin: 'wang.dannyhe.tools.preprocessor' //make sure to apply this plugin after the Android plugin
~~~

## Usage
### Config(above v0.0.6)

The global config is required.

~~~
preprocessor {
    verbose true
    sourceDir file("src/main/java") //required
    targetDir file("src/main/java") //required
    symbols "GLOBAL","GLOBAL_2" //symbols is  valid for each flavors
}
~~~

`processor`  in `productFlavors` without `sourceDir` and `targetDir`,and you can use `flavorDimensions`.

~~~
flavorDimensions "money", "channel"
    productFlavors {

        xiaomi {
            processor.symbols "XIAOMI"
            dimension "money"
        }

        huawei {
            processor.symbols "HUAWEI"
            dimension "channel"
        }

        free {
            // uncomment to test process.
            // processor.symbols "FREE","PRINT"
            // processor.symbols "FREE","VERSION=1"
            processor.symbols "FREE","VERSION=5"
            dimension "channel"
        }

        vip {
            processor.symbols "VIP"
            dimension "channel"
        }
    }
~~~

### Config(0.0.5 and previous versions)

The global config is optional.

~~~
// global setting
preprocessor {
    verbose true //print the log message
    sourceDir file("src/main/java") //the root dir of java source files
    targetDir file("src/main/java") //the root dir to export the java source files
    groupName 'preprocessor' //the group name for plugin auto create tasks
}
~~~

Plugin adds `processor` to the Android plugin in `productFlavors`.You can define the processor argments for each flavor build.

~~~
productFlavors {
	free {
	    processor.symbols "FREE_VERSION" //define the symbols to java
	}
	normal {
	    processor.symbols "NFREE_VERSION"
	    processor.sourceDir file("src/main/java") //this config override the global config
	    processor.targetDir file("src/main/java") //this config override the global config
	}
}
~~~

### Use macro in Java

We can add "macro" in java file,as `MainActivity.java`:

~~~
//#ifdef FREE_VERSION
	Log.i("sample","I am Free Version");
//#else
	Log.i("sample","I am not Free Version");
//#endif
~~~

### Execute the task

The task would be auto executed before Android java Compile task,and we can execute it manually.

~~~
gradle preprocessFreeRelease
~~~

Finally,the `MainActivity.java` file would be modify by plugin:

~~~
//#ifdef FREE_VERSION
	Log.i("sample","I am Free Version");
//#else
//@		Log.i("sample","I am not Free Version");
//#endif
~~~

### Custom task

we can define the custom preprocessor task.

~~~
// custom task
task customProcessJavaTask(type:wang.dannyhe.tools.PreprocessorTask) {
    sourceDir file("src/main/java")
    targetDir file("src/main/java")
    symbols "FREE_VERSION,PRINT" 
    verbose true
}
~~~
