package org.graylog2.plugins.slack.callback;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class SlackAlarmCallbackTest {
    private static final ImmutableMap<String, Object> VALID_CONFIG_SOURCE = ImmutableMap.<String, Object>builder()
            .put("webhook_url", "https://www.example.org/")
            .put("channel", "#test_channel")
            .put("user_name", "test_user_name")
            .put("add_attachment", true)
            .put("notify_channel", true)
            .put("link_names", true)
            .put("color", "#FF0000")
            .put("custom_message", "test_message")
            .build();
    private SlackAlarmCallback alarmCallback;

    @Before
    public void setUp() {
        alarmCallback = new SlackAlarmCallback();
    }

    @Test
    public void testInitialize() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(VALID_CONFIG_SOURCE);
        alarmCallback.initialize(configuration);
    }

    @Test
    public void testGetAttributes() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(VALID_CONFIG_SOURCE);
        alarmCallback.initialize(configuration);

        final Map<String, Object> attributes = alarmCallback.getAttributes();
        assertThat(attributes.keySet(), hasItems("webhook_url", "channel", "user_name", "add_attachment",
                "notify_channel", "link_names", "color", "custom_message"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(new Configuration(VALID_CONFIG_SOURCE));
    }

    @Test(expected = AlarmCallbackConfigurationException.class)
    public void checkConfigurationFailsIfChannelIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithout("channel"));
    }

    @Test
    public void checkConfigurationFailsIfChannelDoesAcceptDirectMessages() throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithValue("channel", "@john"));
    }

    @Test
    public void testGetRequestedConfiguration() {
        assertThat(alarmCallback.getRequestedConfiguration().asList().keySet(),
                hasItems("webhook_url", "channel", "user_name", "add_attachment", "notify_channel", "link_names", "color", "custom_message"));
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
