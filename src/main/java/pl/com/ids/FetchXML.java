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
import java.util.Properties;

public class FetchXML {
	private static boolean debug = false;
	private static final int ERR_SYNTAX_ERROR = 2;
	private static final int ERR_FILE_NOT_FOUND = 3;
	private static final int ARGUMENTS_ERROR = 4;

	static private String msg = "";

	// private String host = "unikat.nazwa.pl";
	// private String username = "testowe@unikat.nazwa.pl";
	// private String password = "haslo.77";

	private static void printUsage() {
		final String usage = "Usage: fetchXML [-o] [-d] [-s] pop3_server_name  user pass \n"
				+ "                   folder_name [sender] [-f date_from] [-t date_to] [-v] -l log_folder";
		System.err.println(usage);
		msg += usage + "\n";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Logger l = new Logger("in.txt");
		OptionParser parser = new OptionParser();

		OptionSpec<Void> opOverwrite = parser.accepts("o");
		OptionSpec<Void> opDelete = parser.accepts("d");
		OptionSpec<Void> opSingle = parser.accepts("s");
		OptionSpec<Integer> opTimeout = parser.accepts("m").withRequiredArg().ofType(Integer.class).defaultsTo(-1);


		OptionSpec<File> opLogFolder = parser.accepts("l").withRequiredArg().ofType(File.class);
		OptionSpec<Void>  opVerbose = parser.accepts("v");
		OptionSpec<String>  opDateFrom = parser.accepts("f").withRequiredArg();
		OptionSpec<String>  opDateTo = parser.accepts("t").withRequiredArg();

		OptionSet options = parser.parse( args );

		if (options.nonOptionArguments().size() < 4) {
			printUsage();
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
/*
		if (options.has(opSender)) {
			sender = options.valueOf(opSender);
		} else {
			sender = null;
		}
*/

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

		Integer timeout = options.valueOf(opTimeout);

		FetchXML fx = new FetchXML();
		fx.fetch(host, user, password, folder, sender, dateFrom, dateTo,
				overwrite, delete, single, timeout, 110);
		l.logExit(0);
		// TODO Auto-generated method stub

	}

	public void fetch(String host, String username, String password,
					  File diskFolder, String sender, String dateLowLimit,
					  String dateHighLimit, Boolean overwrite, Boolean delete,
					  Boolean single, Integer timeout, int port) {
		StatusNotifier statusNotifier = new StatusNotifier(diskFolder);
		statusNotifier.updateStatus(ProcessingStatus.WORKING);

		AugmentedFileWriter fileWriter = new AugmentedFileWriter(overwrite,
				debug);

		Properties props = new Properties();
		props.setProperty("mail.pop3.connectiontimeout", timeout.toString());
		props.setProperty("mail.pop3.timeout", timeout.toString());

		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debug);
		Store store = null;
		Folder folder = null;
		boolean limitReached = false;
		try {
			store = session.getStore("pop3");
			if (debug) {
				System.out.println("Connect" + host + " ; " + username);
			}
			store.connect(host, port, username, password);
			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message messages[] = folder.getMessages();
			for (int i = 0; i < messages.length && !limitReached; i++) {
				Message message = messages[i];
				String ct = message.getContentType();

				debug("---- Wiadomosc numer " + i + " -----");

				boolean senderVerified = verifySender(sender, message);
				boolean messageAvailable = !message.isSet(Flags.Flag.DELETED);
				if (senderVerified && messageAvailable) {

					DatesComparator dc = new DatesComparator();
					Date sentDate = message.getSentDate();

					if (!dc.between(dateLowLimit, dateHighLimit, sentDate)) {
						debug("Spoza zakresu " + sentDate);
						continue;
					}

					debug("Zawartosc" + ct);

					limitReached = saveAttachment(diskFolder, delete, single,
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

				if (Part.ATTACHMENT.equals(disposition)) {

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
