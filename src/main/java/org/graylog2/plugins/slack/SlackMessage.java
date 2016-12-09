package org.graylog2.plugins.slack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SlackMessage {

    private final String channel;
    private final String message;
    private final String color;
    private String customMessage;

    private final List<AttachmentField> attachments;

    public SlackMessage(String color, String message, String customMessage, String channel) {
        this.color = color;
        this.message = message;
        this.channel = channel;
        this.customMessage = customMessage;

        this.attachments = Lists.newArrayList();
    }

    public String getJsonString() {
        // See https://api.slack.com/methods/chat.postMessage for valid parameters
        final Map<String, Object> params = new HashMap<String, Object>(){{
            put("channel", channel);
            put("link_names", "1");
            put("parse", "none");
        }};

        if (isNullOrEmpty(customMessage)) {
            params.put("text", message);
            if (!attachments.isEmpty()) {
                final Attachment attachment = new Attachment("Alert details", null, "Details:", color, attachments);
                final List<Attachment> attachments = ImmutableList.of(attachment);
                params.put("attachments", attachments);
            }
        } else {
            params.put("text", customMessage);
        }

        params.put("username", "graylog");

        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not build payload JSON.", e);
        }
    }

    public void addAttachment(AttachmentField attachment) {
        this.attachments.add(attachment);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        @JsonProperty
        public String fallback;
        @JsonProperty
        public String text;
        @JsonProperty
        public String pretext;
        @JsonProperty
        public String color = "good";
        @JsonProperty
        public List<AttachmentField> fields;

        @JsonCreator
        public Attachment(String fallback, String text, String pretext, String color, List<AttachmentField> fields) {
            this.fallback = fallback;
            this.text = text;
            this.pretext = pretext;
            this.color = color;
            this.fields = fields;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttachmentField {
        @JsonProperty
        public String title;
        @JsonProperty
        public String value;
        @JsonProperty("short")
        public boolean isShort = false;

        @JsonCreator
        public AttachmentField(String title, String value, boolean isShort) {
            this.title = title;
            this.value = value;
            this.isShort = isShort;
        }
    }

}
