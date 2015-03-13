/*
 * Copyright 2013-2015 µg Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.gms.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PackageUtils {
    public static void checkPackageUid(Context context, String packageName, int callingUid) {
        String[] packagesForUid = context.getPackageManager().getPackagesForUid(callingUid);
        if (packagesForUid != null && !Arrays.asList(packagesForUid).contains(packageName)) {
            throw new SecurityException("callingUid [" + callingUid + "] is not related to packageName [" + packageName + "]");
        }
    }

    public static void checkPackageUid(Context context, String packageName, int callerUid, int callingUid) {
        if (callerUid != 0 && callerUid != callingUid) {
            throw new SecurityException("callerUid [" + callerUid + "] and real calling uid [" + callingUid + "] mismatch!");
        }
        checkPackageUid(context, packageName, callingUid);
    }

    public static String firstSignatureDigest(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        final PackageInfo info;
        try {
            info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (info != null && info.signatures != null && info.signatures.length > 0) {
            for (Signature sig : info.signatures) {
                String digest = sha1sum(sig.toByteArray());
                if (digest != null) {
                    return digest;
                }
            }
        }
        return null;
    }

    public static String sha1sum(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (final NoSuchAlgorithmException e) {
            return null;
        }
        if (md != null) {
            bytes = md.digest(bytes);
            if (bytes != null) {
                StringBuilder sb = new StringBuilder(2 * bytes.length);
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            }
        }
        return null;
    }

    public static int versionCode(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
