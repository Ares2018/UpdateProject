package cn.daily.news.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.zjrb.core.common.manager.APICallManager;
import com.zjrb.core.utils.SettingManager;

import java.io.File;

import cn.daily.news.update.listener.OnOperateListener;
import cn.daily.news.update.listener.OnUpdateListener;
import cn.daily.news.update.task.UpdateTask;
import cn.daily.news.update.ui.ForceUpdateDialog;
import cn.daily.news.update.ui.PreloadUpdateDialog;
import cn.daily.news.update.ui.UpdateDialogFragment;

/**
 * Created by lixinke on 2017/8/30.
 */

public class UpdateManager {
    private static String TAG_TASK = "tag_task_update";
    private static String MIME_APK = "application/vnd.android.package-archive";

    private static UpdateManager sInstance = new UpdateManager();
    private OnUpdateListener mOnUpdateListener;
    private OnOperateListener mOnOperateListener;
    private int mLayoutId = 0;
    private int mVersionCode = 0;


    public static UpdateManager getInstance() {
        return sInstance;
    }

    /**
     * 检测更新，浙江新闻使用
     *
     * @param activity
     */
    public void checkUpdate(AppCompatActivity activity) {
        UpdateTask.createTask(activity, null, TAG_TASK);
    }

    /**
     * 取消网络请求
     */
    public void cancel() {
        APICallManager.get().cancel(TAG_TASK);
    }

    /**
     * 对外检测更新
     *
     * @param activity
     * @param bean
     */
    public void checkUpdate(AppCompatActivity activity, VersionBean bean) {
        checkData(activity, bean);
    }


    /**
     * 根据VersionBean显示不同的更新提示框
     *
     * @param activity
     * @param versionBean
     */
    private void checkData(AppCompatActivity activity, VersionBean versionBean) {

        if (VersionCompareUtils.compareVersionName(getVersionName(activity), versionBean.version) == -1) {
            UpdateDialogFragment updateDialogFragment;
            if (versionBean.force_upgraded) {
                updateDialogFragment = new ForceUpdateDialog();
            } else {
                String cachePath = getPreloadApk(getVersionCode(activity));
                if (!TextUtils.isEmpty(cachePath)) {
                    updateDialogFragment = new PreloadUpdateDialog();
                } else {
                    updateDialogFragment = new UpdateDialogFragment();
                }
            }
            Bundle args = new Bundle();
            args.putSerializable(Constants.Key.UPDATE_INFO, versionBean);
            updateDialogFragment.setArguments(args);
            updateDialogFragment.show(activity.getSupportFragmentManager(), "updateDialog");
        }

        if (mOnUpdateListener != null) {
            UpdateResponse.DataBean dataBean = new UpdateResponse.DataBean();
            dataBean.latest = versionBean;
            mOnUpdateListener.onUpdate(dataBean);
        }
    }

    //TODO 当前的问题是包没有完全下载成功时也会认为是可安装包。

    /**
     * 获取预加载的包，防止重复下载
     *
     * @param versionCode
     * @return
     */
    public String getPreloadApk(int versionCode) {
        try {
            String path = SettingManager.getInstance().getApkCachePath();
            if (!TextUtils.isEmpty(path)) {
                Uri uri = Uri.parse(path);
                String code = uri.getQueryParameter(Constants.Key.APK_VERSION_CODE);
                if (Integer.parseInt(code) < versionCode) {
                    return null;
                }
                path = uri.getPath();
                File file = new File(path);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 下载完成后后安装APK命令
     *
     * @param context
     * @param path
     */
    public static void installApk(Context context, String path) {
        File file = new File(path);
        if (file != null && file.exists()) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //兼容7.0私有文件权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.setDataAndType(apkUri, MIME_APK);
            } else {
                install.setDataAndType(Uri.fromFile(file), MIME_APK);
            }
            context.startActivity(install);
        }
    }

    private String getVersionName(AppCompatActivity activity) {
        String versionName = "5.0";
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            if (packageInfo != null) {
                versionName = packageInfo.versionName;
            }
        } catch (Exception e) {
            Log.e("Update", "get version code error", e);
        }
        return versionName;
    }

    /**
     * 获取当前应用的版本号
     *
     * @param context
     * @return
     */
    public int getVersionCode(Context context) {
        if (context == null) {
            return 0;
        }
        if (mVersionCode > 0) {
            return mVersionCode;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                mVersionCode = packageInfo.versionCode;
            }
        } catch (Exception e) {
            Log.e("Update", "get version code error", e);
        }
        return mVersionCode;
    }


    public synchronized OnUpdateListener getOnUpdateListener() {
        return mOnUpdateListener;
    }

    public synchronized OnOperateListener getOnOperateListener() {
        return mOnOperateListener;
    }

    public synchronized void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        mOnUpdateListener = onUpdateListener;
    }

    public synchronized void setOnOperateListener(OnOperateListener onOperateListener) {
        mOnOperateListener = onOperateListener;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    public UpdateManager setLayoutId(int layoutId) {
        mLayoutId = layoutId;
        return this;
    }
}
