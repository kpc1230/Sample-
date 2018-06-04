package com.thed.zephyr.capture.plugin.logstash;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;

/**
 * Created by Masud on 12/8/16.
 */
@Configuration
public class LogstashPlugin {
    private static Logger log = LoggerFactory.getLogger("LogstashPlugin");

    @Value("${logstash.elasticksearchHost}")
    private String elasticksearchHost;

    @Value("${logstash.elasticksearchPort}")
    private String elasticksearchPort;

    @Value("${logstash.path}")
    private String logstashPath;

    @Value("${logstash.logsFilesPattern}")
    private String logsFilesPattern;

    @Value("${logstash.logsFilesFolder}")
    private String logsFilesFolder;

    @Value("${logstash.availableInDev}")
    private Boolean availableInDev;

    @Bean
    public boolean startPlugin() {
        if(!availableInDev) {
            log.warn("Logstash plugin disable in Dev mode. See application.properties");
            return false;
        }
        if (StringUtils.isBlank(elasticksearchHost) || StringUtils.isBlank(logstashPath) || StringUtils.isBlank(logsFilesPattern)) {
            log.warn("Can't initialize logstash plugin, please configure plugin in application.properties");
            return false;
        }
        String configPath = prepareLogstashConfigFile();
        String line = logstashPath + "logstash" + " agent --config " + configPath;
        CommandLine cmdLine = CommandLine.parse(line);
        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setProcessDestroyer(processDestroyer);
        LogstashExecuteResultHandler logstashExecuteResultHandler = new LogstashExecuteResultHandler(this);
        try {
            executor.execute(cmdLine, logstashExecuteResultHandler);
            log.info("Started with elasticksearch host:" + elasticksearchHost);
        } catch (Exception exception) {
            log.error("Start logstash error", exception);
        }
        return true;
    }


    private String prepareLogstashConfigFile() {
        InputStream configFileInputStreamTemp = this.getClass().getClassLoader().getResourceAsStream("logstash.conf");
        String logFilesPath = logsFilesFolder + logsFilesPattern;
        String sincedbPath = logsFilesFolder + "/.sincedb";
        File compiledConfig = new File(logsFilesFolder + "/logstashCompiledConfig.conf");
        try {
            BufferedWriter compiledConfigBw = new BufferedWriter(new FileWriter(compiledConfig));
            BufferedReader tamplateConfigBr = new BufferedReader(new InputStreamReader(configFileInputStreamTemp));
            String line;
            while ((line = tamplateConfigBr.readLine()) != null) {
                line = StringUtils.replace(line, "${logfiles_path_pattern}", logFilesPath);
                line = StringUtils.replace(line, "${sincedb_path}", sincedbPath);
                line = StringUtils.replace(line, "${elasticksearch_host}", elasticksearchHost);
                line = StringUtils.replace(line, "${elasticksearch_port}", elasticksearchPort);
                compiledConfigBw.write(line);
                compiledConfigBw.flush();
                compiledConfigBw.newLine();
            }
            compiledConfigBw.close();
        } catch (FileNotFoundException exception) {
            log.error("File not found", exception);
        } catch (IOException exception) {
            log.error("Can't read file", exception);
        } catch (Exception exception) {
            log.error("Error during read config file.", exception);
        } finally {
            try {
                configFileInputStreamTemp.close();
            } catch (IOException exception) {
                log.error("Error close file input stream during prepare Logstash config file", exception);
            }
        }
        return compiledConfig.getPath();
    }
}
