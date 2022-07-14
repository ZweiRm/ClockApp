# ClockApp
一个可以开启浮窗的安卓钟表应用。  
包含：Handler 驱动的自定义钟表 View; TimerTask 钟表 View（未被使用）; 浮窗 Service; 浮窗权限判断 Dialog.  

在 MainActivity 中放置了钟表 View 和开启关闭浮窗按钮。点按开启浮窗按钮可以开启浮窗，浮窗可以悬浮在其他应用之上；点按关闭浮窗可以关闭浮窗。  
未开启浮窗权限时点击开启浮窗则引导用户开启权限。  
浮窗可以任意移动。  
