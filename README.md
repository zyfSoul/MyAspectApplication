# 背景

云图项目是用于在产品统计用户行为数据（类似友盟），并上传到后端服务器，之前采用用户的行为数据需要在接入云图SDK后再手动的添加埋点代码，这在接入云图时带来了额外的工作量，并且在随后的业务扩展时还需要不断的引入新的埋点代码，过程繁琐且不便。因此云图项目考虑使用无埋点技术，再不添加埋点代码的情况下采集用户的某些行为，这篇文章将介绍和解析在云图Android SDK如何实现无埋点技术。

# AOP

AOP：面向切面编程，**这种在运行时，动态地将代码切入到类的指定方法、指定位置上的编程思想就是面向切面的编程。**

AOP中的几个概念：

**切面：**横切关注点【影响应用多处的功能（安全、事务、日志）】被模块化为特殊的类，这些类称为切面；切面是通知和切点的集合，通知和切点共同定义了切面的全部功能——它是什么，在何时何处完成其功能

**通知：**切面也需要完成工作。在 AOP 术语中，切面的工作被称为通知。

**连接点（Join Point）：**连接点是一个应用执行过程中能够插入一个切面的点。

**切点（Pointcut）**：如果通知定义了“什么”和“何时”。那么切点就定义了“何处”。切点会匹配通知所要织入的一个或者多个连接点。

**引入：**引入允许我们向现有的类中添加方法或属性。

**织入：**织入是将切面应用到目标对象来创建的代理对象过程。

切面在指定的连接点被织入到目标对象中，在目标对象的生命周期中有多个点可以织入

> 1. 编译期——切面在目标类编译时期被织入，这种方式需要特殊编译器。AspectJ的织入编译器就是以这种方式织入切面。
>
> 2. 类加载期——切面在类加载到JVM ，这种方式需要特殊的类加载器，他可以在目标类被引入应用之前增强该目标类的字节码。AspectJ5 的 LTW 就支持这种织入方式
>
> 3. 运行期——切面在应用运行期间的某个时刻被织入。一般情况下，在织入切面时候，AOP 容器会为目标对象动态的创建代理对象。Spring AOP 就是以这种方式织入切面。

Spring AOP 与AspectJ的比较在网上有很多的文章，这里就不在详细叙述。有兴趣的看官可以前往地址 [比较分析 Spring AOP 和 AspectJ 之间的差别](http://blog.csdn.net/a128953ad/article/details/50509437)、[比较分析 Spring AOP 和 AspectJ 之间的差别](http://www.oschina.net/translate/comparative_analysis_between_spring_aop_and_aspectj) 等进行查看。最终云图项目选择了AspectJ作为AOP框架。

# **AspectJ**

[AspcetJ官网](http://www.eclipse.org/aspectj/) 上对AspectJ的介绍是一种Java平台兼容以及容易学习和使用的无缝的面向方面的扩展java编程语言。

直接上例子来看看ApectJ是如何使用的吧。

## **AspectJ的Android示例**

实例的代码可以在【这里】获取，以下代码基本将截图方式展现；

#### 引入AspectJ 依赖包

在app下的gradle文件中添加依赖。

![](/assets/import1.png)

#### 引入AspectJ gradle插件以及gradle task

在项目下的gradle文件中添加AspectJ的gradle插件。PS:这个插件目前发布在jcenter上所以一定要加入`jcenter()`

![](/assets/import3.png)

并且在在app下的gradle文件中添加Task。

```js
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main

final def log = project.logger
final def variants = project.android.applicationVariants
variants.all { variant ->
if \(!variant.buildType.isDebuggable\(\)\) {

    log.debug\("Skipping non-debuggable build type '${variant.buildType.name}'."\)

    return;

}



JavaCompile javaCompile = variant.javaCompile

javaCompile.doLast {

    String\[\] args = \["-showWeaveInfo",

                     "-1.5",

                     "-inpath", javaCompile.destinationDir.toString\(\),

                     "-aspectpath", javaCompile.classpath.asPath,

                     "-d", javaCompile.destinationDir.toString\(\),

                     "-classpath", javaCompile.classpath.asPath,

                     "-bootclasspath", project.android.bootClasspath.join\(File.pathSeparator\)\]

    log.debug "ajc args: " + Arrays.toString\(args\)



    MessageHandler handler = new MessageHandler\(true\);

    new Main\(\).run\(args, handler\);

    for \(IMessage message : handler.getMessages\(null, true\)\) {

        switch \(message.getKind\(\)\) {

            case IMessage.ABORT:

            case IMessage.ERROR:

            case IMessage.FAIL:

                log.error message.message, message.thrown

                break;

            case IMessage.WARNING:

                log.warn message.message, message.thrown

                break;

            case IMessage.INFO:

                log.info message.message, message.thrown

                break;

            case IMessage.DEBUG:

                log.debug message.message, message.thrown

                break;

        }

    }

}
}
```

可能会有人奇怪为什么要加入这个gradle的插件并且还要在gradle文件中加入这样一段groove的代码吗？如果对上文中介绍AOP的还有印象，AspectJ是在编译期对目标类编译时期织入切面的，没错，这段代码就是在编译时对项目中的类进行扫描并调用相应的gradle AspectJ插件代码对目标类进行代码插入的。

#### 主工程代码

![](/assets/import4.png)

代码很简单，MainActivity中只有一个Button，设置了OnClickListener，在`onClick`中点击时出现Toast，并且打印出Log。

#### 切面代码

![](/assets/import5.png)

关键的切面代码来了，让我们来仔细解析一下这个类。

首先，在类`ViewOnClickListenerAspectj`上使用了`@Aspect`标签，其实就是声明这个类是一个切面类，需要编译时进行扫描。

方法`onViewClickAOPB`和`onViewClickAOP`上标签`@Before()`和`@After()`则是表示切面中的方法在切点之前还是之后执行，即切面引入的位置。

其次，标签`@Before()` 和`@After()`中的参数`"execution(* android.view.View.OnClickListener.onClick(android.view.View))"`就是切点，具体含义就是在对有执行`android.view.View.OnClickListener.onClick(android.view.View)`方法时切面织入。这个参数的含义就是针对`MainActivity`中的Button点击事件。其含义就是在要执行`android.view.View.OnClickListener.onClick(android.view.View)`方法前插入`onViewClickAOPB`，在`android.view.View.OnClickListener.onClick(android.view.View)`方法之后插入方法`onViewClickAOP;`

示例中也很简单在`onViewClickAOPB`和`onViewClickAOP`上各输出一条Log，运行一下看一下结果。

#### 

#### 结果：![](/assets/import.png)

> 08-09 14:35:22.080 4127-4127/com.nd.aspect.test D/ViewOnClickListenerAj: ViewOnClickListenerAspectj onViewClickAOPB 方法，在onClick之 前 被调用
>
> 08-09 14:35:22.080 4127-4127/com.nd.aspect.test D/MainActivity: 点击了弹出Toast的Button
>
> 08-09 14:35:22.080 4127-4127/com.nd.aspect.test D/ViewOnClickListenerAj: ViewOnClickListenerAspectj onViewClickAOP 方法，在onClick之 后 被调用

仔细看输出发现点击button后先调用了`onViewClickAOPB`方法之后才是`onClick`方法最后是`onViewClickAOP，`这事就是使用AspectJ进行AOP最简单例子，在没有嵌入代码的情况下监听到了Button的点击事件，并在点击事件之前和之后插入了我们的代码。

#### 示例的扩展

以上的示例是最简单的AspectJ的代码示例，查看过程中可能会有一些问题，比如如果有多个Button都监听了OnClickListener，如何区分呢？方法的参数如何获取呢？

接下来的扩展示例就是针对这些疑问书写的，由于是扩展的示例，就不在重复之前的步骤，直接给出MainActivity代码和切面类的代码。

`MainActivity.java`

![](/assets/import6.png)

切面类`ViewOnClickListenerAspectj.java`

![](/assets/import7.png)

运行结果：

点击Button1的日志输出

![](/assets/import10.png)

点击Button2的日志输出![](/assets/import9.png)

方法onViewClickAOPB和onViewClickAOP上有一个参数JoinPoint，其中就包含了切点方法上的参数列表，而且不仅如此，通过JoinPoint还能获取例如方法的注解等信息。

更多AspectJ的使用知识可以进行百度，这里就不一一叙述了。


