package com.atlassian.bonfire.predicates;

import com.atlassian.excalibur.model.IndexedSession;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

public class SessionNameMatchPredicate implements Predicate<IndexedSession> {
    private String term;

    public SessionNameMatchPredicate(String term) {
        this.term = term;
    }

    @Override
    public boolean apply(IndexedSession input) {
        if (StringUtils.isNotBlank(term)) {
            String name = input.getName();
            String[] split = term.split(" ");
            for (String s : split) {
                if (!name.toLowerCase().contains(s.toLowerCase())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
