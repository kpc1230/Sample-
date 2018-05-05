package com.thed.zephyr.capture.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingPool {

    private Logger log = LoggerFactory.getLogger("application");

    private int poolSize;

    private int jobCount;

    private final Object mutex = new Object();

    public BlockingPool(int poolSize){
        this.poolSize = poolSize;
    }

    public Boolean takeJob(){
        synchronized(mutex){
            while(jobCount >= poolSize){
                try {
                    mutex.wait();
                    log.debug("Wake up after waiting releasing job");
                } catch (InterruptedException e) {
                    log.warn("Error during waiting releasing job in BlockingPool. Will wait again.", e);
                }
            }
        }
        jobCount++;
        log.debug("Increase the number of current jobs to :" + jobCount + " poolSize:" + poolSize);
        return true;
    }

    public void releaseJob(){
        jobCount--;
        log.debug("Release job, the number of current jobs:" + jobCount + " poolSize:" + poolSize);
        synchronized (mutex){
            log.debug("Notify about release job");
            mutex.notify();
        }
    }

    public void resetPool(){
        jobCount = 0;
        log.debug("Pool was reset  jobCount:{}, poolSize:{}",jobCount, poolSize);
        synchronized (mutex){
            log.debug("Notify about reset pool");
            mutex.notify();
        }
    }

    public Boolean isPoolFull(){
        synchronized (mutex){
            while(jobCount > 0){
                try {
                    mutex.wait();
                    log.debug("Wake up after waiting releasing job, the number active job=" + jobCount);
                } catch (InterruptedException e) {
                    log.warn("Error during waiting releasing job in BlockingPool. Will wait again.", e);
                }
            }
        }
        return true;
    }
}
