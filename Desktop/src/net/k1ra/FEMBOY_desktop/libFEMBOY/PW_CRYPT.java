package net.k1ra.FEMBOY_desktop.libFEMBOY;

import org.mindrot.jbcrypt.BCrypt;

/**
 * @author k1ra
 * @version 1
 *
 * Updatable password crypto functions
 */

abstract public class PW_CRYPT {

    static int salt_length = 13;

    public static String hash(final String in){
        return BCrypt.hashpw(in, BCrypt.gensalt(salt_length));
    }

    public static boolean verify(final String given, final String hash){
        return BCrypt.checkpw(given, hash.replace("$2y$","$2a$"));
    }
}
