package com.melvinhou.dimension2.ar.d3;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import de.javagl.obj.Obj;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/20 19:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class D3Entity{

    private String title;
    private String fileName;
    private String cover;
    private String url;
    private String directoryPath;
    private String explain;

    private boolean isDownload;

    D3Entity(){}
    D3Entity(String title,String fileName,String directoryPath){
        setTitle(title);
        setFileName(fileName);
        setDirectoryPath(directoryPath);
        setDownload(true);
    }

    D3Entity(String title,String fileName,String directoryPath,String explain){
        setTitle(title);
        setFileName(fileName);
        setDirectoryPath(directoryPath);
        setExplain(explain);
        setDownload(true);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }
}
