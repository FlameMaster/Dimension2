package com.melvinhou.dimension2.media.video;

import com.melvinhou.dimension2.user.User;

import java.util.List;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/14 14:39
 * <p>
 * = 分 类 说 明：视频文件
 * ============================================================
 */
public class Movie {

    private String title;
    private List<MediaModel> movies;
    /*作者*/
    private User owner;
    /*进阶参数*/
    private long date;
    private String textDate;
    private String explain;

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTextDate() {
        return textDate;
    }

    public void setTextDate(String textDate) {
        this.textDate = textDate;
    }

    public List<MediaModel> getMovies() {
        return movies;
    }

    public void setMovies(List<MediaModel> movies) {
        this.movies = movies;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
