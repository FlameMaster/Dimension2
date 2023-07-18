package com.melvinhou.userlibrary;

import com.melvinhou.kami.io.SharePrefUtil;
import com.melvinhou.userlibrary.bean.User;

import org.jetbrains.annotations.NotNull;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/7/18 0018 13:31
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class UserUtils {

    /**
     * 是否登录
     * @return
     */
    public static boolean isLogin(){
        return SharePrefUtil.getBoolean(UserConstants.KEY_ISLOGIN,false);
    }

    /**
     * 登录成功
     * @param user
     */
    public static void loginComplete(@NotNull User user) {
        SharePrefUtil.saveBoolean(UserConstants.KEY_ISLOGIN,true);
        SharePrefUtil.saveLong(User.USER_ID,user.getUserId());
    }
}
