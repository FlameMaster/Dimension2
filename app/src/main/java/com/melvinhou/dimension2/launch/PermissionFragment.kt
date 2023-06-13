package com.melvinhou.dimension2.launch

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.core.app.NotificationManagerCompat
import com.melvinhou.dimension2.R
import com.melvinhou.dimension2.databinding.FragmentPermissionBinding
import com.melvinhou.dimension2.utils.KeyConstant
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KindFragment


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 15:17
 * <p>
 * = 分 类 说 明：权限页
 * ================================================
 */
class PermissionFragment : KindFragment<FragmentPermissionBinding, LaunchViewModel>() {
    override val _ModelClazz: Class<LaunchViewModel>
        get() = LaunchViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentPermissionBinding = FragmentPermissionBinding.inflate(inflater, container, false)


    //日历
    private val REQUIRED_PERMISSIONS_CALENDAR = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    //相机
    private val REQUIRED_PERMISSIONS_CAMERA = arrayOf(
        Manifest.permission.CAMERA
    )

    //联系人
    private val REQUIRED_PERMISSIONS_CONTACTS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS
    )

    //位置
    private val REQUIRED_PERMISSIONS_LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    //麦克风
    private val REQUIRED_PERMISSIONS_MICROPHONE = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    //手机
    private val REQUIRED_PERMISSIONS_PHONE = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
//        Manifest.permission.ADD_VOICEMAIL,
        Manifest.permission.USE_SIP,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.ANSWER_PHONE_CALLS
    )

    //传感器
    private val REQUIRED_PERMISSIONS_SENSORS = arrayOf(
        Manifest.permission.BODY_SENSORS
    )

    //短信
    private val REQUIRED_PERMISSIONS_SMS = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.RECEIVE_MMS
    )

    //存储
    private val REQUIRED_PERMISSIONS_STORAGE =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        else
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

    override fun initView() {
        //间隔
        mModel.WindowInsets.observe(this) { insets ->
            mBinding.root.setPadding(0, insets.top, 0, insets.bottom)
        }
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION
        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
    }

    override fun initListener() {
        //运行时权限
        mBinding.cbPermissionPhone.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_PHONE))
                requestPermissions(REQUIRED_PERMISSIONS_PHONE)
        }
        mBinding.cbPermissionLocation.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_LOCATION))
                requestPermissions(REQUIRED_PERMISSIONS_LOCATION)
        }
        mBinding.cbPermissionStorage.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_STORAGE))
                requestPermissions(REQUIRED_PERMISSIONS_STORAGE)
        }
        mBinding.cbPermissionCamera.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_CAMERA))
                requestPermission(REQUIRED_PERMISSIONS_CAMERA[0])
        }
        mBinding.cbPermissionSms.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_SMS))
                requestPermissions(REQUIRED_PERMISSIONS_SMS)
        }
        mBinding.cbPermissionMicrophone.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_MICROPHONE))
                requestPermission(REQUIRED_PERMISSIONS_MICROPHONE[0])
        }
        mBinding.cbPermissionSensors.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_SENSORS))
                requestPermission(REQUIRED_PERMISSIONS_SENSORS[0])
        }
        mBinding.cbPermissionContacts.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_CONTACTS))
                requestPermissions(REQUIRED_PERMISSIONS_CONTACTS)
        }
        mBinding.cbPermissionCalendar.setOnClickListener {
            if (!checkPermission(REQUIRED_PERMISSIONS_CALENDAR))
                requestPermissions(REQUIRED_PERMISSIONS_CALENDAR)
        }
        //特殊权限
        mBinding.cbPermissionFilesAccess.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.fromParts("package", requireContext().packageName, null)
                    toResultActivity(
                        intent
                    ) { result: ActivityResult ->
                        onPermissionResult(result.resultCode == Activity.RESULT_OK)
                    }
                }
        }
        mBinding.cbPermissionNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, requireContext().applicationInfo.uid)
                    toResultActivity(
                        intent
                    ) { result: ActivityResult ->
                        onPermissionResult(result.resultCode == Activity.RESULT_OK)
                    }
                }
        }
        mBinding.cbPermissionOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (!Settings.canDrawOverlays(requireContext())) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    toResultActivity(
                        intent
                    ) { result: ActivityResult ->
                        onPermissionResult(result.resultCode == Activity.RESULT_OK)
                    }
                }
        }
        mBinding.cbPermissionUnknownApp.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                if (!requireContext().packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${requireContext().packageName}")
                    )
                    toResultActivity(
                        intent
                    ) { result: ActivityResult ->
                        onPermissionResult(result.resultCode == Activity.RESULT_OK)
                    }
                }
        }
        mBinding.btSubmit.setOnClickListener {
            //权限判断
            if (
                mBinding.cbPermissionPhone.isSelected
                && mBinding.cbPermissionLocation.isSelected
                && mBinding.cbPermissionStorage.isSelected
                && mBinding.cbPermissionCamera.isSelected
                && mBinding.cbPermissionSms.isSelected
                && mBinding.cbPermissionMicrophone.isSelected
                && mBinding.cbPermissionSensors.isSelected
                && mBinding.cbPermissionContacts.isSelected
                && mBinding.cbPermissionCalendar.isSelected
                && mBinding.cbPermissionFilesAccess.isSelected
                && mBinding.cbPermissionNotification.isSelected
                && mBinding.cbPermissionOverlay.isSelected
                && mBinding.cbPermissionUnknownApp.isSelected
            )
                onNext()
            else
                FcUtils.showToast("还有未开启的权限")
        }
    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        checkAllPermissions()
    }

//    override fun onPermissionResult(result: Boolean) {
//        super.onPermissionResult(result)
//        checkAllPermissions()
//    }

    /**
     * 检查权限状态
     */
    private fun checkAllPermissions() {
        //运行时权限
        setPermissionState(mBinding.cbPermissionPhone, checkPermission(REQUIRED_PERMISSIONS_PHONE))
        setPermissionState(
            mBinding.cbPermissionLocation,
            checkPermission(REQUIRED_PERMISSIONS_LOCATION)
        )
        setPermissionState(
            mBinding.cbPermissionStorage,
            checkPermission(REQUIRED_PERMISSIONS_STORAGE)
        )
        setPermissionState(
            mBinding.cbPermissionCamera,
            checkPermission(REQUIRED_PERMISSIONS_CAMERA)
        )
        setPermissionState(mBinding.cbPermissionSms, checkPermission(REQUIRED_PERMISSIONS_SMS))
        setPermissionState(
            mBinding.cbPermissionMicrophone,
            checkPermission(REQUIRED_PERMISSIONS_MICROPHONE)
        )
        setPermissionState(
            mBinding.cbPermissionSensors,
            checkPermission(REQUIRED_PERMISSIONS_SENSORS)
        )
        setPermissionState(
            mBinding.cbPermissionContacts,
            checkPermission(REQUIRED_PERMISSIONS_CONTACTS)
        )
        setPermissionState(
            mBinding.cbPermissionCalendar,
            checkPermission(REQUIRED_PERMISSIONS_CALENDAR)
        )
        //特殊权限
        setPermissionState(
            mBinding.cbPermissionFilesAccess,
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
        )
        setPermissionState(
            mBinding.cbPermissionNotification,
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        )
        setPermissionState(mBinding.cbPermissionOverlay, Settings.canDrawOverlays(requireContext()))
        setPermissionState(
            mBinding.cbPermissionUnknownApp,
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O || requireContext().packageManager.canRequestPackageInstalls()
        )

    }

    /**
     * 设置状态
     */
    private fun setPermissionState(view: TextView, isOpen: Boolean) {
        view.isSelected = isOpen
        view.text = if (isOpen) "已开启" else "未开启"
    }

    /**
     * 下一步操作
     */
    private fun onNext() {
        SharePrefUtil.saveBoolean(KeyConstant.APP_PERMISSION,true)
        mModel.toFragment(R.id.navigation_advert)
    }
}