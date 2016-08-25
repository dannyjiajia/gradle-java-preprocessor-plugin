# gradle-java-preprocessor-plugin

## 特点
将[Antenna](http://antenna.sourceforge.net/wtkpreprocess.php)的Preprocess功能适配到gradle的插件中,可以在Android项目中使用宏修改`Java`代码.

* 可以灵活自定义处理java代码的任务
* 插件拓展`productFlavors`,在build的时候自动执行任务

## 安装

在`build.gradle`中加入以下配置

~~~
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'wang.dannyhe.tools:plugin:0.0.2'
    }
}
apply plugin: 'com.android.application'
apply plugin: 'wang.dannyhe.tools.preprocessor' //一定要在android插件用用后再应用这个插件
~~~

## 使用

### 配置

全局配置,这个是可选的.

~~~
// global setting
preprocessor {
    verbose true //是否打印日志
    sourceDir file("src/main/java") //Java源码所在根目录
    targetDir file("src/main/java") //生成的Java的根目录
    groupName 'preprocessor' //插件根据productFlavors生成的任务所在的分组名
}

~~~

配置`productFlavors`

~~~
productFlavors {
	free {
	    processor.symbols "FREE_VERSION" //定义宏,其他设置会继承preprocessor中的全局配置
	}
	normal {
	    processor.symbols "NFREE_VERSION"
	    processor.sourceDir file("src/main/java") //如果全局配置中存在sourceDir配置,这里会覆盖
	    processor.targetDir file("src/main/java") //如果全局配置中存在targetDir配置,这里会覆盖
	}
}
~~~


### Java中使用宏

比如在`MainActivity.java`中使用

~~~
//#ifdef FREE_VERSION
	Log.i("sample","I am Free Version");
//#else
	Log.i("sample","I am not Free Version");
//#endif
~~~

### 执行任务

执行构建任务便会自动执行Java源码的修改,当然我们也可以手动执行任务.如:

~~~
gradle preprocessFreeRelease
~~~

最终Java代码会变成:

~~~
//#ifdef FREE_VERSION
	Log.i("sample","I am Free Version");
//#else
//@		Log.i("sample","I am not Free Version");
//#endif
~~~

可以看见`FREE_VERSION`已经生效并自动修改了Java源码

### 拓展使用

自定义任务:

~~~
// custom task
task customProcessJavaTask(type:wang.dannyhe.tools.PreprocessorTask) {
    sourceDir file("src/main/java")
    targetDir file("src/main/java")
    symbols "FREE_VERSION"
    verbose true
}
~~~