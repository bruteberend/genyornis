package nl.alleveenstra.genyornis.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

/**
 * PasswordUtil
 * @author berend@berendscholte.nl
 * 
 */
public class PasswordUtil {

	public final static int GEN_PASSWORD_SIZE = 8;
	public final static String GEN_PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()";
	private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);

	public static String hashPassword(String password) {

		String hash = null;
		try {
			byte[] passwordAsBytes = password.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(passwordAsBytes);
			byte[] digest = md.digest();
			hash = new BASE64Encoder().encode(digest);

		} catch (Exception ex) {
			log.error("Hashing password failed", ex);
			hash = null;
		}
		return hash;
	}

	public static String generatePassword() {
		StringBuilder sb = new StringBuilder();
		Random rnd = new SecureRandom();

		for (int i = 0; i < GEN_PASSWORD_SIZE; i++) {
			sb.append(GEN_PASSWORD_CHARS.charAt(rnd.nextInt(GEN_PASSWORD_CHARS.length())));
		}

		return sb.toString();
	}
}
