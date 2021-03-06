package com.auth0.android.lock.internal.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.auth0.android.util.CheckHelper.checkArgument;

public class Connection implements BaseConnection, DatabaseConnection, OAuthConnection, PasswordlessConnection {

    private String strategy;
    private String name;
    private Map<String, Object> values;
    private int minUsernameLength;
    private int maxUsernameLength;
    private boolean isCustomDatabase;
    private boolean allowActiveFlow = true;
    private PasswordComplexity passwordComplexity;

    private Connection(@NonNull String strategy, Map<String, Object> values) {
        checkArgument(values != null && values.size() > 0, "Must have at least one value");
        final String name = (String) values.remove("name");
        checkArgument(name != null, "Must have a non-null name");
        this.strategy = strategy;
        this.name = name;
        this.values = values;
        parseUsernameLength();
        parsePasswordComplexity();
    }

    private void parsePasswordComplexity() {
        int policy = PasswordStrength.NONE;
        String value = valueForKey("passwordPolicy", String.class);
        if ("excellent".equals(value)) {
            policy = PasswordStrength.EXCELLENT;
        }
        if ("good".equals(value)) {
            policy = PasswordStrength.GOOD;
        }
        if ("fair".equals(value)) {
            policy = PasswordStrength.FAIR;
        }
        if ("low".equals(value)) {
            policy = PasswordStrength.LOW;
        }

        final Map<String, Object> complexityOptions = valueForKey("password_complexity_options", Map.class);
        Integer minLength = null;
        if (complexityOptions != null && complexityOptions.containsKey("min_length")) {
            minLength = ((Number) complexityOptions.remove("min_length")).intValue();
        }
        passwordComplexity = new PasswordComplexity(policy, minLength);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStrategy() {
        return strategy;
    }

    /**
     * Getter for the Type of Connection
     *
     * @return the connection Type.
     */
    @AuthType
    int getType() {
        switch (strategy) {
            case "auth0":
                return AuthType.DATABASE;
            case "sms":
            case "email":
                return AuthType.PASSWORDLESS;
            case "ad":
            case "adfs":
            case "auth0-adldap":
            case "custom":
            case "google-apps":
            case "google-openid":
            case "ip":
            case "mscrm":
            case "office365":
            case "pingfederate":
            case "samlp":
            case "sharepoint":
            case "waad":
                return AuthType.ENTERPRISE;
            default:
                return AuthType.SOCIAL;
        }
    }

    @Nullable
    public <T> T valueForKey(@NonNull String key, @NonNull Class<T> tClazz) {
        final Object value = this.values.get(key);
        return tClazz.isInstance(value) ? tClazz.cast(value) : null;
    }

    @Override
    public boolean booleanForKey(@NonNull String key) {
        final Boolean value = valueForKey(key, Boolean.class);
        return value != null && value;
    }

    @Override
    @NonNull
    public PasswordComplexity getPasswordComplexity() {
        return passwordComplexity;
    }

    @Override
    public boolean requiresUsername() {
        return booleanForKey("requires_username");
    }

    @Override
    public boolean showSignUp() {
        return booleanForKey("showSignup");
    }

    @Override
    public boolean showForgot() {
        return booleanForKey("showForgot");
    }

    @Override
    public int getMinUsernameLength() {
        return minUsernameLength;
    }

    @Override
    public int getMaxUsernameLength() {
        return maxUsernameLength;
    }

    @Override
    public boolean isCustomDatabase() {
        return isCustomDatabase;
    }

    @Override
    public boolean isActiveFlowEnabled() {
        return false;
    }

    void disableActiveFlow() {
        this.allowActiveFlow = false;
    }

    @Override
    public Set<String> getDomainSet() {
        Set<String> domains = new HashSet<>();
        String domain = valueForKey("domain", String.class);
        if (domain != null) {
            domains.add(domain.toLowerCase());
            List<String> aliases = valueForKey("domain_aliases", List.class);
            if (aliases != null) {
                for (String alias : aliases) {
                    domains.add(alias.toLowerCase());
                }
            }
        }
        return domains;
    }

    /**
     * Creates a new Connection given a Strategy name and the map of values.
     *
     * @param strategy strategy name for this connection
     * @param values   additional values associated to this connection
     * @return a new instance of Connection. Can be either DatabaseConnection, PasswordlessConnection or OAuthConnection.
     */
    static Connection newConnectionFor(@NonNull String strategy, Map<String, Object> values) {
        return new Connection(strategy, values);
    }

    private void parseUsernameLength() {
        Map<String, Object> validations = valueForKey("validation", Map.class);
        if (validations == null || !validations.containsKey("username")) {
            isCustomDatabase = true;
            minUsernameLength = 1;
            maxUsernameLength = Integer.MAX_VALUE;
            return;
        }

        final Map<String, Object> usernameValidation = (Map<String, Object>) validations.get("username");
        minUsernameLength = intValue(usernameValidation.get("min"), 0);
        maxUsernameLength = intValue(usernameValidation.get("max"), 0);
        if (minUsernameLength < 1 || maxUsernameLength < 1 || minUsernameLength > maxUsernameLength) {
            minUsernameLength = 1;
            maxUsernameLength = Integer.MAX_VALUE;
        }
    }

    /**
     * Will try to get the int value of a given object. If the value cannot be obtained, it will return the default value.
     *
     * @param object       to get an int from.
     * @param defaultValue to return if the int value cannot be obtained.
     * @return the int value of the object or the default value if it cannot be obtained.
     */
    private int intValue(@Nullable Object object, int defaultValue) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        if (object instanceof String) {
            return Integer.parseInt((String) object, 10);
        }
        return defaultValue;
    }

}
