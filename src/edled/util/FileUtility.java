package edled.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class FileUtility {
	
	private static final Logger logger = Logger.getLogger(FileUtility.class);
	
	/*
	 * See:
	 * http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls/1290311
	 */
	/**
     * Get the relative path from one file to another, specifying the directory separator. 
     * If one of the provided resources does not exist, it is assumed to be a file unless it ends with '/' or
     * '\'.
     * 
     * @param target targetPath is calculated to this file
     * @param base basePath is calculated from this file
     * @param separator directory separator. The platform default is not assumed so that we can test Unix behaviour when running on Windows (for example)
     * @return
     */
	public static String relativize(final String targetPath, 
									final String basePath,
									final String fileSeparator) 
		throws PathRelativizationException {
		

        // Normalize the paths
        String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
        String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

        // Undo the changes to the separators made by normalization
        if (fileSeparator.equals("/")) {
            normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
            normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

        } else if (fileSeparator.equals("\\")) {
            normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
            normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

        } else {
            throw new IllegalArgumentException("Unrecognised dir separator '" + fileSeparator + "'");
        }

        String[] base = normalizedBasePath.split(Pattern.quote(fileSeparator));
        String[] target = normalizedTargetPath.split(Pattern.quote(fileSeparator));

        // First get all the common elements. Store them as a string,
        // and also count how many of them there are.
        StringBuffer common = new StringBuffer();

        int commonIndex = 0;
        while (commonIndex < target.length && commonIndex < base.length
                && target[commonIndex].equals(base[commonIndex])) {
            common.append(target[commonIndex] + fileSeparator);
            commonIndex++;
        }

        if (commonIndex == 0) {
            // No single common path element. This most
            // likely indicates differing drive letters, like C: and D:.
            // These paths cannot be relativized.
            throw new PathRelativizationException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath
                    + "'");
        }   

        // The number of directories we have to backtrack depends on whether the base is a file or a dir
        // For example, the relative path from
        //
        // /foo/bar/baz/gg/ff to /foo/bar/baz
        // 
        // ".." if ff is a file
        // "../.." if ff is a directory
        //
        // The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
        // the resource referred to by this path may not actually exist, but it's the best I can do
        boolean baseIsFile = true;

        File baseResource = new File(normalizedBasePath);

        if (baseResource.exists()) {
            baseIsFile = baseResource.isFile();

        } else if (basePath.endsWith(fileSeparator)) {
            baseIsFile = false;
        }

        StringBuffer relative = new StringBuffer();

        if (base.length != commonIndex) {
            int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

            for (int i = 0; i < numDirsUp; i++) {
                relative.append(".." + fileSeparator);
            }
        }
        relative.append(normalizedTargetPath.substring(common.length()));
        return relative.toString();

	}
	
	@SuppressWarnings("serial")
	public static class PathRelativizationException extends RuntimeException {

		PathRelativizationException(String msg) {
            super(msg);
        }
    }
	
	public static Map<String, String> readMapFile(final String path) {
		return FileUtility.readMapFile(new File(path));
	}
	public static Map<String, String> readMapFile(final File file) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = read(reader);
			while (line != null) {
				String key = line.trim();
				String value = read(reader).trim();
				if (value != null) {
					map.put(key, value);
				}
				line = read(reader);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.warn("Could not find map file at " + file.getPath(), e);
		} catch (IOException e) {
			logger.warn("I/O error related to " + file.getPath(), e);
		}
		
		return map;
	}
	/**
	 * Reads a line from a buffered reader. Ignoring empty lines or
	 * lines starting with '#'.
	 * 
	 * @param reader A BufferedReader to read from.
	 * @return		 A String representing the read line. 
	 * 				 Null if no line could be read.
	 */
	private static String read(final BufferedReader reader) {
		String line = null;
		try {
			line = reader.readLine();
			while (line != null) {
				if (line.startsWith("#") || line.trim().equals("")) {
					line = reader.readLine();
				} else {
					return line;
				}
			}
		} catch (IOException e) {
			logger.warn("I/O error!", e);
		}
		
		return line;
	}

}
