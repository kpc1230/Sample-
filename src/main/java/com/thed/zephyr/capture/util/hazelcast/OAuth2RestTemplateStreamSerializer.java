package com.thed.zephyr.capture.util.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

import java.io.IOException;

public class OAuth2RestTemplateStreamSerializer implements StreamSerializer<OAuth2RestTemplate> {
    @Override
    public void write(ObjectDataOutput out, OAuth2RestTemplate oAuth2RestTemplate) throws IOException {
        /*ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JsonNode oAuth2RestTemplateStr = om.convertValue(oAuth2RestTemplate, JsonNode.class);
        out.writeUTF(oAuth2RestTemplateStr.toString());*/
    //    out.writeUTF("rest-template");
    }

    @Override
    public OAuth2RestTemplate read(ObjectDataInput in) throws IOException {
        /*String oAuth2RestTemplateStr = in.readUTF();
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OAuth2RestTemplate oAuth2RestTemplate = om.readValue(oAuth2RestTemplateStr, OAuth2RestTemplate.class);*/
        return null;//oAuth2RestTemplate;
    }

    @Override
    public int getTypeId() {
        return 2;
    }

    @Override
    public void destroy() {

    }
}
