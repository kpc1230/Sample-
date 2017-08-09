package com.thed.zephyr.capture.conditions;


import com.thed.zephyr.capture.service.BonfireLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(BonfireConditionEvaluator.SERVICE)
public class BonfireConditionEvaluatorImpl implements BonfireConditionEvaluator {
    @Autowired
    BonfireLicenseService bonfireLicenseService;

    @Override
    public boolean shouldDisplay(final AccessCheckMode mode) {
        return mode == AccessCheckMode.LICENSE && isLicensed();
    }

    private boolean isLicensed() {
        return bonfireLicenseService.isBonfireActivated();
    }
}
