package com.melvinhou.dimension2.media.tiktok;

import com.google.gson.annotations.SerializedName;
import com.melvinhou.dimension2.Comment;
import com.melvinhou.dimension2.user.User;

import java.util.List;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/21 20:40
 * <p>
 * = 分 类 说 明：抖音数据对象
 * ================================================
 */
public class TiktokEntity {

    private String id;
    private String url;
    @SerializedName(value = "coverAddress", alternate = "cover_address")
    private String coverAddress;
    private String title;
    private String explain;
    @SerializedName(value = "starCount", alternate = {"star_count", "like_count"})
    private int starCount;
    @SerializedName(value = "commentCount", alternate = "comment_count")
    private int commentCount;
    @SerializedName(value = "shareCount", alternate = "share_count")
    private int shareCount;
    private User user;
    private List<Comment> comments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCoverAddress() {
        return coverAddress;
    }

    public void setCoverAddress(String coverAddress) {
        this.coverAddress = coverAddress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
