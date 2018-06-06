package com.youmu.maven.windowtools.api.component;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2018/06/01
 */
public abstract class PluginTabPanel extends JPanel {
    private Map<String, String> userProperties;

    public PluginTabPanel(Map<String, String> userProperties) {
        this.userProperties = userProperties;
    }

    public String getProperty(String name, String defaultVal) {
        String val = getProperty(name);
        return null == val ? defaultVal : val;
    }

    public String getProperty(String name) {
        return userProperties.get(name);
    }

    /**
     * set Window size
     * @return
     */
    public Dimension getExpectedSize() {
        return null;
    }
}
