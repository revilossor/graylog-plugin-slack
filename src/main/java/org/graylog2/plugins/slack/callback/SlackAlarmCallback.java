package org.graylog2.plugins.slack.callback;

import com.google.common.collect.Lists;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugins.slack.SlackClient;
import org.graylog2.plugins.slack.SlackMessage;
import org.graylog2.plugins.slack.SlackPluginBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class SlackAlarmCallback extends SlackPluginBase implements AlarmCallback {
    private static final String DELIMITER = " | ";
    private Configuration configuration;

    @Override
    public void initialize(final Configuration config) throws AlarmCallbackConfigurationException {
        this.configuration = config;

        try {
            checkConfiguration(config);
        } catch (ConfigurationException e) {
            throw new AlarmCallbackConfigurationException("Configuration error. " + e.getMessage());
        }
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
        final SlackClient client = new SlackClient(configuration);

        SlackMessage message = new SlackMessage(
                configuration.getString(CK_COLOR),
                buildMessage(stream, result),
                configuration.getString(CK_CUSTOM_MESSAGE),
                configuration.getString(CK_CHANNEL)
        );

        // Add attachments if requested.
        final List<Message> backlogItems = getAlarmBacklog(result);

        try {
            client.send(message);
        } catch (SlackClient.SlackClientException e) {
            throw new RuntimeException("Could not send message to Slack.", e);
        }
    }

    protected List<Message> getAlarmBacklog(AlertCondition.CheckResult result) {
        final AlertCondition alertCondition = result.getTriggeredCondition();
        final List<MessageSummary> matchingMessages = result.getMatchingMessages();

        final int effectiveBacklogSize = Math.min(alertCondition.getBacklog(), matchingMessages.size());

        if (effectiveBacklogSize == 0) {
            return Collections.emptyList();
        }

        final List<MessageSummary> backlogSummaries = matchingMessages.subList(0, effectiveBacklogSize);

        final List<Message> backlog = Lists.newArrayListWithCapacity(effectiveBacklogSize);

        for (MessageSummary messageSummary : backlogSummaries) {
            backlog.add(messageSummary.getRawMessage());
        }

        return backlog;
    }

    public String buildMessage(Stream stream, AlertCondition.CheckResult result) {
        String titleLink = "_" + stream.getTitle() + "_";

        return "" + "*Alert for Graylog stream " + titleLink + "*:\n" + "> " + result.getResultDescription();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return configuration.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        /* Never actually called by graylog-server */
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        return configuration();
    }

    @Override
    public String getName() {
        return "Slack alarm callback";
    }
}
