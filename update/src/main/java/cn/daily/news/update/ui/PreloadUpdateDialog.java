package cn.daily.news.update.ui;

import android.view.View;

import cn.daily.news.update.UpdateManager;

/**
 * Created by lixinke on 2017/10/19.
 */

public class PreloadUpdateDialog extends UpdateDialogFragment {
    @Override
    protected String getOKText() {
        return "安装";
    }

    @Override
    public void updateApk(View view) {
        installPreloadApk();
    }

    private void installPreloadApk() {
        UpdateManager.installApk(getContext(), UpdateManager.getInstance().getPreloadApk(UpdateManager.getInstance().getVersionCode(getContext())));
    }

    @Override
    protected String getTitle() {
        return "已在WIFI下为您预下载了最新版本"+mLatestBean.version+"(版本号),是否立即更新?";
    }
}
