package com.thed.zephyr.capture.service.gdpr;

import java.util.List;

/**
 * Created by Masud on 4/25/19.
 */
public interface UserConversionService {
     void pullUserKeyFromSessions(String ctId);
     void pullUserAccountIdFromJira(String ctId, List<String> userKeys);
}
