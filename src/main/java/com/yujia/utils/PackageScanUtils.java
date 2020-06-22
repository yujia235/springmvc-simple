package com.yujia.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 包扫描工具类
 */
public class PackageScanUtils {

    public static void main(String[] args) {
        Set<Class<?>> classSet = getClzFromPkg("com.yujia.dao");
//        for (Class<?> aClass : classSet) {
//            System.out.println("aClass.getName() = " + aClass.getName());
//            System.out.println("aClass.getSimpleName() = " + aClass.getSimpleName());
//            System.out.println("aClass.isAnnotation() = " + aClass.isAnnotation());
//            System.out.println("aClass.isInterface() = " + aClass.isInterface());
//            System.out.println("aClass.isLocalClass() = " + aClass.isLocalClass());
//            System.out.println("aClass.isMemberClass() = " + aClass.isMemberClass());
//            System.out.println("--------------------------------");
//        }
        Set<Class<?>> classSet2 = getClzFromPkg("com.yujia.dao");

        classSet = getClzFromPkg("com.yujia.annotation");
        for (Class<?> aClass : classSet) {
            System.out.println("aClass.getName() = " + aClass.getName());
            System.out.println("aClass.isAnnotation() = " + aClass.isAnnotation());
            System.out.println("aClass.isInterface() = " + aClass.isInterface());
            System.out.println("aClass.isLocalClass() = " + aClass.isLocalClass());
            System.out.println("aClass.isMemberClass() = " + aClass.isMemberClass());
            System.out.println("--------------------------------");
        }
        classSet = getClzFromPkg("com.yujia.utils");
        for (Class<?> aClass : classSet) {
            System.out.println("aClass.getName() = " + aClass.getName());
            System.out.println("aClass.isAnnotation() = " + aClass.isAnnotation());
            System.out.println("aClass.isInterface() = " + aClass.isInterface());
            System.out.println("aClass.isLocalClass() = " + aClass.isLocalClass());
            System.out.println("aClass.isMemberClass() = " + aClass.isMemberClass());
            System.out.println("--------------------------------");
        }
    }

    /**
     * 扫描包路径下所有的class文件
     *
     * @param pkg
     * @return
     */
    public static Set<Class<?>> getClzFromPkg(String pkg) {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        String pkgDirName = pkg.replace('.', '/');
        try {
            Enumeration<URL> urls = com.yujia.utils.PackageScanUtils.class.getClassLoader().getResources(pkgDirName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    // 本地项目包
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    findClassesByFile(pkg, filePath, classSet);
                } else if ("jar".equals(protocol)) {
                    // jar包文件
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    findClassesByJar(pkg, jar, classSet);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classSet;
    }

    /**
     * 扫描包路径下的所有class文件（本地项目）
     *
     * @param pkgName  包名
     * @param pkgPath  包对应的绝对地址
     * @param classSet 保存包路径下class的集合
     */
    private static void findClassesByFile(String pkgName, String pkgPath, Set<Class<?>> classSet) {
        File dir = new File(pkgPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 获取目录|class文件
        File[] dirFiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));
        if (dirFiles == null || dirFiles.length == 0) {
            return;
        }
        Class clazz = null;
        String className = null;
        for (File f : dirFiles) {
            if (f.isDirectory()) {
                findClassesByFile(pkgName + "." + f.getName(),
                        pkgPath + "/" + f.getName(),
                        classSet);
                continue;
            }
            // 获取类名，去掉'.class'后缀
            className = f.getName();
            className = className.substring(0, className.length() - 6);
            // 加载类
            clazz = loadClass(pkgName + "." + className);
//            try {
//                clazz = Class.forName(pkgName + "." + className);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            if (clazz != null) {
                classSet.add(clazz);
            }
        }
    }

    /**
     * 扫描包路径下的所有class文件（jar包）
     *
     * @param pkgName  包名
     * @param jar      jar文件
     * @param classSet 保存包路径下class的集合
     */
    private static void findClassesByJar(String pkgName, JarFile jar, Set<Class<?>> classSet) throws ClassNotFoundException {
        String pkgDir = pkgName.replace(".", "/");
        Enumeration<JarEntry> entry = jar.entries();
        JarEntry jarEntry;
        String name = null, className = null;
        Class<?> clazz = null;
        while (entry.hasMoreElements()) {
            jarEntry = entry.nextElement();
            name = jarEntry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }
            if (jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith(".class")) {
                // 非指定包路径|非class文件
                continue;
            }
            // 去掉后面的'.class', 将路径转为package格式
            className = name.substring(0, name.length() - 6);
            try {
                clazz = Class.forName(className.replace("/", "."));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (clazz != null) {
                classSet.add(clazz);
            }
        }
    }

    private static Class<?> loadClass(String fullClzName) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(fullClzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
