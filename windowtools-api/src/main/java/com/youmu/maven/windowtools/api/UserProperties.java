package com.youmu.maven.windowtools.api;

import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2018/06/01
 */
public class UserProperties {

    private final Map<String, String> properties;

    public UserProperties(Properties properties) {
        if(null == properties) {
            throw new NullPointerException(
                    "null properties to construct UserProperties");
        }
        Map<String, String> map = Maps.newHashMap();
        for(String s : properties.stringPropertyNames()) {
            map.put(s, verifyProperties(properties.getProperty(s)));
        }
        this.properties = ImmutableMap.copyOf(map);
    }

    /**
     * 校验属性
     * @param property
     * @return
     */
    private String verifyProperties(String property) {
        return property;
    }

    public String getProperty(String name, String defaultVal) {
        String val = getProperty(name);
        return null == val ? defaultVal : val;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, String> toMap() {
        return ImmutableMap.copyOf(properties);
    }
}
