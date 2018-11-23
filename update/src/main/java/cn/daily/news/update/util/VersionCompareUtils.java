package cn.daily.news.update.util;

public class VersionCompareUtils {
    /**
     * 版本比较
     * @param versionName1
     * @param versionName2
     * @return
     */
    public static int compareVersionName(String versionName1, String versionName2) {
        try {
            if (versionName1.equals(versionName2)) {
                return 0;
            }
            String[] version1Array = versionName1.split("\\.");
            String[] version2Array = versionName2.split("\\.");
            int index = 0;
            //获取最小长度值
            int minLen = Math.min(version1Array.length, version2Array.length);
            int diff = 0;
            //循环判断每位的大小
            while (index < minLen && (diff = Integer.parseInt(version1Array[index]) - Integer.parseInt(version2Array[index])) == 0) {
                index++;
            }
            if (diff == 0) {
                //如果位数不一致，比较多余位数
                for (int i = index; i < version1Array.length; i++) {
                    if (Integer.parseInt(version1Array[i]) > 0) {
                        return 1;
                    }
                }

                for (int i = index; i < version2Array.length; i++) {
                    if (Integer.parseInt(version2Array[i]) > 0) {
                        return -1;
                    }
                }
                return 0;
            } else {
                return diff > 0 ? 1 : -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void main(String[] args) {
        String version1="5.2.1";
        String version2="5.2.0";
        System.out.println(compareVersionName(version1, version2));
    }
}
