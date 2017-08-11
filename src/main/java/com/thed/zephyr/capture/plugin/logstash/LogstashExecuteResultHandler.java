package com.thed.zephyr.capture.plugin.logstash;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Masud on 12/8/16.
 */
public class LogstashExecuteResultHandler implements ExecuteResultHandler {

    private LogstashPlugin logstashPlugin;

    private static Logger log = LoggerFactory.getLogger("LogstashPlugin");

    public LogstashExecuteResultHandler(LogstashPlugin logstashPlugin) {
        this.logstashPlugin = logstashPlugin;
    }

    @Override
    public void onProcessComplete(int exitValue) {

    }

    @Override
    public void onProcessFailed(ExecuteException e) {
        log.error("Logstash process failed", e);
        logstashPlugin.startPlugin();
    }
}
