package pl.com.ids;


import pl.com.ids.io.AugmentedFileWriter;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class FetchXML {
	private Debugger debugger;

	public FetchXML(Debugger debugger) {
		this.debugger = debugger;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Logger l = new Logger("in.txt");
		OptionsFetcher optionsFetcher = new OptionsFetcher(args);
		Debugger debugger = DebuggerFactory.getDebugger(args);

		FetchOptions fetchOptions = optionsFetcher.getFetchOptions(l, debugger);

		FetchXML fx = new FetchXML(debugger);
		fx.fetch(fetchOptions);
		l.logExit(0);
		// TODO Auto-generated method stub

	}


	public void fetch(FetchOptions fetchOptions) {
		StatusNotifier statusNotifier = new StatusNotifier(fetchOptions.getDiskFolder());
		statusNotifier.updateStatus(ProcessingStatus.WORKING);

		AugmentedFileWriter fileWriter = new AugmentedFileWriter(fetchOptions.getOverwrite(),
				fetchOptions.isDebug());

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
			store = getConnectedStore(fetchOptions.getService(), props, fetchOptions.isDebug());

			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message messages[] = folder.getMessages();
			for (int i = 0; i < messages.length && !limitReached; i++) {
				Message message = messages[i];
				String ct = message.getContentType();

				boolean debug = fetchOptions.isDebug();
				debugger.debug("---- Wiadomosc numer " + i + " -----");

				boolean senderVerified = verifySender(fetchOptions.getSender(), message, debug);
				boolean messageAvailable = !message.isSet(Flags.Flag.DELETED);
				if (senderVerified && messageAvailable) {

					DatesComparator dc = new DatesComparator();
					Date sentDate = message.getSentDate();

					if (!dc.between(fetchOptions.getDateLowLimit(), fetchOptions.getDateHighLimit(), sentDate)) {
						debugger.debug("Spoza zakresu " + sentDate);
						continue;
					}

					debugger.debug("Zawartosc" + ct);

					limitReached = saveAttachment(fetchOptions.getDiskFolder(), fetchOptions.getDelete(), fetchOptions.getSingle(),
							fileWriter, message, ct, fetchOptions.isDebug());
				}
			}
		} catch (NoSuchProviderException ex) {
			debugger.debug(ex.getMessage());

		} catch (MessagingException me) {
			debugger.debug(me.getMessage());
			me.printStackTrace();
		} catch (IOException ioe) {
			debugger.debug(ioe.getMessage());
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

	private Store getConnectedStore(Service service, Properties props, boolean debug) throws MessagingException {
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
								   Message message, String ct, boolean debug)
			throws IOException, MessagingException {
		boolean retrived = false;
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
						retrived = true;
					}
				} else {
					if (debug)
						System.out.println("Jestemy w else");
				}
			}
		}
		return retrived;
	}


	private boolean verifySender(String sender, Message message, boolean debug)
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
