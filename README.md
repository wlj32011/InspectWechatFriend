###前言
最近在研究Android辅助服务，实现了这个小工具，也算是对最近学习的一个总结。

###原理
通过Android 无障碍辅助功能实现模拟点击控件来实现
检查被删好友有两种方法：
1.  向好友发送一条消息，如果对方已经把你删除，则消息发送失败。
2.  建群法：新建一个不大于40人的群，如果其中有好友已经把你删除，微信会有条消息提示
3.  整体执行步骤：启动微信->点击+号->发起群聊->选择35个联系人->点击确定->点击群里详情->删除并退出，依次轮询执行，知道所有好友轮询结束。




![Paste_Image.png](http://upload-images.jianshu.io/upload_images/2326742-275c88921c422c65.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


本文采用建群的方式进行检查。
本人微信有300好友，全部检测一遍只需3分钟即可，亲测已经成功，
但是建群没有超过40人 会有个别好友会受到打扰消息，可能是微信哪里的bug，具体原因未知。



###说明和app预览
此软件通过无障碍辅助进行模拟点击，无任何外挂木马，无封号风险

![Paste_Image.png](http://upload-images.jianshu.io/upload_images/2326742-524e1335e3f233f2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![Paste_Image.png](http://upload-images.jianshu.io/upload_images/2326742-9434b604b85ae87f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



###使用方法
1.  Android 手机一部，登录微信账号
2.  安装辅助软件apk下载地址请点击[这里](https://raw.githubusercontent.com/wlj32011/InspectWechatFriend/master/app-release.apk)
3.  打开辅助软件-点击打开辅助功能按钮，跳转到无障碍辅助设置把辅助开关打开。
4.  点击开始检查按钮，开始一系列的模拟点击，检查完成后会跳转到一个列表会把被删好友列表展示出来。




###实现步骤：






*  新建Android Studio 工程，新建一个Services类集成AccessibilityService,实现对应方法，详细介绍见代码注释

```java

/**
 * Created by wanglj on 16/10/20.
 */

public class InspectWechatFriendService extends AccessibilityService{


    @Override
    protected void onServiceConnected() {//辅助服务被打开后 执行此方法
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {//监听手机当前窗口状态改变 比如 Activity 跳转,内容变化,按钮点击等事件

    }

    @Override
    public void onInterrupt() {//辅助服务被关闭 执行此方法

    }
}

```

* 在manifests.xml文件中注册此服务：


```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest package="com.wanglj.inspectwechatfriend"
          xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>

                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>


        <service
            android:name=".accessibility.InspectWechatFriendService"
            android:enabled="true"
            android:exported="true"
            android:label="@string/app_name"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService"/>
            </intent-filter>

            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/inspect_wechat_friend"/>
        </service>

    </application>

</manifest>

```

* 新建res/xml/inspect_wechat_friend.xml文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
                       android:accessibilityEventTypes="typeWindowStateChanged" 
                       android:accessibilityFeedbackType="feedbackGeneric"
                       android:accessibilityFlags=""
                       android:canRetrieveWindowContent="true"
                       android:notificationTimeout="100"
                       android:description="@string/accessibility"

    />
```

* 实现对某个控件的点击

通过getRootInActiveWindow方法获取当前窗口信息，通过findAccessibilityNodeInfosByText方法找到当前对应控件进行模拟点击

```java

public class PerformClickUtils {


    /**
     * 在当前页面查找文字内容并点击
     *
     * @param text
     */
    public static void findTextAndClick(AccessibilityService accessibilityService,String text) {

        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityService.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (text.equals(nodeInfo.getText()) || text.equals(nodeInfo.getContentDescription()))) {
                    performClick(nodeInfo);
                    break;
                }
            }
        }
    }


    /**
     * 检查viewId进行点击
     * 
     * @param accessibilityService
     * @param id
     */
    public static void findViewIdAndClick(AccessibilityService accessibilityService,String id) {

        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityService.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performClick(nodeInfo);
                    break;
                }
            }
        }
    }


    /**
     * 在当前页面查找对话框文字内容并点击
     *
     * @param text1 默认点击text1
     * @param text2
     */
    public static void findDialogAndClick(AccessibilityService accessibilityService,String text1, String text2) {

        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityService.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> dialogWait = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text1);
        List<AccessibilityNodeInfo> dialogConfirm = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text2);
        if (!dialogWait.isEmpty() && !dialogConfirm.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : dialogWait) {
                if (nodeInfo != null && text1.equals(nodeInfo.getText())) {
                    performClick(nodeInfo);
                    break;
                }
            }
        }

    }

    //模拟点击事件
    public static void performClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(nodeInfo.getParent());
        }
    }

    //模拟返回事件
    public static void performBack(AccessibilityService service) {
        if (service == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        }
    }
}

```

* 监听窗口事件

获取当前窗口的classname 通过classname进行判断当前手机处于某个界面
下面代码逻辑：
1.  如果当前为微信主页面，则点击+号然后点击发起群聊
2.  如果当前页面为创建群聊选择联系人界面，则开启一个while循环模拟滚动时间以及点击选择框，当选择用户到39人时，则模拟点击确定按钮发起群聊。
3.  发起群聊后，微信会返回哪些用户不是你的好友，这个时候，取到当前控件的字符串并截取用户列表保存到本地。
4.  获取到不是好友的用户后，点击右上角进入群聊详情，点击删除并退出
5.  退出后又回到微信主页面，依次执行1 2 3 4步骤，直到滚动到联系人最底部为止。
6.  当所有用户执行完成后，则启动检查结果界面，列出所有被删好友。

下面为对应逻辑代码：

```java
 @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {//监听手机当前窗口状态改变 比如 Activity 跳转,内容变化,按钮点击等事件

        //如果手机当前界面的窗口发送变化
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            //获取当前activity的类名:
            String currentWindowActivity = accessibilityEvent.getClassName().toString();
            if(!hasComplete){
                if ("com.tencent.mm.ui.contact.SelectContactUI".equals(currentWindowActivity)) {
                    canChecked = true;
                    createGroup();
                } else if ("com.tencent.mm.ui.chatting.ChattingUI".equals(currentWindowActivity)) {
                    getDeleteFriend();
                } else if ("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI".equals(currentWindowActivity)) {
                    deleteGroup();
                }else if("com.tencent.mm.ui.LauncherUI".equals(currentWindowActivity)){
                    PerformClickUtils.findTextAndClick(this,"更多功能按钮");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PerformClickUtils.findTextAndClick(this,"发起群聊");
                }
            }else{
                nickNameList.clear();
                deleteList.clear();
                sortItems.clear();
                startActivity(new Intent(this, DeleteFriendListActivity.class));
            }
        }

    }


    /**
     * 模拟创建群组步骤
     */
    private void createGroup() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> listview = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_listview_id);
        int count = 0;

        if (!listview.isEmpty()) {
            while (canChecked) {
                List<AccessibilityNodeInfo> checkboxList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_checkbox_id);
                List<AccessibilityNodeInfo> sortList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_sortitem_id);

                for(AccessibilityNodeInfo nodeInfo:sortList){
                    if(nodeInfo != null && nodeInfo.getText()!= null){
                        sortItems.add(nodeInfo.getText().toString());
                    }

                }

                for (AccessibilityNodeInfo nodeInfo : checkboxList) {

                    String nickname = nodeInfo.getParent().findAccessibilityNodeInfosByViewId(selectUI_nickname_id).get(0).getText().toString();
                    Log.e(TAG, "nickname = " + nickname);
                    if (!nickNameList.contains(nickname)) {
                        nickNameList.add(nickname);
                        performClick(nodeInfo);
                        count++;
                        if (count >= GROUP_COUNT || nickNameList.size() >= listview.get(0).getCollectionInfo().getRowCount() - sortItems.size() - 2) {

                            List<AccessibilityNodeInfo> createButtons = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_create_button_id);
                            if (!createButtons.isEmpty()) {
                                performClick(createButtons.get(0));
                            }


                            if(nickNameList.size() >= listview.get(0).getCollectionInfo().getRowCount() - sortItems.size() - 2){
                                hasComplete = true;

                            }
                            return;
                        }
                    }
                }

                listview.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    /**
     * 模拟获取被删好友列表步骤
     */
    private void getDeleteFriend() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(chattingUI_message_id);

        for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo != null && nodeInfo.getText() != null && nodeInfo.getText().toString().contains("你无法邀请未添加你为好友的用户进去群聊，请先向")) {
                String str = nodeInfo.getText().toString();
                str = str.replace("你无法邀请未添加你为好友的用户进去群聊，请先向", "");
                str = str.replace("发送朋友验证申请。对方通过验证后，才能加入群聊。", "");
                String[] arr = str.split("、");
                deleteList.addAll(Arrays.asList(arr));
                Preferences.saveDeleteFriends(this);

                Log.e(TAG, "deleteList.size():" + deleteList.size());
                Toast.makeText(this, "僵尸粉数量:" + deleteList.size(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
        PerformClickUtils.findTextAndClick(this,"聊天信息");


    }

    /**
     * 退出群组步骤
     */
    private void deleteGroup(){
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(groupinfoUI_listview_id);
        if (!nodeInfoList.isEmpty()) {

            nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            PerformClickUtils.findTextAndClick(this,"删除并退出");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PerformClickUtils.findTextAndClick(this,"删除并退出");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if(Utils.getVersion(this).equals(WECHAT_VERSION_27)){
//                PerformClickUtils.findTextAndClick(this,"确定");
//            }else{
                PerformClickUtils.findTextAndClick(this,"离开群聊");
                PerformClickUtils.findTextAndClick(this,"确定");
//            }



        }
    }
```


###ui automator viewer的使用
uiautomatorviewer可以检查当前手机的布局结构，如果想更精确的找到控件位置，uiautomatorviewer必不可少！

使用方法：
1.  搭建Android开发环境，并设置环境变量，这里就不说了。
2.  在Android Studio 中打开 terminal 窗口,或者在终端直接执行命令

```
$uiautomatorviewer
```

整体效果图：

![Paste_Image.png](http://upload-images.jianshu.io/upload_images/2326742-13f5c0dd0fbecd16.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


项目源码github地址：https://github.com/wlj32011/InspectWechatFriend

支持原创，拒绝盗窃，如果此文章帮助到了你，就打赏一下吧~~~

微信号：wlj3203  
Android技术交流QQ群：10908959   欢迎加入

博客地址:http://www.jianshu.com/users/67db2debac96/latest_articles

声明：此项目请不要用于商业用途，若有侵权，均与本人无关~










