package cn.daily.news.update.ui;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.zjrb.core.common.permission.AbsPermSingleCallBack;
import com.zjrb.core.common.permission.IPermissionOperate;
import com.zjrb.core.common.permission.Permission;
import com.zjrb.core.common.permission.PermissionManager;
import com.zjrb.core.ui.widget.dialog.LoadingIndicatorDialog;
import com.zjrb.core.utils.DownloadUtil;
import com.zjrb.core.utils.NetUtils;
import com.zjrb.core.utils.SettingManager;
import com.zjrb.core.utils.UIUtils;

import java.util.List;

import cn.daily.news.update.Constants;
import cn.daily.news.update.listener.OnOperateListener;
import cn.daily.news.update.notify.NotifyDownloadManager;
import cn.daily.news.update.R;
import cn.daily.news.update.UpdateManager;
import cn.daily.news.update.VersionBean;
import cn.daily.news.update.type.UpdateType;
import cn.daily.news.update.util.DownloadManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateDialogFragment extends DialogFragment implements IPermissionOperate, View.OnClickListener {
    private TextView mTitleView;
    protected TextView mRemarkView;
    private View mCancelView;
    protected TextView mOkView;
    protected VersionBean mLatestBean;
    private DownloadManager mDownloadManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        int layoutId = UpdateManager.getInstance().getLayoutId();
        if (layoutId == 0) {
            layoutId = R.layout.fragment_update_dialog;
        }

        View rootView = inflater.inflate(layoutId, container, false);
        initUI(rootView);
        if (getArguments() != null) {
            mLatestBean = (VersionBean) getArguments().getSerializable(Constants.Key.UPDATE_INFO);
            if (mLatestBean != null && !TextUtils.isEmpty(getRemark())) {
                mRemarkView.setText(Html.fromHtml(getRemark()));
            } else {
                mRemarkView.setText("有新版本请更新!");
            }
            mOkView.setText(getOKText());
            mTitleView.setText(getTitle());
        }
        setCancelable(false);
        PermissionManager.get().request(this, new AbsPermSingleCallBack() {
            @Override
            public void onGranted(boolean isAlreadyDef) {
                initDownload();
            }

            @Override
            public void onDenied(List<String> neverAskPerms) {

            }
        }, Permission.STORAGE_WRITE, Permission.STORAGE_READE);
        return rootView;
    }

    private void initUI(View rootView) {
        mTitleView = rootView.findViewById(R.id.update_title);
        mRemarkView = rootView.findViewById(R.id.update_remark);
        mRemarkView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mOkView = rootView.findViewById(R.id.update_ok);
        mCancelView = rootView.findViewById(R.id.update_cancel);
        mOkView.setOnClickListener(this);
        mCancelView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.update_ok) {
            updateApk(v);
        } else if (id == R.id.update_cancel) {
            cancelUpdate(v);
        }
    }

    protected void hideCancel() {
        mCancelView.setVisibility(View.GONE);
    }

    protected String getTitle() {
        return "检测到新版本V" + mLatestBean.version + "(版本号),立即更新?";
    }

    protected void setTitleVisible(int visible) {
        mTitleView.setVisibility(visible);
    }

    protected String getRemark() {
        return mLatestBean.remark;
    }

    protected String getOKText() {
        return "更新";
    }


    public void updateApk(View view) {
        if (NetUtils.isWifi()) {
            downloadApk();
            if (UpdateManager.getInstance().getOnOperateListeners() != null && UpdateManager.getInstance().getOnOperateListeners().size() > 0) {
                for (OnOperateListener listener : UpdateManager.getInstance().getOnOperateListeners()) {
                    listener.onOperate(UpdateType.NORMAL, R.id.update_ok);
                }
            }
        } else {
            dismissAllowingStateLoss();
            NonWiFiUpdateDialog dialog = new NonWiFiUpdateDialog();
            Bundle args = new Bundle();
            args.putSerializable(Constants.Key.UPDATE_INFO, mLatestBean);
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "updateDialog");
        }
    }


    protected void downloadApk() {

        PermissionManager.get().request(this, new AbsPermSingleCallBack() {
            @Override
            public void onGranted(boolean isAlreadyDef) {
                dismissAllowingStateLoss();
                if (mDownloadManager == null) {
                    initDownload();
                }
                new NotifyDownloadManager(getContext(), mDownloadManager, mLatestBean.version, mLatestBean.pkg_url, mLatestBean.version_code)
                        .startDownloadApk();
            }

            @Override
            public void onDenied(List<String> neverAskPerms) {
                Toast.makeText(getContext(), "请给我写文件的权限", Toast.LENGTH_SHORT).show();
            }
        }, Permission.STORAGE_WRITE, Permission.STORAGE_READE);


    }

    private void initDownload() {
        mDownloadManager = DownloadManager.get()
                .setDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath())
                .setFileName(UIUtils.getString(R.string.app_name) + ".apk");
    }


    public void cancelUpdate(View view) {
        dismissAllowingStateLoss();
        String cachePath = UpdateManager.getInstance().getPreloadApk(UpdateManager.getInstance().getVersionCode(getContext()));
        if (TextUtils.isEmpty(cachePath) && NetUtils.isWifi()) {
            if (mDownloadManager == null) {
                initDownload();
            }
            mDownloadManager.setListener(new DownloadManager.OnDownloadListener() {
                @Override
                public void onLoading(int progress) {
                }

                @Override
                public void onSuccess(String path) {
                    String cachePath = null;
                    try {
                        cachePath = Uri.parse(path).buildUpon().appendQueryParameter(Constants.Key.APK_VERSION_CODE, String.valueOf(mLatestBean.version_code)).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SettingManager.getInstance().setApkCachePath(cachePath);
                }

                @Override
                public void onFail(String err) {
                }
            }).download(mLatestBean.pkg_url);
        }


        if (UpdateManager.getInstance().getOnOperateListeners() != null && UpdateManager.getInstance().getOnOperateListeners().size() > 0) {
            for (OnOperateListener listener : UpdateManager.getInstance().getOnOperateListeners()) {
                listener.onOperate(UpdateType.NORMAL, R.id.update_cancel);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.get().onRequestPermissionsResult(requestCode, permissions,
                grantResults, this);
    }

    @Override
    public void exeRequestPermissions(@NonNull String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public boolean exeShouldShowRequestPermissionRationale(@NonNull String permission) {
        return shouldShowRequestPermissionRationale(permission);
    }
}
