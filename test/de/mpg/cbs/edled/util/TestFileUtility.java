package de.mpg.cbs.edled.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mpg.cbs.edled.util.FileUtility;
import de.mpg.cbs.edled.util.FileUtility.PathRelativizationException;


public class TestFileUtility {

	@BeforeClass
	public static void setUpBeforeClass() {
	}

	@AfterClass
	public static void tearDownAfterClass() {
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * The following test cases are from:
	 * http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls/1290311
	 */
	@Test
	public void testGetRelativePathsUnix() {
	    assertEquals("stuff/xyz.dat", FileUtility.relativize("/var/data/stuff/xyz.dat", "/var/data/", "/"));
	    assertEquals("../../b/c", FileUtility.relativize("/a/b/c", "/a/x/y/", "/"));
	    assertEquals("../../b/c", FileUtility.relativize("/m/n/o/a/b/c", "/m/n/o/a/x/y/", "/"));
	}
	
	@Test
	public void testGetRelativePathFileToFile() {
	    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
	    String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";

	    String relPath = FileUtility.relativize(target, base, "\\");
	    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
	}

	@Test
	public void testGetRelativePathDirectoryToFile() {
	    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
	    String base = "C:\\Windows\\Speech\\Common\\";

	    String relPath = FileUtility.relativize(target, base, "\\");
	    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
	}

	@Test
	public void testGetRelativePathFileToDirectory() {
	    String target = "C:\\Windows\\Boot\\Fonts";
	    String base = "C:\\Windows\\Speech\\Common\\foo.txt";

	    String relPath = FileUtility.relativize(target, base, "\\");
	    assertEquals("..\\..\\Boot\\Fonts", relPath);
	}

	@Test
	public void testGetRelativePathDirectoryToDirectory() {
	    String target = "C:\\Windows\\Boot\\";
	    String base = "C:\\Windows\\Speech\\Common\\";
	    String expected = "..\\..\\Boot";

	    String relPath = FileUtility.relativize(target, base, "\\");
	    assertEquals(expected, relPath);
	}

	@Test
	public void testGetRelativePathDifferentDriveLetters() {
	    String target = "D:\\sources\\recovery\\RecEnv.exe";
	    String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";

	    try {
	    	FileUtility.relativize(target, base, "\\");
	        fail();

	    } catch (PathRelativizationException ex) {
	        // expected exception
	    }
	}

}
