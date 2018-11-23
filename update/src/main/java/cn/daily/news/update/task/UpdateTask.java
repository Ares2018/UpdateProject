package cn.daily.news.update.task;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.zjrb.core.api.base.APIGetTask;
import com.zjrb.core.api.callback.APICallBack;

import java.util.List;

import cn.daily.news.update.listener.OnUpdateListener;
import cn.daily.news.update.UpdateManager;
import cn.daily.news.update.UpdateResponse;

public class UpdateTask extends APIGetTask<UpdateResponse.DataBean> {
    private static String DEFAULT_URL = "/api/app_version/detail";
    private String mUrl;

    public static void createTask(AppCompatActivity activity, String url, String tag) {
        new UpdateTask(url, new CheckUpdateCallBack(activity)).setTag(tag).exe();
    }

    public UpdateTask(String url, APICallBack<UpdateResponse.DataBean> callback) {
        super(callback);
        mUrl = url;
    }

    @Override
    protected void onSetupParams(Object... params) {

    }

    @Override
    protected String getApi() {
        return TextUtils.isEmpty(mUrl) ? DEFAULT_URL : mUrl;
    }

    private static class CheckUpdateCallBack extends APICallBack<UpdateResponse.DataBean> {
        private final AppCompatActivity mActivity;
        private List<OnUpdateListener> mListeners;

        public CheckUpdateCallBack(AppCompatActivity activity) {
            mListeners = UpdateManager.getInstance().getOnUpdateListeners();
            mActivity = activity;
        }

        @Override
        public void onSuccess(UpdateResponse.DataBean data) {
            if (data == null || data.latest == null) {
                if (mListeners != null && mListeners.size() > 0) {
                    for (OnUpdateListener listener : mListeners) {
                        listener.onError("服务端返回错误!", -1);
                    }
                }
                return;
            }
            UpdateManager.getInstance().checkUpdate(mActivity, data.latest);
        }

        @Override
        public void onError(String errMsg, int errCode) {
            super.onError(errMsg, errCode);
            if (mListeners != null && mListeners.size() > 0) {
                for (OnUpdateListener listener : mListeners) {
                    listener.onError("服务端返回错误!", -1);
                }
            }
        }
    }
}
