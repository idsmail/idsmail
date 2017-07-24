package pl.com.ids;

public enum  Protocol {
    IMAPS("imaps"), POP3("pop3"), POP3S("pop3s");

    private String code;

    Protocol(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
