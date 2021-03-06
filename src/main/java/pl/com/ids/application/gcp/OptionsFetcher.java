package pl.com.ids.application.gcp;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import pl.com.ids.domain.Configuration;
import pl.com.ids.infrastructure.Debugger;
import pl.com.ids.domain.FetchOptions;
import pl.com.ids.infrastructure.IdsLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class OptionsFetcher {
    private final String[] args;

    static private String msg = "";

    private static void printUsage(OptionParser parser) throws IOException {
        final String usage = "Usage: fetchXML  [-o] [-d] [-s] [-m] [-c plik_config]\n"
                + "                   folder_name [sender] [-f date_from] [-t date_to] [-v] [-l log_folder]";
        System.out.println(usage);
        parser.printHelpOn(System.out);
        msg += usage + "\n";
    }

    public OptionsFetcher(String[] args) {
        this.args = args;
    }

    Configuration<OAuthConfig> getFetchOptions(IdsLogger log, Debugger debugger) throws IOException {
        OptionParser parser = new OptionParser();

        OptionSpec<Void> opOverwrite = parser.accepts("o", "[przelacznik] nadpisuj plik na dysku podczas pobierania załącznika z tą samą nazwą");
        OptionSpec<Void> opDelete = parser.accepts("d", "[przelacznik] kasuje plik po pobraniu go z serwera");
        OptionSpec<Void> opSingle = parser.accepts("s", "[przelacznik] pobierz tylko jeden plik z serwera");
        OptionSpec<Integer> opTimeout = parser.accepts("m", "[argument z parametrem] czas oczekiwania na odpowiedz serwera (timeout)").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<File> configFile = parser.accepts("c", "[argument z parametrem] lokalizacja pliku z konfiguracją").withRequiredArg().ofType(File.class).defaultsTo(new File("app.cfg"));

        OptionSpec<File> opLogFolder = parser.accepts("l", "[argument z parametrem] docelowy folder dla logow").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<Void> opVerbose = parser.accepts("v", "[przelacznik] wlacz tryb gadatliwy");
        OptionSpec<String> opDateFrom = parser.accepts("f", "[argument z parametrem] pobierz od daty").withRequiredArg();
        OptionSpec<String> opDateTo = parser.accepts("t", "[argument z parametrem] pobierz do daty").withRequiredArg();

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException e) {
            debugger.debug(e.getMessage());
            printUsage(parser);
            log.logSyntaxError(msg);
        }
        if (options.nonOptionArguments().isEmpty()) {
            debugger.debug("Zbyt malo parametrow");
            printUsage(parser);
            log.logSyntaxError(msg);
        }


        final File logFolder = options.valueOf(opLogFolder);
        log.setLogFolder(logFolder);

        boolean debug = options.has(opVerbose);
        List<?> others = options.nonOptionArguments();
        final File folder = new File((String) others.get(0));
        String sender;
        if (others.size() > 1) {
            sender = (String) others.get(1);
        } else {
            sender = null;
        }

        msg += (folder.getAbsolutePath() + " FILE EXISTS: " + folder.exists() + "\n");
        msg += ("Output folder: " + folder.getAbsolutePath() + "\n");
        if (debug) {
            System.out.println(msg);
        }

        if (!folder.exists()) {
            log.logFileNotFound(msg);
        }
        Boolean overwrite = options.has(opOverwrite);
        Boolean delete = options.has(opDelete);
        Boolean single = options.has(opSingle);
        String dateFrom = options.valueOf(opDateFrom);
        String dateTo = options.valueOf(opDateTo);

        Optional<Integer> timeout = Optional.ofNullable(options.valueOf(opTimeout)).map(to -> to * 1000);
        FetchOptions fetchOptions = new FetchOptions(folder, sender, dateFrom, dateTo, overwrite, delete, single, timeout, debug);
        final File oauthConfig = options.valueOf(configFile);
        return new Configuration<>(fetchOptions, new OAuthConfig(oauthConfig));
    }

}
