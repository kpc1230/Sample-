package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by Masud on 4/23/19.
 */
public interface TenantUpdateService {
    void runAllTenantStatusUpdate();
    void updateTenantStatus(AcHostModel acHostModel);
}
