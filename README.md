# 检测更新使用手册
## 说明
检测更新是一个业务模块，是基于浙江新闻的core-library进行开发，主要使用了core中的下载模块和权限请求模块和浙江新闻业务没有太大耦合性。其他APP可以如果有相似的功能可以直接使用。

## 功能介绍
1. 通知栏显示APK下载进度，下载完成后自动进入安装页面。
2. WIFI下自动缓存APK，下次进来直接进行安装。

显示UI有一下几种类型：

1. 正常更新提示，会展示更新信息。
2. 强制更新，如果用户不进行更新无法进入页面。
3. 移动网络下更新，会提示用户当前为移动网络需要使用移动流量，让用户判断是否更新。

## 使用方法
1. 添加依赖

	检测更新依赖的其他模块和版本号如下，如果出现依赖冲突，可以使用 `exclude group:"cn.daily.android",module:"core-library"` 语法排除
	
	```
	compile 'cn.daily.android:core-library:5.3.2.2-SNAPSHOT'    
	compile 'com.android.support:appcompat-v7:26.1.0'
	```
	
	
	主工程build.gradle添加仓库地址:
	
	```
	allprojects {
	    repositories {
	        maven { url "http://10.100.62.98:8086/nexus/content/groups/public" }
	    }
	}
	```
	
	项目build.gradle添加依赖依赖 [最新版本](http://10.100.62.98:8086/nexus/#nexus-search;gav~cn.daily.android~update~~~~kw,versionexpand)
	
	```
	compile 'cn.daily.android:update:5.3.1.12-SNAPSHOT'
	```
	

1. 构造请求数据结构，数据结构说明如下
	
	```
	public class VersionBean implements Serializable {
	    /**
	     * 最新版本号versionName
	     */
	    public String version;
	    /**
	     * 最新版本code versionCode
	     */
	    public int version_code;
	    /**
	     * 最新APK下载地址
	     */
	    public String pkg_url;
	    /**
	     * 是否强制更新
	     */
	    public boolean force_upgraded;
	    /**
	     * 更新信息说明
	     */
	    public String remark;
	}
	```

2. 调用方法进行检测`UpdateManager.getInstance().checkUpdate(activity, bean);`

	```
	  /**
	     * 检测更新
	     * @param appCompatActivity 获取getSupportFragmentManager
	     * @param latest_version 检测更新数据结构
	     */
	     public void checkUpdate(AppCompatActivity activity, VersionBean bean){};
	```
3. 按钮点击事件监听

	```
	/**
	 * 检测更新操作符监听
	 */
	public interface OnOperateListener {
	    /**
	     * 检测更新按钮操作符
	     *
	     * @param type 更新类型 {@link UpdateType#NON_WIFI,UpdateType#NORMAL,UpdateType#FORCE}
	     * @param id   按钮ID{update_ok,update_cancel}
	     */
	    void onOperate(UpdateType type, int id);
	}
	```
	
4. 定制UI

	检测更新保留 更新(@id/update_ok)、取消(@id/update_cancel)和名称(@id/update_title)三个按钮，其余部分可以自行定制
	`        UpdateManager.getInstance().setLayoutId(R.layout.custom_update_layout).checkUpdate(activity, bean);
`
	
