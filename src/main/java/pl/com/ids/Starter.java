package pl.com.ids;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Starter {

	private String fileName;
	private String address;

	/**
	 * @param args
	 */

	/*
	 * Zwracane parametry
	 */
	/*
	 * Blad skladni
	 */
	private static final int ERR_SYNTAX_ERROR = 2;
	private static final int ERR_FILE_NOT_FOUND = 3;

	private static void printUsage() {
		System.err
				.println("Usage: sendXML [-d,--debug] [{-v,--verbose}] [{--alt}] [{--name} a_name]\n"
						+ "                  [{-s,--size} a_number] fileName address");
	}

	public static void main(String[] args) {
		try {
			BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("out.txt")));

			bwr.write("Hello");
			bwr.close();
		} catch (FileNotFoundException fnf) {
			System.out.println(fnf);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		System.out.println("Halo");
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
		/*CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option opHost = parser.addStringOption('h', "host");
		CmdLineParser.Option opAuth = parser.addBooleanOption('a', "auth");
		CmdLineParser.Option opUser = parser.addStringOption('u', "user");
		CmdLineParser.Option opPassword = parser.addStringOption('p', "pass");

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(ERR_SYNTAX_ERROR);
		}
		Boolean auth = (Boolean) parser.getOptionValue(opAuth);
		String user = "";
		String password = "";
		String host = "";
		if (auth) {
			user = (String) parser.getOptionValue(opUser);
			password = (String) parser.getOptionValue(opPassword);
		}
		host = (String) parser.getOptionValue(opHost);
		String[] otherArgs = parser.getRemainingArgs();
		System.out.println("remaining args: (  " + otherArgs.length + ")");
		for (int i = 0; i < otherArgs.length; ++i) {
			System.out.println(otherArgs[i]);
		}
		
		if (otherArgs.length != 2) {
			printUsage();
			System.exit(2);
		}

		File f = new File(otherArgs[0]);
		if (!f.exists()) {
			System.exit(ERR_FILE_NOT_FOUND);
		}
		System.out.println(f.exists());
		System.out.println("User: " + user);
		System.out.println("Password: " + password);
		System.out.println("Host: " + host);
*/
	}
}
