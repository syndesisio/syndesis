package io.syndesis.server.endpoint.v1.handler.setup;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;

import org.springframework.boot.autoconfigure.social.SocialProperties;

public final class OAuthApp {
    private static final class OAuthProperties extends SocialProperties {

        public OAuthProperties(final String clientId, final String clientSecret) {
            setAppId(clientId);
            setAppSecret(clientSecret);
        }

    }

    private String clientId;

    private String clientSecret;

    private String icon;

    private String id;

    private String name;

    public OAuthApp() {
    }

    OAuthApp(final Connector connector) {
        id = connector.getId().get();
        name = connector.getName();
        icon = connector.getIcon();
        clientId = connector.propertyTaggedWith(Credentials.CLIENT_ID_TAG).orElse(null);
        clientSecret = connector.propertyTaggedWith(Credentials.CLIENT_SECRET_TAG).orElse(null);
    }

    public SocialProperties asSocialProperties() {
        return new OAuthProperties(clientId, clientSecret);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
