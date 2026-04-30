package it.gov.pagopa.pu.common.pii.citizen.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class AESUtils {
  private AESUtils() {
  }

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;
  private static final int SALT_LENGTH_BYTE = 16;
  private static final String ALGORITHM_TYPE = "AES";
  private static final int KEY_LENGTH = 256;
  private static final int ITERATION_COUNT = 65536;
  private static final Charset UTF_8 = StandardCharsets.UTF_8;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static byte[] getRandomNonce(int length) {
    byte[] nonce = new byte[length];
    SECURE_RANDOM.nextBytes(nonce);
    return nonce;
  }

  private static final SecretKeyFactory SECRET_KEY_FACTORY;

  static {
    try {
      SECRET_KEY_FACTORY = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public static SecretKey getSecretKey(String password, byte[] salt) {
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);

    try {
      return new SecretKeySpec(SECRET_KEY_FACTORY.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
    } catch (InvalidKeySpecException e) {
      throw new IllegalStateException("Cannot initialize cryptographic data", e);
    }
  }

  public static byte[] encrypt(String password, String plainMessage) {
    byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
    SecretKey secretKey = getSecretKey(password, salt);

    // GCM recommends 12 bytes iv
    byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
    Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

    byte[] plainBytes = plainMessage.getBytes(UTF_8);
    byte[] encryptedMessageByte = executeCipherOp(cipher, plainBytes, 0, plainBytes.length);

    // prefix IV and Salt to cipher text
    ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + salt.length + encryptedMessageByte.length)
      .put(iv)
      .put(salt)
      .put(encryptedMessageByte);

    return byteBuffer
      .array();
  }


  public static String decrypt(String password, byte[] cipherMessage) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);

    byte[] iv = new byte[IV_LENGTH_BYTE];
    byteBuffer.get(iv);

    byte[] salt = new byte[SALT_LENGTH_BYTE];
    byteBuffer.get(salt);

    SecretKey secretKey = getSecretKey(password, salt);
    Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secretKey, iv);

    byte[] decryptedMessageByte = executeCipherOp(cipher, cipherMessage, byteBuffer.position(), byteBuffer.remaining());
    return new String(decryptedMessageByte, UTF_8);
  }

  private static byte[] executeCipherOp(Cipher cipher, byte[] input, int inputOffset, int inputLen) {
    try {
      return cipher.doFinal(input, inputOffset, inputLen);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new IllegalStateException("Cannot execute cipher op", e);
    }
  }

  private static Cipher initCipher(int mode, SecretKey secretKey, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
      return cipher;
    } catch (NoSuchPaddingException | NoSuchAlgorithmException |
             InvalidKeyException
             | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException("Cannot initialize cipher data", e);
    }
  }
}
