GetSwitch
=========

通过向指定的服务端发送http请求获取开关数据

1）根据应用id、渠道号来获取相应的开关，结果中返回开关状态及错误消息码
2）开关可以在SwitchCallback(boolean open,int errorCode)中获取到
   open为true表示开关为开，相反false表示开关为关
   errorCode 为0表示成功获取开关，非0表示请求过程中发送错误，详细错误信息可以参考代码部分

3）使用时需要在应用的manifest中添加如下配置：
    <!-- 应用index -->
    <meta-data
        android:name="switch_app_index"
        android:value="1" />
    <!-- 渠道号 -->
    <meta-data
        android:name="switch_app_channel_id"
        android:value="daiji_1000" />
    <!-- 获取开关 end -->
