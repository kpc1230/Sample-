package com.thed.zephyr.capture.util;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

/**
 * Created by Masud on 4/4/18.
 */
@Component
public class WikiMarkupRenderer {

    @Autowired
    private Logger log;

    @Autowired
    private WikiParser wikiParser;

    @Autowired
    private EmojiUtil emojiUtil;

    @Autowired
    private AtlassianHostRestClients atlasHostRestClient;

    public  String getWikiRender(String data){
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
         JSONObject reqJson = new JSONObject();
        ResponseEntity<String> resp = null;
         try {
            reqJson.put(ApplicationConstants.WIKI_TYPE, ApplicationConstants.ATLASSIAN_WIKI_RENDERER);
            reqJson.put(ApplicationConstants.WIKI_KEY, data);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestUpdate = new HttpEntity<>(reqJson.toString(), httpHeaders);
            resp = atlasHostRestClient.authenticatedAs(hostUser).exchange(JiraConstants.REST_API_WIKI_RENDER,
                    HttpMethod.POST, requestUpdate, String.class);
            return CaptureUtil.createNoteData(resp.getBody());
        } catch (Exception exp) {
            log.error("Error in wiki renderer : " + exp.getMessage(), exp);
            return CaptureUtil.createNoteData(emojiUtil.emojify(wikiParser.parseWiki(data,ApplicationConstants.HTML)));
        }

    }
}
