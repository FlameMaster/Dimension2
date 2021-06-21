package com.melvinhou.kami.helper;

import android.view.Menu;

import java.lang.reflect.Field;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/3/9 11:27
 * <p>
 * = 分 类 说 明：溢出菜单帮助类
 * ================================================
 */

public class OverflowMenuHelper {


    /**
     * 通过反射的方法将该值mOptionalIconsVisible 设为true，即显示出icon图片
     * @param menu
     */
    public static void disableShiftMode(Menu menu) {
        Field field;
        try {
            field = menu.getClass().getDeclaredField("mOptionalIconsVisible");

            field.setAccessible(true);
            field.set(menu, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
