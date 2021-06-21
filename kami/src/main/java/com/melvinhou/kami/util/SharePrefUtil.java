package com.melvinhou.kami.util;

import android.content.SharedPreferences;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p/>
 * = 版 权 所 有：7416064@qq.com
 * <p/>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p/>
 * = 时 间：2016/5/6 12:21
 * <p/>
 * = 分 类 说 明：SharePreferences操作工具类
 * ================================================
 */
public class SharePrefUtil {
	/*操作工具类*/
	private static SharedPreferences sp;

	/**
	 * @return 获取对象
	 */
	public static SharedPreferences getSp(){
		return getSp("config");
	}
	public static SharedPreferences getSp(String spName){
		if (sp == null)
			sp = FcUtils.getContext().getSharedPreferences(spName, 0);
		return sp;
	}

	/**
	 * 保存布尔值
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveBoolean(String key, boolean value) {
		getSp().edit().putBoolean(key, value).commit();
	}

	/**
	 * 保存字符串
	 *
	 * @param key
	 * @param value
	 */
	public static void saveString(String key, String value) {
		getSp().edit().putString(key, value).commit();
	}

	public static void clear() {
		getSp().edit().clear().commit();
	}

	/**
	 * 保存long型
	 *
	 * @param key
	 * @param value
	 */
	public static void saveLong(String key, long value) {
		getSp().edit().putLong(key, value).commit();
	}

	/**
	 * 保存int型
	 *
	 * @param key
	 * @param value
	 */
	public static void saveInt(String key, int value) {
		getSp().edit().putInt(key, value).commit();
	}

	/**
	 * 保存float型
	 *
	 * @param key
	 * @param value
	 */
	public static void saveFloat(String key, float value) {
		getSp().edit().putFloat(key, value).commit();
	}

	/**
	 * 获取字符值
	 *
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static String getString(String key, String defValue) {
		return getSp().getString(key, defValue);
	}

	/**
	 * 获取int值
	 *
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static int getInt(String key, int defValue) {
		return getSp().getInt(key, defValue);
	}

	/**
	 * 获取long值
	 *
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static long getLong(String key, long defValue) {
		return getSp().getLong(key, defValue);
	}

	/**
	 * 获取float值
	 *
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static float getFloat(String key, float defValue) {
		return getSp().getFloat(key, defValue);
	}

	/**
	 * 获取布尔值
	 *
	 * @param key
	 * @param defValue
	 * @return
	 */
	public static boolean getBoolean(String key, boolean defValue) {
		return getSp().getBoolean(key, defValue);
	}

}
