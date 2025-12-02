package bg.sit_varna.sit.si.constant;

public enum NotificationChannel {
    EMAIL("html"),
    SMS("txt"),
    TELEGRAM("txt");

    private final String extension;

    NotificationChannel(String extension) {
        this.extension = extension;
    }

    public String getExtension() { return extension; }
    public String getFolderName() { return this.name().toLowerCase(); }
}
