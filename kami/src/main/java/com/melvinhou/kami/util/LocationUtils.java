package com.melvinhou.kami.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/15 0015 16:59
 * <p>
 * = 分 类 说 明：地址工具类
 * ================================================
 */
public class LocationUtils {
    /**
     * 获取经纬度
     */
    public static Location getLocation(Context context) {
        String locationProvider;
        //获取地理位置管理器
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers == null) return null;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            //Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            Log.i("wxbnb", "getLocation: 没有可用的位置提供器");
            return null;
        }
        //获取Location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            Log.i("wxbnb", "1纬度：" + location.getLatitude() + "经度：" + location.getLongitude());
            return location;
        } else {
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                //不为空,显示地理位置经纬度
                Log.i("wxbnb", "2纬度：" + location.getLatitude() + "经度：" + location.getLongitude());
            }
            return location;
        }
    }

    //获取地址信息:城市、街道等信息
    public static List<Address> getAddress(Context context, Location location) {
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(context, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                Log.v("TAG", "获取地址信息："+result.toString());
                //[Address[addressLines=[0:"北京市海淀区中关村东路18号C1206室",1:"财智国际大厦",2:"中国建设银行(北京保福寺支行)",3:"财智国际大厦-C座",4:"财智国际大厦-B座",5:"财智中心写字楼",6:"银谷大厦",7:"财智孵化园",8:"中国科学院理化技术研究所(中关村东路)",9:"中国银行(银谷大厦支行)",10:"保福寺桥"],feature=财智国际大厦,admin=北京市,sub-admin=中关村街道,locality=北京市,thoroughfare=中关村东路,postalCode=null,countryCode=CN,countryName=中国,hasLatitude=true,latitude=39.99397778602467,hasLongitude=true,longitude=116.34026963171384,phone=null,url=null,extras=null]]
                for(int i=0;i<result.size();i++){
                    Log.i("ceshi", "position:"+i+"，省:"+result.get(i).getAdminArea()+"，市："+result.get(i).getLocality());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
