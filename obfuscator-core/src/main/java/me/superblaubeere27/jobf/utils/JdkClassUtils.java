/*
 * Copyright © 2020-2026 matou.tech Inc. All rights reserved.
 */
package me.superblaubeere27.jobf.utils;

/*
 * 修订记录:
 * turalyon@matou.tech 2026-04-14 15:37 创建
 *
 */


/**
 * 说明：
 * <p>
 *
 * @author turalyon@matou.tech
 */
public class JdkClassUtils {
    /**
     * 判断一个类是否是 JDK 自带类
     * @param internalClassName 内部类名格式，如 java/io/Serializable
     * @return true=JDK自带，false=自定义/第三方类
     */
    public static boolean isJdkClass(String internalClassName) {
        // 1. 转换为标准全类名：java/io/Serializable → java.io.Serializable
        String className = internalClassName.replace('/', '.');

        // 2. 快速判断：包名前缀（最高效）
        if (className.startsWith("java.")
                || className.startsWith("javax.")
                || className.startsWith("jdk.")
                || className.startsWith("sun.")) {
            return true;
        }

        // 3. 加载类，判断类加载器（精准判断）
        try {
            Class<?> clazz = Class.forName(className);
            // Bootstrap 类加载器加载的类，getClassLoader() 返回 null
            return clazz.getClassLoader() == null;
        } catch (Exception | NoClassDefFoundError e) {
            // 类无法加载时，降级：仅通过包名判断
            return false;
        }
    }
    public static boolean isSpringClass(String springClassName) {
        String className = springClassName.replace('/', '.');
        return className.startsWith("org.springframework.");
    }
    // 测试
    public static void main(String[] args) {
        String className = "java/io/Serializable";
        System.out.println(isJdkClass(className)); // 输出 true
    }


}
