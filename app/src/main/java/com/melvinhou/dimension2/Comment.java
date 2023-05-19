package com.melvinhou.dimension2;

import com.google.gson.annotations.SerializedName;
import com.melvinhou.accountlibrary.bean.User;

import java.util.List;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/22 22:33
 * <p>
 * = 分 类 说 明：评论
 * ================================================
 */
public class Comment {

    private String id;
    private String content;
    private User user;
    @SerializedName(value = "subComments", alternate = "sub_comments")
    private List<SubComment> subComments;
    private long date;

    public class SubComment extends Comment{

        private String parentId;
        private String correlativeId;
        private String correlativeUserName;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getCorrelativeId() {
            return correlativeId;
        }

        public void setCorrelativeId(String correlativeId) {
            this.correlativeId = correlativeId;
        }

        public String getCorrelativeUserName() {
            return correlativeUserName;
        }

        public void setCorrelativeUserName(String correlativeUserName) {
            this.correlativeUserName = correlativeUserName;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<SubComment> getSubComments() {
        return subComments;
    }

    public void setSubComments(List<SubComment> subComments) {
        this.subComments = subComments;
    }

    public long getTimer() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
