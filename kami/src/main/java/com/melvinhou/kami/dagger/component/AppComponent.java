package com.melvinhou.kami.dagger.component;

import android.content.Context;

import com.melvinhou.kami.dagger.module.AppModule;

import javax.inject.Singleton;

import dagger.Component;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/5/24 14:16
 * <p>
 * = 分 类 说 明：dagger组件
 * ================================================
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    Context getContext();
}