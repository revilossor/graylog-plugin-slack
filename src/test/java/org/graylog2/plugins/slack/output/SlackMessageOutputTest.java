package org.graylog2.plugins.slack.output;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class SlackMessageOutputTest {
    private static final ImmutableMap<String, Object> VALID_CONFIG_SOURCE = ImmutableMap.<String, Object>builder()
            .put("webhook_url", "https://www.example.org/")
            .put("channel", "#test_channel")
            .put("user_name", "test_user_name")
            .put("add_attachment", true)
            .put("notify_channel", true)
            .put("color", "#FF0000")
            .put("custom_message", "test_message")
            .build();

    @Test
    public void testGetAttributes() throws MessageOutputConfigurationException {
        SlackMessageOutput output = new SlackMessageOutput(null, new Configuration(VALID_CONFIG_SOURCE));

        final Map<String, Object> attributes = output.getConfiguration();
        assertThat(attributes.keySet(), hasItems("webhook_url", "channel", "user_name", "add_attachment",
                "notify_channel", "color", "custom_message"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration() throws MessageOutputConfigurationException {
        new SlackMessageOutput(null, new Configuration(VALID_CONFIG_SOURCE));
    }

    @Test(expected = MessageOutputConfigurationException.class)
    public void checkConfigurationFailsIfChannelIsMissing() throws MessageOutputConfigurationException {
        new SlackMessageOutput(null, validConfigurationWithout("channel"));
    }

    @Test
    public void checkConfigurationWorksWithCorrectChannelNotations() throws MessageOutputConfigurationException {
        new SlackMessageOutput(null, validConfigurationWithValue("channel", "#valid_channel"));
    }

    @Test
    public void checkConfigurationWorksWithCorrectDirectMessageNotations() throws MessageOutputConfigurationException {
        new SlackMessageOutput(null, validConfigurationWithValue("channel", "@john"));
    }

    private Configuration validConfigurationWithout(final String key) {
        return new Configuration(Maps.filterEntries(VALID_CONFIG_SOURCE, new Predicate<Map.Entry<String, Object>>() {
            @Override
            public boolean apply(Map.Entry<String, Object> input) {
                return key.equals(input.getKey());
            }
        }));
    }

    private Configuration validConfigurationWithValue(String key, String value) {
        Map<String, Object> confCopy = Maps.newHashMap(VALID_CONFIG_SOURCE);
        confCopy.put(key, value);

        return new Configuration(confCopy);
    }

}
