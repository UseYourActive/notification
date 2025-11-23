package bg.sit_varna.sit.si.service.channel.sms;

public enum SmsProvider {
    TWILIO("twilio");

    private final String provider;

    SmsProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
