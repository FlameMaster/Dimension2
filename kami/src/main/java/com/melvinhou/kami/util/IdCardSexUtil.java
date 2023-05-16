package com.melvinhou.kami.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class IdCardSexUtil {
    public static Integer judgeGender(String idNumber,Integer i) throws IllegalArgumentException{
        System.out.println(idNumber.length());
        if(idNumber.length() != 18 && idNumber.length() != 15){
            throw new RuntimeException("第"+i+"行身份证号长度错误");
        }
        int gender = 0;
        if(idNumber.length() == 18){
            //如果身份证号18位，取身份证号倒数第二位
            char c = idNumber.charAt(idNumber.length() - 2);
            gender = Integer.parseInt(String.valueOf(c));
        }else{
            //如果身份证号15位，取身份证号最后一位
            char c = idNumber.charAt(idNumber.length() - 1);
            gender = Integer.parseInt(String.valueOf(c));
        }
        System.out.println("gender = " + gender);
        if(gender % 2 == 1){
            return 1;
        }else{
            return 2;
        }
    }

    public static Integer judgeGenderWeb(String idNumber) throws IllegalArgumentException{
        System.out.println(idNumber.length());
        if(idNumber.length() != 18 && idNumber.length() != 15){
            throw new RuntimeException("身份证号长度错误");
        }
        int gender = 0;
        if(idNumber.length() == 18){
            //如果身份证号18位，取身份证号倒数第二位
            char c = idNumber.charAt(idNumber.length() - 2);
            gender = Integer.parseInt(String.valueOf(c));
        }else{
            //如果身份证号15位，取身份证号最后一位
            char c = idNumber.charAt(idNumber.length() - 1);
            gender = Integer.parseInt(String.valueOf(c));
        }
        System.out.println("gender = " + gender);
        if(gender % 2 == 1){
            return 1;
        }else{
            return 2;
        }
    }

    /**
     *根据身份证号码计算年龄
     * @param idNumber 考虑到了15位身份证，但不一定存在
     */
    private static final int invalidAge = -1;//非法的年龄，用于处理异常。

    public static int getAgeByIDNumber(String idNumber) {
        String dateStr;
        if (idNumber.length() == 15) {
            dateStr = "19" + idNumber.substring(6, 12);
        } else if (idNumber.length() == 18) {
            dateStr = idNumber.substring(6, 14);
        } else {//默认是合法身份证号，但不排除有意外发生
            return invalidAge;
        }


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date birthday = simpleDateFormat.parse(dateStr);
            return getAgeByDate(birthday);
        } catch (ParseException e) {
            return invalidAge;
        }


    }

    public static int getAgeByDate(Date birthday) {
        Calendar calendar = Calendar.getInstance();

        //calendar.before()有的点bug
        if (calendar.getTimeInMillis() - birthday.getTime() < 0L) {
            return invalidAge;
        }


        int yearNow = calendar.get(Calendar.YEAR);
        int monthNow = calendar.get(Calendar.MONTH);
        int dayOfMonthNow = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(birthday);


        int yearBirthday = calendar.get(Calendar.YEAR);
        int monthBirthday = calendar.get(Calendar.MONTH);
        int dayOfMonthBirthday = calendar.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirthday;


        if (monthNow <= monthBirthday && monthNow == monthBirthday && dayOfMonthNow < dayOfMonthBirthday || monthNow < monthBirthday) {
            age--;
        }

        return age;
    }
}
