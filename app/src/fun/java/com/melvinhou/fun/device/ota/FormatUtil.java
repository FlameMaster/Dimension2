package com.melvinhou.fun.device.ota;

public class FormatUtil {

    public static String bytesToHexString(byte[] bArr) {
        if (bArr == null || bArr.length==0)
            return "";
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;
        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2)
                sb.append(0);
            sb.append(sTmp.toUpperCase() + " ");
        }
        return sb.toString();
    }
    public static int bytesToIntLittleEndian(byte a[], int start) {
        int s = 0;
        int s0 = a[start + 3] & 0xff;
        int s1 = a[start + 2] & 0xff;
        int s2 = a[start + 1] & 0xff;
        int s3 = a[start + 0] & 0xff;
        s0 <<= 24;
        s1 <<= 16;
        s2 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }
}
