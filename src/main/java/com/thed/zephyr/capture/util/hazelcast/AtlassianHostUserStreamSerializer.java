package com.thed.zephyr.capture.util.hazelcast;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class AtlassianHostUserStreamSerializer implements StreamSerializer<AtlassianHostUser> {

    @Override
    public void write(ObjectDataOutput out, AtlassianHostUser object) throws IOException {
        /*ObjectMapper om = new ObjectMapper();
        JsonNode hostUserStr = om.convertValue(object, JsonNode.class);
        out.writeUTF(hostUserStr.toString());*/
    }

    @Override
    public AtlassianHostUser read(ObjectDataInput in) throws IOException {
        /*String hostUserStr = in.readUTF();
        ObjectMapper om = new ObjectMapper();
        AtlassianHostUser atlassianHostUser = om.readValue(hostUserStr, AtlassianHostUser.class);
        return atlassianHostUser;*/
        return null;
    }

    @Override
    public int getTypeId() {
        return 1;
    }

    @Override
    public void destroy() {

    }
}
