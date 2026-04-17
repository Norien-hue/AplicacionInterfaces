package com.javafx.reciWins.utiles;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utilidad para hashear y verificar contraseñas con BCrypt.
 * Genera hashes en formato $2b$ (compatible con Node bcrypt usado por reci_app)
 * y verifica también hashes $2a$ y $2y$ existentes.
 */
public class BCryptUtils {

    public static final int COST = 10;

    private BCryptUtils() {}

    /**
     * Genera el hash BCrypt ($2b$) de la contraseña indicada.
     */
    public static String hash(String plainPassword) {
        return BCrypt.with(BCrypt.Version.VERSION_2B)
                .hashToString(COST, plainPassword.toCharArray());
    }

    /**
     * Verifica una contraseña en texto plano contra un hash almacenado.
     * Acepta hashes $2a$, $2b$ y $2y$.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash);
            return result.verified;
        } catch (Exception e) {
            return false;
        }
    }
}
