package com.melvinhou.dimension2;

import com.melvinhou.dimension2.db.SqlManager;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.dimension2.user.User;
import com.melvinhou.kami.BaseApplication;
import com.melvinhou.kami.BaseException;
import com.melvinhou.kami.manager.ThreadManager;
import com.melvinhou.kami.util.SharePrefUtil;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/1 0:53
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class Dimension2Application extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        createUserSql();
    }

    /**
     * 初始化数据库
     */
    private void createUserSql() {
        ThreadManager.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                boolean isUserCreated =SharePrefUtil.getBoolean("user_sql",false);
                if (!isUserCreated){
                    SqlManager.addUsers(getContext(),
                            createUser(10001l,"拜拜风尘子",
                                    HttpConstant.SERVER_RES +"image/portrait/01.jpg"),
                            createUser(10002l,"车厘子",
                                    HttpConstant.SERVER_RES +"image/portrait/02.jpeg"),
                            createUser(10003l,"呆呆小耗子",
                                    HttpConstant.SERVER_RES +"image/portrait/03.jpg"),
                            createUser(10004l,"医生姐姐来啦",
                                    HttpConstant.SERVER_RES +"image/portrait/04.jpg"),
                            createUser(10005l,"花无痕两两",
                                    HttpConstant.SERVER_RES +"image/portrait/05.jpeg")
                            );

                }
                SharePrefUtil.saveBoolean("user_sql",true);
            }
        });
    }

    private static User createUser(long id,String name,String photo){
        User user = new User();
        user.setUserId(id);
        user.setName(name);
        user.setPhoto(photo);
        return user;
    }

    @Override
    protected BaseException getException() {
        return new BaseException();
    }
}
