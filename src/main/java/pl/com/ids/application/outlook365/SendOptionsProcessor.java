package pl.com.ids.application.outlook365;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import pl.com.ids.infrastructure.Debugger;
import pl.com.ids.infrastructure.IdsLogger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class SendOptionsProcessor {

    public static final String FILE_NAME = "file_name";
    public static final String ADDRESS_TO = "address_to";
    public static final String DEBUG = "debug";

    public Properties getSendOptions(String[] args, IdsLogger log, Debugger debugger) throws IOException {
        OptionParser parser = new OptionParser();

        OptionSpec<File> configFile = parser.accepts("c", "[argument z parametrem] lokalizacja pliku z konfiguracją").withRequiredArg().ofType(File.class).defaultsTo(new File("app.cfg"));
        OptionSpec<Void> opVerbose = parser.accepts("v", "[przelacznik] wlacz tryb gadatliwy");

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException e) {
            debugger.debug(e.getMessage());
            String usage = printUsage(parser);
            log.logSyntaxError(usage);
        }

        List<?> otherArgs = options.nonOptionArguments();
        if (otherArgs.size() != 2) {
            printUsage(parser);
            debugger.debug("Zbyt malo parametrow, wymagane 2");
            log.logSyntaxError();
        }

        final File oauthConfig = options.valueOf(configFile);


        Properties props = new Properties();
        try {
            props.load(new FileReader(oauthConfig));
        } catch (IOException e) {
            e.printStackTrace();
            log.logFileNotFound("Nie znaleziono pliku konfiguracyjnego " + oauthConfig.getAbsolutePath());
        }
        props.setProperty(FILE_NAME, (String) otherArgs.get(0));
        props.setProperty(ADDRESS_TO, (String) otherArgs.get(1));
        props.setProperty(DEBUG, options.has(opVerbose) ? "true" : "false");
        return props;
    }

    private String printUsage(OptionParser parser) throws IOException {
        final String usage = "Usage: sendXML file_name address_to [-c plik_config]";
        System.out.println(usage);
        parser.printHelpOn(System.out);
        return usage;
    }
}
