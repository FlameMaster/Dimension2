package com.melvinhou.dimension2.game;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/26 19:58
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class GameLaunchItem {

    private String name;
    private String imgUrl;

    GameLaunchItem(){

    }

    GameLaunchItem(String name,String imgUrl){
        setName(name);
        setImgUrl(imgUrl);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
