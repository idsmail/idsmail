package pl.com.ids.domain;

import java.io.File;
import java.util.Optional;

public class FetchOptions {
    private final File diskFolder;
    private final String sender;
    private final String dateLowLimit;
    private final String dateHighLimit;
    private final Boolean overwrite;
    private final Boolean delete;
    private final Boolean single;
    private final Optional<Integer> timeout;
    private boolean debug;

    public FetchOptions(File diskFolder, String sender, String dateLowLimit, String dateHighLimit, Boolean overwrite, Boolean delete, Boolean single, Optional<Integer> timeout, boolean debug) {
        this.diskFolder = diskFolder;
        this.sender = sender;
        this.dateLowLimit = dateLowLimit;
        this.dateHighLimit = dateHighLimit;
        this.overwrite = overwrite;
        this.delete = delete;
        this.single = single;
        this.timeout = timeout;
        this.debug = debug;
    }

    public File getDiskFolder() {
        return diskFolder;
    }

    public String getSender() {
        return sender;
    }

    public String getDateLowLimit() {
        return dateLowLimit;
    }

    public String getDateHighLimit() {
        return dateHighLimit;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    public Boolean getDelete() {
        return delete;
    }

    public Boolean getSingle() {
        return single;
    }

    public Optional<Integer> getTimeout() {
        return timeout;
    }

    public boolean isDebug() {
        return debug;
    }
}
