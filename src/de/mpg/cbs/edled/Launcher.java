package de.mpg.cbs.edled;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that launches the edled.Application class in a separate process.
 * This enables configuration of the JVM. All CLI output is redirected
 * to the launcher thread.
 * 
 * @author Oliver Z.
 */
public class Launcher extends Thread {
	
	/** Indicator for Apple's Mac OS X in system property "os.name". */
	private final static String MAC_OS_X = "Mac OS X";
	/** Additional JVM options for Mac OS X. */
	private final static String MAC_OS_X_OPTIONS = "-Xdock:name=\"EDLed\" -Xdock:icon=res/img/edled.png ";
	
	/** InputStream that should be redirection to PrintStream os. */
	private final InputStream is;
	/** Target for the redirection of is. */
	private final PrintStream os;
	
	/**
	 * Constructor.
	 * 
	 * @param from Redirects this InputStream to the PrintStream to.
	 * @param to   PrintStream to take the input from from.
	 */
	Launcher(InputStream from, PrintStream to) {
		this.is = from;
		this.os = to;
	}
	
	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				this.os.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launcher entry point.
	 * 
	 * @param argv Command line arguments.
	 */
	public static void main(String[] argv) {
		List<String> args = new LinkedList<String>(Arrays.asList(argv));
		List<String> jvmParams = filterJVMParams(args);
		args.removeAll(jvmParams);
		
		String osSpecific = generateOSSpecific();
		String cmd = "java "
				   + "-cp "
				   + ".:edled.jar:plugin/:res/lib/ "
				   + osSpecific
				   + joinArgs(jvmParams)
				   + "de.mpg.cbs.edled.Application "
				   + joinArgs(args);
		
		String appPath = "";
		String execDir = System.getProperty("user.dir") 
							+ System.getProperty("file.separator") 
							+ appPath;
		
		Runtime rt = Runtime.getRuntime();

		try {
			Process p = rt.exec(cmd,
								null,
								new File(execDir));
			
			Launcher stdout = new Launcher(p.getInputStream(), System.out);
			Launcher stderr = new Launcher(p.getErrorStream(), System.err);
			stdout.start();
			stderr.start();
			
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Filters all command line parameters that start with a minus ("-")
	 * indicating JVM options.
	 * 
	 * @param args List of all launcher parameters.
	 * @return     List of Strings only containing parameters starting with a 
	 * 			   minus ("-").
	 */
	private static List<String> filterJVMParams(final List<String> args) {
		List<String> opts = new LinkedList<String>();
		
		for (String arg : args) {
			if (arg.startsWith("-")) {
				opts.add(arg);
			}
		}
		
		return opts;
	}
	
	/**
	 * Yields a String with additional operation system specific JVM options.
	 * 
	 * @return String containing OS specific flags for the JVM.
	 * 		   Empty string if there are none for the host system.
	 */
	private static String generateOSSpecific() {
		if (System.getProperty("os.name").startsWith(MAC_OS_X)) {
			return MAC_OS_X_OPTIONS;
		}
		
		return "";
	}
	
	/**
	 * Joins a List of Strings to a single String where each original String
	 * is separated by a space character.
	 * 
	 * @param args List of Strings to join.
	 * @return     String representing args with each original String being
	 * 			   separated by a space character.
	 */
	private static String joinArgs(List<String> args) {
		StringBuffer sb = new StringBuffer();
		
		for (String arg : args) {
			sb.append(arg + " ");
		}
		
		return sb.toString();
	}

}
