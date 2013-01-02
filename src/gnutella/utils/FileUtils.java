package gnutella.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
	/**
	 * ファイルのMD5ハッシュ値を求める
	 * @param file 求める対象のファイル
	 * @return ファイルのMD5ハッシュ値
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static byte[] getFileMD5Digest(File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream in = new FileInputStream(file);
		byte[] date = new byte[256];
		int len;
		while ((len = in.read(date)) >= 0) {
			md.update(date, 0, len);
		}
		in.close();
		return md.digest();
	}

	/**
	 * MD5ハッシュ値のバイト配列を文字列表現にする
	 * @param digest MD5ハッシュ値
	 * @return MD5ハッシュ値のバイト配列を文字列表現
	 */
	public static String getDigestStringExpression(byte[] digest) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			int d = digest[i];
			if (d < 0) {
				d += 256;
			}
			if (d < 16) {
				System.out.print("0");
				sb.append("0");
			}
			sb.append(Integer.toString(d, 16));
		}
		return sb.toString();
	}
}
