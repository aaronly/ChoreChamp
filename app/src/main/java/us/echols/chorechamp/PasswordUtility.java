package us.echols.chorechamp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import us.echols.chorechamp.dialogs.ConfirmPasswordDialog;

public class PasswordUtility {

    private static KeyStore keyStore;
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String KEY_ALGORITHM_RSA = "RSA";
    private static final String RSA_MODE_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String RSA_MODE_PROVIDER = "AndroidOpenSSL";
    private static final String RSA_MODE_PROVIDER_M = "AndroidKeyStoreBCWorkaround";
    private static final String AES_MODE_TRANSFORMATION = "AES/ECB/PKCS7Padding";
    private static final String AES_MODE_PROVIDER = "BC";
    private static final String TAG = "Encryption";

    private static final String ENCRYPTED_KEY = "us.echols.chorechamp.KEY.encrypted_key";
    private static final String KEY_ALIAS = "us.echols.chorechamp.KEY.parental_password";
    private static final String SHARED_PREFERENCE_NAME = "us.echols.chorechamp.PREF.parental_password";

    private static final String PASSWORD_FILE_NAME = "data";

    @SuppressLint("StaticFieldLeak")
    private static PasswordUtility instance;
    private final Context context;

    private PasswordUtility(Context context) {
        this.context = context;
        generateRsaKeys();
        generateAesKey();
    }

    public static synchronized PasswordUtility getInstance(Context context) {
        if(instance == null) {
            instance = new PasswordUtility(context.getApplicationContext());
        }
        return instance;
    }

    private void generateRsaKeys() {
        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);

            // Generate the RSA key pairs
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                // Generate a key pair for encryption
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);

                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, AndroidKeyStore);

                @SuppressWarnings("deprecation")
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                kpg.initialize(spec);
                kpg.generateKeyPair();
            }
        } catch (KeyStoreException |
                IOException |
                NoSuchAlgorithmException |
                CertificateException |
                NoSuchProviderException |
                InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Error generating RSA keys");
            e.printStackTrace();
        }
    }

    private Cipher getRsaCipher(int cipher_mode) {
        Cipher cipher = null;
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(KEY_ALIAS, null);
            String provider;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // below android m
                provider = RSA_MODE_PROVIDER; // error in android 6: InvalidKeyException: Need RSA private or public key
            }
            else { // android m and above
                provider = RSA_MODE_PROVIDER_M; // error in android 5: NoSuchProviderException: Provider not available: AndroidKeyStoreBCWorkaround
            }
            cipher = Cipher.getInstance(RSA_MODE_TRANSFORMATION, provider);
            if (cipher_mode == Cipher.ENCRYPT_MODE) {
                cipher.init(cipher_mode, privateKeyEntry.getCertificate().getPublicKey());
            } else if (cipher_mode == Cipher.DECRYPT_MODE) {
                cipher.init(cipher_mode, privateKeyEntry.getPrivateKey());
            }
        } catch (NoSuchAlgorithmException |
                InvalidKeyException |
                NoSuchPaddingException |
                NoSuchProviderException |
                KeyStoreException |
                UnrecoverableEntryException e) {
            Log.e(TAG, "Error with RSA encryption/decryption");
            e.printStackTrace();
        }

        return cipher;
    }

    private byte[] rsaEncrypt(byte[] secret) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Cipher cipher = getRsaCipher(Cipher.ENCRYPT_MODE);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            cipherOutputStream.write(secret);
            cipherOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error with RSA encryption/decryption");
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    private byte[] rsaDecrypt(byte[] encrypted) {
        List<Byte> values = new ArrayList<>();

        try {
            Cipher cipher = getRsaCipher(Cipher.DECRYPT_MODE);
            CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encrypted), cipher);

            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error with RSA encryption/decryption");
            e.printStackTrace();
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }
        return bytes;
    }

    private void generateAesKey() {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
        if (encryptedKeyB64 == null) {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            byte[] encryptedKey = rsaEncrypt(key);
            encryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(ENCRYPTED_KEY, encryptedKeyB64);
            edit.apply();
        }
    }

    private Key getSecretKey() {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null);
        byte[] encryptedKey = Base64.decode(encryptedKeyB64, Base64.DEFAULT);
        byte[] key = rsaDecrypt(encryptedKey);
        return new SecretKeySpec(key, "AES");
    }

    private Cipher getAesCipher(int cipher_mode) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(AES_MODE_TRANSFORMATION, AES_MODE_PROVIDER);
            cipher.init(cipher_mode, getSecretKey());
        } catch (Exception e) {
            Log.e(TAG, "Error with AES encryption/decryption");
            e.printStackTrace();
        }
        return cipher;
    }

    private String encrypt(byte[] secret) {
        Cipher cipher = getAesCipher(Cipher.ENCRYPT_MODE);
        byte[] encodedBytes = new byte[0];
        try {
            encodedBytes = cipher.doFinal(secret);
            //noinspection UnusedAssignment
            secret = null;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Log.e(TAG, "Error with AES encryption/decryption");
            e.printStackTrace();
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    @SuppressWarnings("unused")
    public byte[] decrypt(String encryptedString) {
        byte[] encrypted = Base64.decode(encryptedString, Base64.DEFAULT);

        Cipher cipher = getAesCipher(Cipher.DECRYPT_MODE);
        byte[] decodedBytes = null;
        try {
            decodedBytes = cipher.doFinal(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Log.e(TAG, "Error with AES encryption/decryption");
            e.printStackTrace();
        }

        return decodedBytes;
    }

    public boolean checkForPasswordFile() {
        File folder = context.getFilesDir();
        File passwordFile = new File(folder, PASSWORD_FILE_NAME);
        return passwordFile.exists();
    }

    public void setPassword(byte[] password) {
        String encryptedPassword = encrypt(password);
        File folder = context.getFilesDir();
        File passwordFile = new File(folder, PASSWORD_FILE_NAME);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFile))){
            writer.write(encryptedPassword);
        } catch (IOException e) {
            Log.e("WriteFileError", e.getMessage());
        }
    }

    private String getEncryptedPassword() {
        String encryptedPassword = null;

        File folder = context.getFilesDir();
        File passwordFile = new File(folder, PASSWORD_FILE_NAME);

        try (BufferedReader reader = new BufferedReader(new FileReader(passwordFile))) {
            encryptedPassword = reader.readLine() + "\n";
        } catch (IOException e) {
            Log.e("RetrievePasswordError", e.getMessage());
        }
        return encryptedPassword;
    }

    public boolean verifyPassword(byte[] password) {
        boolean result = false;

        String enteredPassword = encrypt(password);
        //noinspection UnusedAssignment
        password = null;
        String encryptedPassword = getEncryptedPassword();

        if(enteredPassword.equals(encryptedPassword)) {
            result = true;
        }

        return result;
    }

    public static void confirmPassword(FragmentManager fragmentManager, String tag, ConfirmPasswordDialog.ConfirmPasswordDialogListener listener) {
        ConfirmPasswordDialog confirmPasswordDialog = ConfirmPasswordDialog.newInstance();
        confirmPasswordDialog.show(fragmentManager, tag);
        confirmPasswordDialog.setConfirmPasswordDialogListener(listener);
    }



}
