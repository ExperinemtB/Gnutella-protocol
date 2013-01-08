package gnutella.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
	public static byte[] getFileMD5Digest(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream in = new FileInputStream(file);
		byte[] date = new byte[(int)Math.min(536870912,Math.max(256,file.length()/10))];
		int len;
		while ((len = in.read(date)) >= 0) {
			md.update(date, 0, len);
		}
		in.close();
		return md.digest();
	}
}
