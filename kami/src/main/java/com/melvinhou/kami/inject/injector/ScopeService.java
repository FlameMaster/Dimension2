package com.melvinhou.kami.inject.injector;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
 * = 分 类 说 明：
 * ================================================
 */
@Scope
@Retention(RUNTIME)
public @interface ScopeService {

}

