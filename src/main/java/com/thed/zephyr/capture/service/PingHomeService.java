package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by Masud on 9/15/17.
 */
public interface PingHomeService {
    void dialHome(AcHostModel acHostModel);
    void runPing();
}
