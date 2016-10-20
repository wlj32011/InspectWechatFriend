package com.wanglj.inspectwechatfriend.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

/**
 * Created by wanglj on 16/10/20.
 */

public class Utils {

    /**
     * 获取微信的版本号
     * @param context
     * @return
     */
    public static String getVersion(Context context){
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);

        for(PackageInfo packageInfo:packageInfoList){
            if("com.tencent.mm".equals(packageInfo.packageName)){
                return packageInfo.versionName;
            }
        }
        return "6.3.25";
    }
}
