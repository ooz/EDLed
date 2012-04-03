package edled;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Launcher extends Thread {
	
	private final static String MAC_OS_X = "Mac OS X";
	private final static String MAC_OS_X_OPTIONS = "-Xdock:name=\"EDLed\" -Xdock:icon=res/img/edled.png ";
	
	private final InputStream is;
	private final PrintStream os;
	
	/**
	 * Constructor.
	 * 
	 * @param from Redirects this InputStream to the PrintStream to.
	 * @param to
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
	 * @param args
	 */
	public static void main(String[] argv) {
		List<String> args = Arrays.asList(argv);
		List<String> jvmParams = filterJVMParams(args);
		args.removeAll(jvmParams);
		
		String osSpecific = generateOSSpecific();
		String cmd = "java "
				   + "-cp "
				   + ".:edled.jar:plugin/:res/lib/ "
				   + osSpecific
				   + joinArgs(jvmParams)
				   + "edled.Application "
				   + joinArgs(args);
				   ;
		
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
	
	private static List<String> filterJVMParams(final List<String> args) {
		List<String> opts = new LinkedList<String>();
		
		for (String arg : args) {
			if (arg.startsWith("-")) {
				opts.add(arg);
			}
		}
		
		return opts;
	}
	
	private static String generateOSSpecific() {
		if (System.getProperty("os.name").startsWith(MAC_OS_X)) {
			return MAC_OS_X_OPTIONS;
		}
		
		return "";
	}
	
	private static String joinArgs(List<String> args) {
		StringBuffer sb = new StringBuffer();
		
		for (String arg : args) {
			sb.append(arg + " ");
		}
		
		return sb.toString();
	}

}
