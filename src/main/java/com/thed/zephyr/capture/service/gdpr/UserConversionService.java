package com.thed.zephyr.capture.service.gdpr;

import java.util.List;
import java.util.Map;

/**
 * Created by Masud on 4/25/19.
 */
public interface UserConversionService {
    Map<String, String> pullUserKeyFromSessions(String ctId);

    Map<String, String> pullUserAccountIdFromJira(List<String> userKeys, String keyType);
}
