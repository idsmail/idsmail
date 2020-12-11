package pl.com.ids.application.standard;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import pl.com.ids.Protocol;
import pl.com.ids.domain.Configuration;
import pl.com.ids.domain.FetchOptions;
import pl.com.ids.infrastructure.Debugger;
import pl.com.ids.infrastructure.IdsLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class OptionsFetcher {
    private String[] args;

    private static final int ERR_SYNTAX_ERROR = 2;
    private static final int ERR_FILE_NOT_FOUND = 3;

    static private String msg = "";

    private Service service;

    private static void printUsage(OptionParser parser) throws IOException {
        final String usage = "Usage: fetchXML [-c] [-o] [-d] [-s] [-i] [-e] mail_server_name  user pass \n"
                + "                   folder_name [sender] [-f date_from] [-t date_to] [-v] -l log_folder";
        System.out.println(usage);
        parser.printHelpOn(System.out);
        msg += usage + "\n";
    }

    public OptionsFetcher(String[] args) {
        this.args = args;
    }

    public Configuration<Service> getFetchOptions(IdsLogger l, Debugger debugger) throws IOException {
        OptionParser parser = new OptionParser();

        OptionSpec<Void> opOverwrite = parser.accepts("o", "[przelacznik] nadpisuj plik na dysku podczas pobierania załącznika z tą samą nazwą");
        OptionSpec<Void> opDelete = parser.accepts("d", "[przelacznik] kasuje plik po pobraniu go z serwera");
        OptionSpec<Void> opSingle = parser.accepts("s", "[przelacznik] pobierz tylko jeden plik z serwera");
        OptionSpecBuilder opImap = parser.accepts("i", "[przelacznik] uzyj protokol impas");
        OptionSpec<Void> opSsl = parser.accepts("e", "[przelacznik] wlacz szyfrowanie SSL");
        OptionSpec<Void> opTls = parser.accepts("c", "[przelacznik] skorzystaj z tls ");
        OptionSpec<Integer> opPort = parser.accepts("p", "port to use").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> opTimeout = parser.accepts("m", "[argument z parametrem] czas oczekiwania na odpowiedz serwera (timeout)").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<Void> opDialog = parser.accepts("q", "[przelacznik] pokaz blad w oknie");


        OptionSpec<File> opLogFolder = parser.accepts("l", "[argument z parametrem] docelowy folder dla logow").withRequiredArg().ofType(File.class);
        OptionSpec<Void>  opVerbose = parser.accepts("v", "[przelacznik] wlacz tryb gadatliwy");
        OptionSpec<String>  opDateFrom = parser.accepts("f", "[argument z parametrem] pobierz od daty").withRequiredArg();
        OptionSpec<String>  opDateTo = parser.accepts("t", "[argument z parametrem] pobierz do daty").withRequiredArg();

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException e) {
            debugger.debug(e.getMessage());
            printUsage(parser);
            l.logSyntaxError(msg);
        }
        if (options.nonOptionArguments().size() < 4) {
            debugger.debug("Zbyt malo parametrow");
            printUsage(parser);
            l.logSyntaxError(msg);
        }

        final File logFolder = options.valueOf(opLogFolder);
        l.setLogFolder(logFolder);

        boolean debug = options.has(opVerbose);
        List<?> others = options.nonOptionArguments();
        final String host = (String) others.get(0);
        final String user = (String) others.get(1);
        final String password = (String) others.get(2);
        final File folder = new File((String) others.get(3));
        String sender;
        if (others.size() > 4) {
            sender = (String) others.get(4);
        } else {
            sender = null;
        }

        msg += (folder.getAbsolutePath() + " FILE EXISTS: " + folder.exists() + "\n");
        msg += ("User: " + user + "\n");
        msg += ("Password: " + password + "\n");
        msg += ("Host: " + host + "\n");
        msg += ("Output folder: " + folder.getAbsolutePath() + "\n");
        System.out.println(msg);

        if (!folder.exists()) {
            l.logFileNotFound(msg);
        }
        Boolean overwrite = options.has(opOverwrite);
        Boolean delete = options.has(opDelete);
        Boolean single = options.has(opSingle);
        String dateFrom = options.valueOf(opDateFrom);
        String dateTo = options.valueOf(opDateTo);

        Optional<Integer> timeout = Optional.ofNullable(options.valueOf(opTimeout)).map(to -> to * 1000);

        final Protocol protocol;
        boolean ssl = options.has(opSsl);
        if (options.has(opImap)) {
            protocol = Protocol.IMAPS;
        } else {
            protocol = ssl ? Protocol.POP3S : Protocol.POP3;
        }
        Boolean useTls = options.has(opTls);
        int port = options.has(opPort) ? options.valueOf(opPort) : -1;
        service = new Service(host, port, user, password, protocol, useTls);
        FetchOptions fetchOptions = new FetchOptions(folder, sender, dateFrom, dateTo, overwrite, delete, single, timeout, debug);
        return new Configuration(fetchOptions, service);
    }

    public Service getService() {
        return service;
    }
}
