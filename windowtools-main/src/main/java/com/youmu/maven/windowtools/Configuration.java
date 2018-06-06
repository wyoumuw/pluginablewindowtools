package com.youmu.maven.windowtools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.youmu.maven.windowtools.api.UserProperties;
import com.youmu.maven.windowtools.api.annotation.TabName;
import com.youmu.maven.windowtools.api.component.PluginTabPanel;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2018/06/01
 */
class Configuration {
    private Logger logger = LoggerFactory.getLogger(Configuration.class);
    /**
     * 插件dir
     */
    public static final String PRO_PLUGINS_DIR = "pluginsDir";

    /**
     * 初始窗口高
     */
    public static final String PRO_INITIAL_WINDOW_HEIGHT = "initWindowHeight";

    /**
     * 初始窗口宽
     */
    public static final String PRO_INITIAL_WINDOW_WIDTH = "initWindowWidth";

    private final Map<String, Class<? extends PluginTabPanel>> tabs = Maps
            .newHashMap();

    private int initWindowHeight;

    private int initWindowWidth;

    private UserProperties userProperties;

    public Configuration(UserProperties userProperties)
            throws IOException {
        this.userProperties = userProperties;
        setInitWindowHeight(Integer.valueOf(userProperties
                .getProperty(PRO_INITIAL_WINDOW_HEIGHT, "500")));
        setInitWindowWidth(Integer.valueOf(userProperties
                .getProperty(PRO_INITIAL_WINDOW_WIDTH, "500")));
        String pluginsDirStr = userProperties.getProperty(PRO_PLUGINS_DIR,
                "/plugins");
        File pluginsDir = new File(pluginsDirStr);
        if(!pluginsDir.exists()) {
            throw new RuntimeException(pluginsDirStr + " is not exists");
        }
        if(!pluginsDir.isDirectory()) {
            throw new RuntimeException(pluginsDirStr + " is not dir");
        }
        File[] pluginClasses = pluginsDir.listFiles();
        List<URL> urls = Lists.newLinkedList();
        for(File pluginClass : pluginClasses) {
            // 判断是否是jar包或者独立的class，如果是的话则要添加
            if("jar".equalsIgnoreCase(FilenameUtils
                    .getExtension(pluginClass.getAbsolutePath()))
                    || "class".equalsIgnoreCase(FilenameUtils.getExtension(
                            pluginClass.getAbsolutePath()))) {
                urls.add(pluginClass.toURI().toURL());
            }
        }
        URLClassLoader urlClassLoader = new URLClassLoader(
                urls.toArray(new URL[urls.size()]));
        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver(
                urlClassLoader);
        List<String> fullClassNames = Lists.newLinkedList();
        for(URL url : urls) {
            if(url.getFile().endsWith(".jar")) {
                // add all jar class
                String jarPath = url.toString();
                Resource[] resources = resourceLoader
                        .getResources("jar:" + jarPath + "!/**/*.class");
                fullClassNames.addAll(Lists.newArrayList(resources)
                        .stream().map(resource -> {
                            // 吧file:/C:/Users/ucmed/Desktop/soft/jclasslib-5.2/jclasslib/lib/annotations-13.0.jar!/org/intellij/lang/annotations/Flow.class
                            // 去掉file:/C:/Users/ucmed/Desktop/soft/jclasslib-5.2/jclasslib/lib/annotations-13.0.jar
                            // 然后在去掉!/
                            // 然后再替换/成.获取包路径
                            try {
                                return StringUtils.replace(
                                        resource.getURL().getPath(),
                                        jarPath, "").substring(2);
                            } catch(IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toList()));
            } else if(url.getFile().endsWith(".class")) {
                // class type file
                fullClassNames.add(StringUtils.replace(url.getPath(),
                        pluginsDir.toURI().toURL().toString(), ""));
            }
        }
        // load all sub-class of PluginTabPanel to configuration
        loadComponentClasses(urlClassLoader,
                fullClassNames.stream()
                        .map(Configuration::getFullClassName)
                        .collect(Collectors.toSet()));
    }

    private void loadComponentClasses(ClassLoader classLoader,
            Set<String> fullClassNames) {
        for(String clazzStr : fullClassNames) {
            try {
                Class clazz = classLoader
                        .loadClass(clazzStr);
                // 如果是PluginTabPanel的子类并且不是abstract的话
                if(ClassUtils.isAssignable(PluginTabPanel.class, clazz)
                        && !Modifier.isAbstract(clazz.getModifiers())) {
                    String tabName = clazz.getSimpleName();
                    TabName tabNameAnnotation = (TabName) clazz
                            .getAnnotation(TabName.class);
                    if(null != tabNameAnnotation) {
                        tabName = tabNameAnnotation.value();
                    }
                    add(tabName, clazz);
                }
            } catch(Throwable e) {
                logger.error("fail to read plugins class", e);
            }
        }
    }

    //org/spring/xxx/xxx/StringUtils.class ->org.spring.xxx.xxx.StringUtils
    private static String getFullClassName(String className) {
        return StringUtils.replace(className.replace('/', '.'), ".class",
                "");
    }

    public void add(String tabName,
            Class<? extends PluginTabPanel> panel) {
        tabs.put(tabName, panel);
    }

    public int getInitWindowHeight() {
        return initWindowHeight;
    }

    public void setInitWindowHeight(int initWindowHeight) {
        this.initWindowHeight = initWindowHeight;
    }

    public int getInitWindowWidth() {
        return initWindowWidth;
    }

    public void setInitWindowWidth(int initWindowWidth) {
        this.initWindowWidth = initWindowWidth;
    }

    /**
     * 构建PluginTabPanel
     * @return
     */
    public Map<String, PluginTabPanel> constructTabPanels() {
        Map<String, PluginTabPanel> map = Maps.newHashMap();
        for(Map.Entry<String, Class<? extends PluginTabPanel>> stringClassEntry : tabs
                .entrySet()) {
            try {
                Constructor constructor = stringClassEntry.getValue()
                        .getConstructor(Map.class);
                map.put(stringClassEntry.getKey(),
                        (PluginTabPanel) constructor
                                .newInstance(userProperties.toMap()));
            } catch(NoSuchMethodException | IllegalAccessException
                    | InstantiationException
                    | InvocationTargetException e) {
                logger.error("fail to load plugin {}",
                        stringClassEntry.getKey(), e);
            }
        }
        return map;
    }
}
