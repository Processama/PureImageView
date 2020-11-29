# PureImageView
### 功能介绍
* PureImageView是一个ImageView的自定义控件，实现了圆形、圆角ImageView且支持边框，使用简单方便！
* 对于圆形ImageView因为是把控件变成正方形对控件尺寸进行了处理，故不支持Padding属性，圆角ImageView则处理了Padding
* 圆形及圆角ImageView都支持边框，边框可以设置颜色、尺寸
### 效果图
* 圆形ImageView（带边框及不带边框）
![Image text](https://github.com/Processama/PureImageView/blob/master/ScreenShots/CircleWithBorder.jpg)
![Image text](https://github.com/Processama/PureImageView/blob/master/ScreenShots/CircleNoBorder.jpg)
* 圆角ImageView（带边框及不带边框）
![Image text](https://github.com/Processama/PureImageView/blob/master/ScreenShots/CornerWithBorder.jpg)
![Image text](https://github.com/Processama/PureImageView/blob/master/ScreenShots/CornerNoBorder.jpg)
### 控件属性
* imgType: 1.circleImg 2.cornerImg 3.normalImg 指定ImageView类型，分别是圆形、圆角、默认
* cornerRadius: 圆角大小单位dp
* borderWidth: 边框宽度单位dp
* borderColor: 边框颜色
### 如何使用
  #### Step 1.工程目录中build.gradle在repositories字段中末端添加:
 ```java
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' } 
	}
}
  ```
  #### Step 2.app目录中build.gradle在添加依赖:
```java
dependencies {
  implementation 'com.github.Processama:PureImageView:v1.1'
}
```
  #### Step 3.使用该控件指定img类型等属性
```java
<com.example.pureimageview.PureImageView
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="200dp"
  android:layout_height="400dp"
  android:layout_centerInParent="true"
  android:src="@drawable/cat"
  app:imgType="cornerImg"
  app:cornerRadius="6dp"
  app:borderColor="#CF2D73"
  app:borderWidth="2dp"/>
```
### 总结
    关于这个ImageView的介绍就到此为止了哈，使用起来比较简单，后续有bug或新功能再更新哦！！！
![alt text](https://vdn1.vzuu.com/SD/d4122296-2d8c-11eb-a1ac-faaeab4f2b8d.mp4?disable_local_cache=1&bu=pico&expiration=1606633492&auth_key=1606633492-0-0-4af756afe0cf56c46100a677402589a6&f=mp4&v=hw)
    
