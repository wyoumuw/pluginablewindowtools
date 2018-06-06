package com.youmu.maven.windowtools;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.*;

import com.youmu.maven.windowtools.api.UserProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class MainRunner {
    private static final Logger logger = LoggerFactory
            .getLogger(Configuration.class);

    public static void main(String[] args) {
        String config = "./config.conf";
        if(null != args && args.length != 0) {
            for(int i = 0; i < args.length; i++) {
                String arg = args[i];
                if("--config".equals(arg)) {
                    i++;
                    if(i < args.length) {
                        config = args[i];
                    }
                } else if("--help".equals(arg) || "--h".equals(arg)) {
                    printHelp();
                    System.exit(0);
                }
            }
        }
        Configuration configuration = null;
        try(FileInputStream fileInputStream = new FileInputStream(
                config)) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            UserProperties userProperties = new UserProperties(properties);
            configuration = new Configuration(userProperties);
        } catch(Exception e) {
            JFrame jFrame = new JFrame();
            jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            logger.error("错误", e);
            JOptionPane.showMessageDialog(jFrame, e.getMessage());
            System.exit(0);
        }
        new MainFrame(configuration).setVisible(true);
        System.out.println();
    }

    private static void printHelp() {
        System.out.println("--config <filepath> \t 设置配置文件");
        System.out.println("--help|-h \t 设置配置文件");
    }
}
