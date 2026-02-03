package edu.aitu.oop3.db;

public class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();

    private final String url;
    private final String user;
    private final String password;

    private AppConfig() {
        // Load from environment or defaults
        this.url = System.getenv().getOrDefault("DB_URL",
                "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require");
        this.user = System.getenv().getOrDefault("DB_USER", "postgres.wvkdnfzggynvqbuszecy");
        this.password = System.getenv().getOrDefault("DB_PASSWORD", "");
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
}
