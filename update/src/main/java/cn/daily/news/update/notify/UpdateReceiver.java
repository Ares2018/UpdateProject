package cn.daily.news.update.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.zjrb.core.utils.DownloadUtil;
import com.zjrb.core.utils.SettingManager;
import com.zjrb.core.utils.UIUtils;

import cn.daily.news.update.Constants;
import cn.daily.news.update.R;
import cn.daily.news.update.UpdateManager;
import cn.daily.news.update.util.DownloadManager;

/**
 * Created by lixinke on 2017/10/23.
 */

public class UpdateReceiver extends BroadcastReceiver {
    private DownloadManager mDownloadUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.Action.DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            String path = intent.getStringExtra(Constants.Key.APK_PATH);
            String versionCode = intent.getStringExtra(Constants.Key.APK_VERSION_CODE);
            String cachePath = null;
            try {
                cachePath = Uri.parse(path).buildUpon().appendQueryParameter(Constants.Key.APK_VERSION_CODE, String.valueOf(versionCode)).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SettingManager.getInstance().setApkCachePath(cachePath);
            UpdateManager.installApk(context, path);
        } else if (Constants.Action.DOWNLOAD_RETRY.equals(intent.getAction())) {
            String url = intent.getStringExtra(Constants.Key.APK_URL);
            String versionName = intent.getStringExtra(Constants.Key.APK_VERSION_NAME);
            int versionCode = intent.getIntExtra(Constants.Key.APK_VERSION_CODE, 0);
            mDownloadUtil = DownloadManager.get()
                    .setDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath())
                    .setFileName(UIUtils.getString(R.string.app_name) + ".apk");
            new NotifyDownloadManager(context,mDownloadUtil, versionName, url, versionCode).startDownloadApk();
        }
    }
}
