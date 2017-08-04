package com.atlassian.bonfire.service.parser;

/**
 * Represents parsed option of html select
 *
 * @since v2.9.1
 */
public class ParsedFieldOption {
    private String text;

    private String value;

    public ParsedFieldOption(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
