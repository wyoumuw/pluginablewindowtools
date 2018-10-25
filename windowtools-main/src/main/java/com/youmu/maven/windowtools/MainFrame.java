package com.youmu.maven.windowtools;

import com.youmu.maven.windowtools.api.component.PluginTabPanel;

import java.awt.*;
import java.util.Map;

import javax.swing.*;

/**
 * @Author: YOUMU
 * @Description:
 * @Date: 2018/06/01
 */
public class MainFrame extends JFrame {
	private JTabbedPane tabPane = new JTabbedPane();
	private Configuration configuration;

	public MainFrame(Configuration configuration)
			throws HeadlessException {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.configuration = configuration;
		// generate all tab components
		Map<String, PluginTabPanel> tabs = configuration
				.constructTabPanels();
		// to add to tabPane
		for (Map.Entry<String, PluginTabPanel> stringPluginTabPanelEntry : tabs
				.entrySet()) {
			tabPane.addTab(stringPluginTabPanelEntry.getKey(),
					stringPluginTabPanelEntry.getValue());
		}
//		tabPane.addMouseWheelListener(event -> {
//			System.out.println(event);
//		});
		add(tabPane);
		// set tab change listener
		tabPane.addChangeListener(event -> {
			JTabbedPane tabbedPane = (JTabbedPane) event.getSource();
			Component component = tabbedPane.getSelectedComponent();
			if (component instanceof PluginTabPanel) {
				onSelected((PluginTabPanel) component);
			}
		});
		setSize(configuration.getInitWindowWidth(),
				configuration.getInitWindowHeight());
		//trigger component select event
		Component component = tabPane.getSelectedComponent();
		if (component instanceof PluginTabPanel) {
			onSelected((PluginTabPanel) component);
		}
		// cannot resize on current version
		setResizable(false);
	}

	private void onSelected(PluginTabPanel panel) {
		// resize if get getExpectedSize is not null
		Dimension dimension = panel.getExpectedSize();
		if (null != dimension) {
			setSize(dimension);
		} else {
			// or re-initial size
			setSize(configuration.getInitWindowWidth(),
					configuration.getInitWindowHeight());
		}

		panel.onSelected();
	}
}
