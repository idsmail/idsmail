package pl.com.ids.infrastructure;

import java.io.*;

public class IdsLogger {
    private BufferedWriter bwr;
    private String filename;
    private String logFolder = "";

    /*
     * Zwracane parametry
     */
    private static final int ERR_SYNTAX_ERROR = 2; // blad skladni
    static final int ERR_FILE_NOT_FOUND = 3; // blad nie znaleziono pliku
    private static final int ERR_FILE_NOT_SENT = 4; // nie wyslano pliku

    public IdsLogger(final String filename) {
        this.filename = filename;
    }

    private String addSlash(String folder) {
        char lastChar = folder.charAt(folder.length() - 1);
        if (lastChar != '\\') {
            folder += "\\";
        }
        return folder;
    }

    public void setLogFolder(File logFolder) {
        if (logFolder != null) {
            this.logFolder = addSlash(logFolder.getAbsolutePath());
        } else {
            this.logFolder = null;
        }
    }

    public void logSuccess() {
        logExit(0);
    }

    void doExit(int result) {
        System.exit(result);
    }
    private void logExit(int result) {
        try {
            bwr = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logFolder + filename)));
            bwr.write("" + result);
            bwr.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        doExit(result);
    }

    public void logSyntaxError(String details) {
        logExit(ERR_SYNTAX_ERROR, details);
    }

    public void logFileNotFound(String details) {
        logExit(ERR_FILE_NOT_FOUND, details);
    }

    void logExit(int result, String details) {
        BufferedWriter bwrExtended = null;
        try {
            bwrExtended = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("outDetails.txt")));
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf.getMessage());
            System.exit(result);
        }
        try {
            if (bwr != null) {
                bwr.write("" + result);
                bwr.close();
            }
            bwrExtended.write(details);
            bwrExtended.close();
            System.exit(result);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }

    }

    public void logSyntaxError() {
        logExit(ERR_SYNTAX_ERROR);
    }

    public void returnSyntaxErrorCode() {
        System.exit(ERR_SYNTAX_ERROR);
    }

    public void logFileNotFound() {
        logExit(ERR_FILE_NOT_FOUND);
    }

    public void logFileNotSent() {
        logExit(ERR_FILE_NOT_SENT);
    }


}
