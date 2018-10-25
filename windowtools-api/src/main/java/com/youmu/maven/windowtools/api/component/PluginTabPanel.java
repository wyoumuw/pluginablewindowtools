package com.youmu.maven.windowtools.api.component;

import com.youmu.maven.windowtools.api.ConfigConstants;
import com.youmu.maven.windowtools.api.annotation.TabPrefix;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * @Author: YOUMU
 * @Description: must be annotate {@link TabPrefix} or not can be load
 * @Date: 2018/06/01
 */
public abstract class PluginTabPanel extends JPanel {
	private Map<String, String> userProperties;

	private int order;

	private boolean disabled;

	public PluginTabPanel(Map<String, String> userProperties) {
		this.userProperties = userProperties;
		this.order = Integer.valueOf(getProperty(ConfigConstants.CONFIG_ORDER, "0"));
		this.disabled = Boolean.valueOf(getProperty(ConfigConstants.CONFIG_DISABLED, "false"));
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
	 *
	 * @return
	 */
	public Dimension getExpectedSize() {
		return null;
	}

	public int getOrder() {
		return order;
	}

	public boolean disabled() {
		return disabled;
	}

	public void onSelected() {
		//do not any things
	}
}
