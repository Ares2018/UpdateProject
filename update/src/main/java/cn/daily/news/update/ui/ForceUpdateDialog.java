package cn.daily.news.update.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zjrb.core.ui.widget.dialog.LoadingIndicatorDialog;
import com.zjrb.core.utils.SettingManager;

import cn.daily.news.update.Constants;
import cn.daily.news.update.R;
import cn.daily.news.update.UpdateManager;
import cn.daily.news.update.listener.OnOperateListener;
import cn.daily.news.update.type.UpdateType;
import cn.daily.news.update.util.DownloadManager;


/**
 * Created by lixinke on 2017/10/19.
 */

public class ForceUpdateDialog extends UpdateDialogFragment implements DownloadManager.OnDownloadListener {
    private LoadingIndicatorDialog mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        setCancelable(false);
        hideCancel();
        return rootView;
    }

    @Override
    public void updateApk(View view) {
        forceDownloadApk();
        if (UpdateManager.getInstance().getOnOperateListeners() != null && UpdateManager.getInstance().getOnOperateListeners().size() > 0) {
            for (OnOperateListener listener : UpdateManager.getInstance().getOnOperateListeners()) {
                listener.onOperate(UpdateType.FORCE, R.id.update_ok);
            }
        }
    }

    @Override
    protected String getOKText() {
        return "更新";
    }

    protected void forceDownloadApk() {
        String cachePath = UpdateManager.getInstance().getPreloadApk(UpdateManager.getInstance().getVersionCode(getContext()));
        if (!TextUtils.isEmpty(cachePath)) {
            mOkView.setText("安装");
            UpdateManager.installApk(getContext(), cachePath);
        } else {
            mOkView.setText("更新");
            mProgressBar = new LoadingIndicatorDialog(getActivity());
            mProgressBar.setCancelable(false);
            mProgressBar.show();
            DownloadManager.get().setListener(this).download(mLatestBean.pkg_url);
        }
    }

    @Override
    public void onLoading(int progress) {

    }

    @Override
    public void onSuccess(String path) {
        UpdateManager.installApk(getContext(), path);
        String cachePath = null;
        try {
            cachePath = Uri.parse(path)
                    .buildUpon()
                    .appendQueryParameter(Constants.Key.APK_VERSION_CODE, String.valueOf(mLatestBean.version_code))
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SettingManager.getInstance().setApkCachePath(cachePath);
        mProgressBar.dismiss();
        mOkView.setEnabled(true);
        mOkView.setText("安装");
    }

    @Override
    public void onFail(String err) {
        mProgressBar.dismiss();
        mRemarkView.setText("更新失败,请稍后再试");
        mRemarkView.setVisibility(View.VISIBLE);
    }
}
