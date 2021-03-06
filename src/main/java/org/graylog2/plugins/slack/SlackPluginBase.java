package org.graylog2.plugins.slack;

import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class SlackPluginBase {
    public static final String CK_WEBHOOK_URL = "webhook_url";
    public static final String CK_CHANNEL = "channel";
    public static final String CK_COLOR = "color";
    public static final String CK_CUSTOM_MESSAGE = "custom_message";

    protected static ConfigurationRequest configuration() {
        final ConfigurationRequest configurationRequest = new ConfigurationRequest();

        configurationRequest.addField(new TextField(
                CK_WEBHOOK_URL, "Webhook URL", "", "Slack \"Incoming Webhook\" URL",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new TextField(
                CK_CHANNEL, "Channel", "#channel", "Name of Slack #channel or @user for a direct message.",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new TextField(
                CK_COLOR, "Color", "#FF0000",
                "Color to use for Slack message",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new TextField(
                CK_CUSTOM_MESSAGE, "Custom message", null,
                "Add a custom message for the alert (overrides all the others)",
                ConfigurationField.Optional.OPTIONAL)
        );

        return configurationRequest;
    }

    protected static void checkConfiguration(Configuration configuration) throws ConfigurationException {
        if (!configuration.stringIsSet(CK_WEBHOOK_URL)) {
            throw new ConfigurationException(CK_WEBHOOK_URL + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(CK_CHANNEL)) {
            throw new ConfigurationException(CK_CHANNEL + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(CK_COLOR)) {
            throw new ConfigurationException(CK_COLOR + " is mandatory and must not be empty.");
        }
    }

    private static boolean isValidUriScheme(URI uri, String... validSchemes) {
        return uri.getScheme() != null && Arrays.binarySearch(validSchemes, uri.getScheme(), null) >= 0;
    }

    private static void checkUri(Configuration configuration, String settingName) throws ConfigurationException {
        if (configuration.stringIsSet(settingName)) {
            try {
                final URI uri = new URI(configuration.getString(settingName));
                if (!isValidUriScheme(uri, "http", "https")) {
                    throw new ConfigurationException(settingName + " must be a valid HTTP or HTTPS URL.");
                }
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Couldn't parse " + settingName + " correctly.", e);
            }
        }
    }

    protected String buildStreamLink(String baseUrl, Stream stream) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl + "streams/" + stream.getId() + "/messages?q=*&rangetype=relative&relative=3600";
    }

}
