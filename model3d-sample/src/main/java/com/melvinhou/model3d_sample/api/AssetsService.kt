package com.melvinhou.model3d_sample.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.melvinhou.kami.io.AssetsPath
import com.melvinhou.kami.net.*
import com.melvinhou.kami.tool.AssetsUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import java.lang.reflect.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/20 0020 15:35
 * <p>
 * = 分 类 说 明：本地数据加载类
 * ================================================
 */
class AssetsService {

    companion object {
        @kotlin.jvm.JvmField
        val instance = AssetsService()
    }

    private var mService: AssetsApi? = null

    fun Api(): AssetsApi {
        if (mService == null)
            mService = create(AssetsApi::class.java)

        return mService!!
    }

    /**
     * 动态代理模式
     */
    fun create(service: Class<AssetsApi>): AssetsApi {
        val proxy = Proxy.newProxyInstance(
            service.getClassLoader(), arrayOf(service),
            object : InvocationHandler {
                private val emptyArgs = arrayOfNulls<Any>(0)

                @Throws(Throwable::class)
                override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
                    // 判断是不是 Object 的方法
                    if (method.declaringClass == Any::class.java) {
                        return method.invoke(this, args)
                    }
                    //void返回
                    val returnType = method.genericReturnType as ParameterizedType
                    val type = method.returnType
                    val entityType = returnType.actualTypeArguments[0]
                    if (returnType === Void.TYPE) {
                        return emptyFunction()
                    }
                    //获取注解
                    val path = method.getAnnotation(AssetsPath::class.java)?.value
                    //method=AssetsApi.getMediaList()/path=sample_media_list.json
                    //type=Observable/returnType=Observable<FcEntity<ArrayList<MediaItemEntity>>>
                    return loadAssetsData(path, TypeToken.get(entityType))
                }
            })
        return proxy as AssetsApi
    }

    fun emptyFunction() {
    }

    /**
     * 加载数据
     */
    fun <E> loadAssetsData(fileName: String?, type: TypeToken<E>): Observable<E>? {
        return Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<E> ->
            val json = AssetsUtil.readText(fileName)
            val entity = Gson().fromJson<E>(json, type.type)
            emitter.onNext(entity)
            emitter.onComplete()
        } as ObservableOnSubscribe<E>)
    }

}