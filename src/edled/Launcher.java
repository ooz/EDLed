package edled;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

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
	public static void main(String[] args) {
		String osSpecific = "";
		if (System.getProperty("os.name").startsWith(MAC_OS_X)) {
			osSpecific = MAC_OS_X_OPTIONS;
		}
		
		String argv = "";
		String cmd = "java "
				   + "-cp "
				   + ".:edled.jar:plugin/:res/lib/ "
				   + osSpecific
				   + "-Xmx1g "
				   + "edled.Application "
				   + argv
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

}
