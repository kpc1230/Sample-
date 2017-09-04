package com.thed.zephyr.capture.service.data.impl;

import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.Tag;
import com.thed.zephyr.capture.repositories.elasticsearch.TagRepository;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.TagService;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aliakseimatsarski on 8/31/17.
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private DynamoDBAcHostRepository dynamoDBAcHostRepository;
    @Autowired
    private TagRepository tagRepository;

    @Override
    public Set<String> parseTags(String noteData) {
        Set<String> tagList = new TreeSet<>();
        Pattern pattern = Pattern.compile("#(\\w+)|#!|#\\?");
        Matcher matcher = pattern.matcher(noteData);
        String tagName;
        while (matcher.find()) {
            String originalMatch = matcher.group(0);
            if (StringUtils.equals(originalMatch, Tag.QUESTION)){
                tagName = Tag.QUESTION_TAG_NAME;
            } else if (StringUtils.equals(originalMatch, Tag.FOLLOWUP)){
                tagName = Tag.FOLLOWUP_TAG_NAME;
            } else if (StringUtils.equals(originalMatch, Tag.ASSUMPTION)){
                tagName = Tag.ASSUMPTION_TAG_NAME;
            } else if (StringUtils.equals(originalMatch, Tag.IDEA)){
                tagName = Tag.IDEA_TAG_NAME;
            } else {
                tagName = matcher.group(1);
            }

            tagList.add(tagName);
        }

        return tagList;
    }

    @Override
    public List<Tag> saveTags(Note note){
        deleteTags(note.getId());
        Set<String> tagStringList = parseTags(note.getNoteData());
        List<Tag> result = new ArrayList<>();
        for (String tagString:tagStringList){
            Tag tag = new Tag();
            tag.setCtId(note.getCtId());
            tag.setProjectId(note.getProjectId());
            tag.setSessionId(note.getSessionId());
            tag.setName(tagString);
            tag.getNoteIds().add(note.getId());
            result.add(saveTag(tag));
        }

        return result;
    }

    @Override
    public Tag saveTag(Tag rowTag){
        Tag fetchTag = tagRepository.findByCtIdAndNameAndProjectIdAndSessionId(rowTag.getCtId(), rowTag.getName(), rowTag.getProjectId(), rowTag.getSessionId());
        if (fetchTag != null){
            rowTag.getNoteIds().addAll(fetchTag.getNoteIds());
            rowTag.setId(fetchTag.getId());
        }

        return tagRepository.save(rowTag);
    }

    @Override
    public void deleteTags(String noteId) {
        List<Tag> tags = tagRepository.findByCtIdAndNoteIds(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), noteId);
        for (Tag tag:tags){
            if(tag.getNoteIds().size() == 1){
                tagRepository.delete(tag);
            } else {
                tag.getNoteIds().remove(noteId);
                tagRepository.save(tag);
            }
        }
    }
    
    @Override
    public List<Tag> getTags(String noteId) {
        return tagRepository.findByCtIdAndNoteIds(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), noteId);
    }
}
