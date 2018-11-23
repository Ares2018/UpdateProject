package cn.daily.news.update.notify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.zjrb.core.utils.DownloadUtil;
import com.zjrb.core.utils.SettingManager;
import com.zjrb.core.utils.UIUtils;

import cn.daily.news.update.Constants;
import cn.daily.news.update.R;
import cn.daily.news.update.UpdateManager;
import cn.daily.news.update.util.DownloadManager;

/**
 * Created by lixinke on 2017/12/28.
 */

public class NotifyDownloadManager {
    private static final int NOTIFY_PROGRESS_ID = 11111;
    private static final long UPDATE_DURATION_TIME = 500;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private long mUpdateTime;
    private cn.daily.news.update.util.DownloadManager mDownloadUtil;
    private String mLastVersion;
    private String mLastVersionCode;
    private String mApkUrl;
    private Context mContext;

    public NotifyDownloadManager(Context context, DownloadManager downloadUtil, String version, String apkUrl, int versionCode) {
        mContext = context;
        mDownloadUtil = downloadUtil;
        mLastVersion = version;
        mLastVersionCode = String.valueOf(versionCode);
        mApkUrl = apkUrl;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1", mContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(mContext, "1");
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
    }

    public void startDownloadApk() {
        mBuilder.setContentTitle(UIUtils.getString(R.string.app_name));
        mBuilder.setContentText("更新" + mContext.getString(R.string.app_name) + "到" + mLastVersion);
        mBuilder.setProgress(0, 0, true);
        mNotificationManager.notify(NOTIFY_PROGRESS_ID, mBuilder.build());

        mUpdateTime = System.currentTimeMillis();

        mDownloadUtil.setListener(new MyOnDownloadListener()).download(mApkUrl);
    }

    private class MyOnDownloadListener implements DownloadManager.OnDownloadListener {
        @Override
        public void onLoading(int progress) {
            if (System.currentTimeMillis() - mUpdateTime < UPDATE_DURATION_TIME) {
                return;
            }
            mUpdateTime = System.currentTimeMillis();
            mBuilder.setAutoCancel(false);
            mBuilder.setProgress(100, progress, false);
            mNotificationManager.notify(NOTIFY_PROGRESS_ID, mBuilder.build());
        }

        @Override
        public void onSuccess(String path) {
            Intent data = new Intent(mContext, UpdateReceiver.class);
            data.setAction(Constants.Action.DOWNLOAD_COMPLETE);
            data.putExtra(Constants.Key.APK_URL, mApkUrl);
            data.putExtra(Constants.Key.APK_PATH, path);
            data.putExtra(Constants.Key.APK_VERSION_CODE, mLastVersionCode);

            PendingIntent intent = PendingIntent.getBroadcast(mContext, 100, data, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(intent);
            mBuilder.setContentText(UIUtils.getString(R.string.download_complete_tip)).setProgress(0, 0, false);
            mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
            mBuilder.setAutoCancel(true);
            mNotificationManager.notify(NOTIFY_PROGRESS_ID, mBuilder.build());

            mContext.sendBroadcast(data);
        }

        @Override
        public void onFail(String err) {

            Intent data = new Intent(mContext, UpdateReceiver.class);
            data.setAction(Constants.Action.DOWNLOAD_RETRY);
            data.putExtra(Constants.Key.APK_URL, mApkUrl);
            data.putExtra(Constants.Key.APK_VERSION_NAME, mLastVersion);

            PendingIntent intent = PendingIntent.getBroadcast(mContext, 100, data, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentText(UIUtils.getString(R.string.download_error_tip)).setProgress(0, 0, false);
            mBuilder.setContentIntent(intent);
            mBuilder.setSmallIcon(android.R.drawable.stat_notify_error);
            mBuilder.setAutoCancel(true);
            mNotificationManager.notify(NOTIFY_PROGRESS_ID, mBuilder.build());
        }
    }
}
