package com.commissar.handy.instance;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * java对象地址获取
 */
public class InstanceAddress {

    private static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取实例对象的内存地址
     *
     * @param instance 参数对象
     * @return
     * @throws Exception
     */
    public static long addressOf(Object instance) throws Exception {
        checkNotNull(instance, "instance can not be null");
        Object[] array = new Object[]{instance};

        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        int addressSize = unsafe.addressSize();
        long objectAddress;
        switch (addressSize) {
            case 4:
                objectAddress = unsafe.getInt(array, baseOffset);
                break;
            case 8:
                objectAddress = unsafe.getLong(array, baseOffset);
                break;
            default:
                throw new Exception("unsupported address size: " + addressSize);
        }

        return (objectAddress);
    }

}
