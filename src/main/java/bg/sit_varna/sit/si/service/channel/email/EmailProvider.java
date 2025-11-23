package bg.sit_varna.sit.si.service.channel.email;

public enum EmailProvider {
    SENDGRID("sendgrid");

    private final String provider;

    EmailProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
