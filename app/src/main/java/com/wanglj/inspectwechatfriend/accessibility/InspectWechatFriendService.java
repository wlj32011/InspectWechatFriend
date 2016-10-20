package com.wanglj.inspectwechatfriend.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.wanglj.inspectwechatfriend.Preferences;
import com.wanglj.inspectwechatfriend.activity.DeleteFriendListActivity;
import com.wanglj.inspectwechatfriend.utils.PerformClickUtils;
import com.wanglj.inspectwechatfriend.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


import static android.content.ContentValues.TAG;
import static com.wanglj.inspectwechatfriend.utils.PerformClickUtils.performClick;

/**
 * Created by wanglj on 16/10/20.
 */

public class InspectWechatFriendService extends AccessibilityService{

    public static final int GROUP_COUNT = 39;//群组成员个数

    public static final String WECHAT_VERSION_25 = "6.3.25";
    public static final String WECHAT_VERSION_27 = "6.3.27";

    public static List<String> nickNameList = new ArrayList<>();
    public static HashSet<String> deleteList = new HashSet<>();
    public static HashSet<String> sortItems = new HashSet<>();

    public static boolean hasComplete;

    public static boolean canChecked;


    public static String selectUI_listview_id = "com.tencent.mm:id/bd3";
    public static String selectUI_checkbox_id = "com.tencent.mm:id/lg";
    public static String selectUI_sortitem_id = "com.tencent.mm:id/a5s";
    public static String selectUI_nickname_id = "com.tencent.mm:id/ib";
    public static String selectUI_create_button_id = "com.tencent.mm:id/eu";
    public static String chattingUI_message_id = "com.tencent.mm:id/h7";
    public static String groupinfoUI_listview_id = "android:id/list";





    @Override
    protected void onServiceConnected() {//辅助服务被打开后 执行此方法
        super.onServiceConnected();
        Toast.makeText(this, "_已开启检测好友服务_", Toast.LENGTH_LONG).show();

        String wechatVersion = Utils.getVersion(this);
        if(WECHAT_VERSION_25.equals(wechatVersion)){
             selectUI_listview_id = "com.tencent.mm:id/bd3";
             selectUI_checkbox_id = "com.tencent.mm:id/lg";
             selectUI_sortitem_id = "com.tencent.mm:id/a5s";
             selectUI_nickname_id = "com.tencent.mm:id/ib";
             selectUI_create_button_id = "com.tencent.mm:id/eu";
             chattingUI_message_id = "com.tencent.mm:id/h7";
             groupinfoUI_listview_id = "android:id/list";

        }else if(WECHAT_VERSION_27.equals(wechatVersion)){
            selectUI_listview_id = "com.tencent.mm:id/bg3";
            selectUI_checkbox_id = "com.tencent.mm:id/o3";
            selectUI_sortitem_id = "com.tencent.mm:id/a8j";
            selectUI_nickname_id = "com.tencent.mm:id/kz";
            selectUI_create_button_id = "com.tencent.mm:id/fb";
            chattingUI_message_id = "com.tencent.mm:id/ho";
            groupinfoUI_listview_id = "android:id/list";
        }


    }

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










    @Override
    public void onInterrupt() {//辅助服务被关闭 执行此方法
        canChecked = false;
        Toast.makeText(this, "_检测好友服务被中断啦_", Toast.LENGTH_LONG).show();

    }
}
