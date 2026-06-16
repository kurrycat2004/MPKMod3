package io.github.kurrycat.mpkmod.service.util;

public final class ServiceUtil {
    private ServiceUtil() {}

    public static boolean doesClassExist(String binaryClassName) {
        try {
            Class.forName(binaryClassName, false, ServiceUtil.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
