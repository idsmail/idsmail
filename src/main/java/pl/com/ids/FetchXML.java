package pl.com.ids;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import pl.com.ids.io.AugmentedFileWriter;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class FetchXML {
	private static boolean debug = false;
	private static final int ERR_SYNTAX_ERROR = 2;
	private static final int ERR_FILE_NOT_FOUND = 3;

	static private String msg = "";

	private static void printUsage(OptionParser parser) throws IOException {
		final String usage = "Usage: fetchXML [-c] [-o] [-d] [-s] [-p IMAPS|POP3|POP3S] mail_server_name  user pass \n"
				+ "                   folder_name [sender] [-f date_from] [-t date_to] [-v] -l log_folder";
		System.out.println(usage);
		parser.printHelpOn(System.out);
		msg += usage + "\n";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Logger l = new Logger("in.txt");
		OptionParser parser = new OptionParser();

		OptionSpec<Void> opOverwrite = parser.accepts("o", "[przelacznik] nadpisuj plik na dysku podczas pobierania załącznika z tą samą nazwą");
		OptionSpec<Void> opDelete = parser.accepts("d", "[przelacznik] kasuje plik po pobraniu go z serwera");
		OptionSpec<Void> opSingle = parser.accepts("s", "[przelacznik] pobierz tylko jeden plik z serwera");
		OptionSpec<Protocol> opImap = parser.accepts("p", "[argument z parametrem] wybierz protokol pobiernia, poprawne wartosci IMAPS, POP3, POP3S").withRequiredArg().ofType(Protocol.class);
		OptionSpec<Void> opTls = parser.accepts("c", "[przelacznik] skorzystaj z tls ");

		OptionSpec<Integer> opTimeout = parser.accepts("m", "[argument z parametrem] czas oczekiwania na odpowiedz serwera (timeout)").withRequiredArg().ofType(Integer.class).defaultsTo(-1);


		OptionSpec<File> opLogFolder = parser.accepts("l", "[argument z parametrem] docelowy folder dla logow").withRequiredArg().ofType(File.class);
		OptionSpec<Void>  opVerbose = parser.accepts("v", "[przelacznik] wlacz tryb gadatliwy");
		OptionSpec<String>  opDateFrom = parser.accepts("f", "[argument z parametrem] pobierz od daty").withRequiredArg();
		OptionSpec<String>  opDateTo = parser.accepts("t", "[argument z parametrem] pobierz do daty").withRequiredArg();

		OptionSet options = null;
		try {
			 options = parser.parse(args);
		} catch (joptsimple.OptionException e) {
			System.out.println(e.getMessage());
			printUsage(parser);
			l.logExit(ERR_SYNTAX_ERROR, msg);
		}
		if (options.nonOptionArguments().size() < 4) {
			printUsage(parser);
			l.logExit(ERR_SYNTAX_ERROR, msg);
		}

		final File logFolder = options.valueOf(opLogFolder);
		l.setLogFolder(logFolder);
		Boolean verbose = options.has(opVerbose);

		if (verbose != null) {
			debug = verbose.booleanValue();
		}

		int i = 0;

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
			l.logExit(ERR_FILE_NOT_FOUND, msg);
		}
		Boolean overwrite = options.has(opOverwrite);
		Boolean delete = options.has(opDelete);
		Boolean single = options.has(opSingle);
		String dateFrom = options.valueOf(opDateFrom);
		String dateTo = options.valueOf(opDateTo);

		Optional<Integer> timeout = Optional.ofNullable(options.valueOf(opTimeout)).map(to -> to * 1000);

		FetchXML fx = new FetchXML();
		Protocol protocol = options.valueOf(opImap);
		Boolean useTls = options.has(opTls);
		Service service = new Service(host, -1, user, password, protocol, useTls);
		fx.fetch(
				new FetchOptions(service, folder, sender, dateFrom, dateTo, overwrite, delete, single, timeout));
		l.logExit(0);
		// TODO Auto-generated method stub

	}

	public void fetch(FetchOptions fetchOptions) {
		StatusNotifier statusNotifier = new StatusNotifier(fetchOptions.getDiskFolder());
		statusNotifier.updateStatus(ProcessingStatus.WORKING);

		AugmentedFileWriter fileWriter = new AugmentedFileWriter(fetchOptions.getOverwrite(),
				debug);

		Properties props = new Properties();
		fetchOptions.getTimeout().ifPresent(to -> {
				props.setProperty("mail.pop3.connectiontimeout", to.toString());
				props.setProperty("mail.pop3.timeout", to.toString());
		});

		if (fetchOptions.getService().getUseTls()) {
			props.setProperty("mail.pop3.starttls.enable", "true");
		}

		if (fetchOptions.getService().getProtocol() == Protocol.POP3S) {
			props.put("mail.pop3s.ssl.trust", "*");

			props.setProperty("mail.pop3.socketFactory.fallback", "false");
		}
		Folder folder = null;
		boolean limitReached = false;
		Store store = null;
		try {
			store = getConnectedStore(fetchOptions.getService(), props);

			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message messages[] = folder.getMessages();
			for (int i = 0; i < messages.length && !limitReached; i++) {
				Message message = messages[i];
				String ct = message.getContentType();

				debug("---- Wiadomosc numer " + i + " -----");

				boolean senderVerified = verifySender(fetchOptions.getSender(), message);
				boolean messageAvailable = !message.isSet(Flags.Flag.DELETED);
				if (senderVerified && messageAvailable) {

					DatesComparator dc = new DatesComparator();
					Date sentDate = message.getSentDate();

					if (!dc.between(fetchOptions.getDateLowLimit(), fetchOptions.getDateHighLimit(), sentDate)) {
						debug("Spoza zakresu " + sentDate);
						continue;
					}

					debug("Zawartosc" + ct);

					limitReached = saveAttachment(fetchOptions.getDiskFolder(), fetchOptions.getDelete(), fetchOptions.getSingle(),
							fileWriter, limitReached, message, ct);
				}
			}
		} catch (NoSuchProviderException ex) {
			System.out.println(ex);

		} catch (MessagingException me) {
			System.out.println(me);
			me.printStackTrace();
		} catch (IOException ioe) {
			System.out.println(ioe);
			ioe.printStackTrace();
		} finally {
			statusNotifier.updateStatus(ProcessingStatus.DONE);
			try {
				if (folder != null) {
					folder.close(true);
				}
				store.close();
			} catch (MessagingException me) {

			}
		}
	}

	private Store getConnectedStore(Service service, Properties props) throws MessagingException {
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);

		Store store =  session.getStore(service.getUrl());
		if (debug) {
            System.out.println("Connect" + service.getHost() + " ; " + service.getUsername());
        }
		store.connect();
		return store;
	}

	private boolean saveAttachment(File diskFolder, Boolean delete,
			Boolean single, AugmentedFileWriter fileWriter,
			boolean limitReached, Message message, String ct)
			throws IOException, MessagingException {
		if (ct.startsWith("multipart")) {
			Multipart mp = (Multipart) message.getContent();
			// Iterujemy po zawarto�ci listu
			for (int j = 0, n = mp.getCount(); j < n; j++) {
				Part part = mp.getBodyPart(j);

				String disposition = part.getDisposition();
				if (debug)
					System.out.println("Tutaj :" + part.getContentType() + " "
							+ part.getFileName() + " " + disposition);

				if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {

					if (debug)
						System.out.println("Jestemy w save");

					fileWriter.storeFile(diskFolder, part.getFileName(),
							part.getInputStream());
					if (delete != null && delete.booleanValue()) {
						message.setFlag(Flags.Flag.DELETED, true);
					}
					if (single) {
						limitReached = true;
					}
				} else {
					if (debug)
						System.out.println("Jestemy w else");
				}
			}
		}
		return limitReached;
	}

	private void debug(String dbgMsg) {
		if (debug) {
			System.out.println(dbgMsg);
		}
	}

	private boolean verifySender(String sender, Message message)
			throws MessagingException {
		final boolean verificationResult;

		if (sender == null) {
			verificationResult = true;
		} else {
			Address[] fromList = message.getFrom();

			String from = "";
			for (int j = 0; j < fromList.length; j++) {
				from = from + fromList[j];
			}

			if (debug) {
				System.out.println("Nadawca " + from);
			}
			verificationResult = from.contains(sender);

		}
		return verificationResult;

	}

}
