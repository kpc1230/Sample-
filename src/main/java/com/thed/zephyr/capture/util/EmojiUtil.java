package com.thed.zephyr.capture.util;

import com.atlassian.connect.spring.AtlassianHostUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Masud on 10/21/17.
 */
@Component
public class EmojiUtil  {

    public EmojiUtil(){
    }

    public static String emojify(String str){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String emoUrl = host.getHost().getBaseUrl()+JiraConstants.EMOICON_PREFIX;
        String ext = ".gif";
        Map<String,String> jiraEmojis = new HashMap<>();
        jiraEmojis.put(":)","smile");
        jiraEmojis.put(":(","sad");
        jiraEmojis.put(":P","tongue");
        jiraEmojis.put(":D","biggrin");
        jiraEmojis.put(";)","wink");
        jiraEmojis.put("(y)","thumbs_up");
        jiraEmojis.put("(n)","thumbs_down");
        jiraEmojis.put("(i)","information");
        jiraEmojis.put("(/)","check");
        jiraEmojis.put("(x)","error");
        jiraEmojis.put("(!)","warning");
        jiraEmojis.put("(+)","add");
        jiraEmojis.put("(-)","forbidden");
        jiraEmojis.put("(?)","help_16");
        jiraEmojis.put("(on)","lightbulb_on");
        jiraEmojis.put("(off)","lightbulb");
        jiraEmojis.put("(*)","star_yellow");
        jiraEmojis.put("(*r)","star_red");
        jiraEmojis.put("(*g)","star_green");
        jiraEmojis.put("(*b)","star_blue");
        jiraEmojis.put("(*y)","star_yellow");
        jiraEmojis.put("(flag)","flag");
        jiraEmojis.put("(flagoff)","flag_grey");
//        jiraEmojis.put("<3","heart");
//        jiraEmojis.put("</3","broken_heart");
        String result = str;
        for (Map.Entry<String, String> entry : jiraEmojis.entrySet()) {
            if(result.toLowerCase().contains(entry.getKey().toLowerCase())){
                result = result.replace(entry.getKey(),"<img src=\""+emoUrl+entry.getValue()+ext+"\"></img>");;
            }
        }
        return result;
    }
}

