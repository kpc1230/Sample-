package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AutocompleteBeans {
    @XmlElement
    public List<AutocompleteBean> searchResult;

    public AutocompleteBeans(List<AutocompleteBean> searchResult) {
        this.searchResult = searchResult;
    }
}
