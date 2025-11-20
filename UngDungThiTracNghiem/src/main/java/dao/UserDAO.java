package dao;

import entity.Room;
import entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class UserDAO extends Generic_DAO<User, String> {
    private static final String ALGORITHM = "AES";
    private static final String DEFAULT_KEY = "group01";
    private static SecretKeySpec secretKey;

    public UserDAO(Class<User> clazz) {
        super(clazz);
    }

    public UserDAO(EntityManager em,Class<User> clazz) {
        super(em, clazz);
    }

    private static void prepareSecreteKey() {
        try {
            byte[] key = DEFAULT_KEY.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            prepareSecreteKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting password", e);
        }
    }

    public static String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null) {
            return null;
        }
        try {
            prepareSecreteKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedPassword)));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting password", e);
        }
    }

    public User selectByAccount(String username, String password) {
        String encryptedPassword = encryptPassword(password);
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.userId = :username AND u.passwordHash = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", encryptedPassword)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean save(User user) {
        user.setPasswordHash(encryptPassword(user.getPasswordHash()));
        return super.save(user);
    }

    @Override
    public boolean update(User user) {
        // Assuming the password might not be changed every time.
        // If the passed user object has an unencrypted password, encrypt it.
        // This logic might need adjustment based on how User objects are handled in the service layer.
        // A common practice is to have a separate method for password changes.
        // For now, we check if the hash looks like a real hash or a new password.
        // This is not a very reliable check.
        User existingUser = findById(user.getUserId());
        if (existingUser != null && !existingUser.getPasswordHash().equals(user.getPasswordHash())) {
            user.setPasswordHash(encryptPassword(user.getPasswordHash()));
        }
        return super.update(user);
    }
}