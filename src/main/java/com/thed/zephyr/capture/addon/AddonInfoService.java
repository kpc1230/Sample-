package com.thed.zephyr.capture.addon;

import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import org.springframework.web.client.RestClientException;


public interface AddonInfoService {
    AddonInfo getAddonInfo(AcHostModel acHostModel) throws RestClientException;
}
