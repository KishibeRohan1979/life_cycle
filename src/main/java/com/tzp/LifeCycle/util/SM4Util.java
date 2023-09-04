package com.tzp.LifeCycle.util;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

/**
 * @author KangXvdong
 */
public class SM4Util {

    static final String ALGORITHM_NAME = "SM4";

    static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";

    static final String EPIDEMIC_KEY = "10aaf71cea12d5ea8590a995e0880de3";


    public static String encryption(String str, String key) throws Exception {
        Cipher cipher = encryptionCipher(key);
        // String -> byte[]
        byte[] srcData = str.getBytes(StandardCharsets.UTF_8);
        byte[] cipherArray = cipher.doFinal(srcData);
        // byte[] -> hexString
        return ByteUtils.toHexString(cipherArray);
    }

    public static String decrypt(String encryptedText, String key) throws Exception {
        Cipher cipher = decryptCipher(key);

        byte[] cipherData = ByteUtils.fromHexString(encryptedText);
        byte[] decryptedData = cipher.doFinal(cipherData);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * 获取加密对象
     * 如果给自定义 key 就用自定义 key，如果给空就用这个类默认的密钥
     *
     * @param key 密钥
     * @return 返回加密对象
     * @throws Exception 异常
     */
    public static Cipher encryptionCipher(String key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // 16进制字符串 -> byte[]
        SecretKeySpec sm4Key;
        if (StringUtils.isNotBlank(key))  {
            sm4Key = new SecretKeySpec(ByteUtils.fromHexString(key), ALGORITHM_NAME);
        } else {
            sm4Key = new SecretKeySpec(ByteUtils.fromHexString(EPIDEMIC_KEY), ALGORITHM_NAME);
        }
        // 加密后的数组
        Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, sm4Key);
        return cipher;
    }

    /**
     * 获取解密对象
     * 如果给自定义 key 就用自定义 key，如果给空就用这个类默认的密钥
     *
     * @param key 密钥
     * @return 返回解密对象
     * @throws Exception 异常
     */
    public static Cipher decryptCipher(String key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        SecretKeySpec sm4Key;
        if (StringUtils.isNotBlank(key))  {
            sm4Key = new SecretKeySpec(ByteUtils.fromHexString(key), ALGORITHM_NAME);
        } else {
            sm4Key = new SecretKeySpec(ByteUtils.fromHexString(EPIDEMIC_KEY), ALGORITHM_NAME);
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, sm4Key);
        return cipher;
    }

    /**
     * 本地加密文件
     *
     * @param inputFile 原文件地址
     * @param outputPath 加密文件地址（可以为空）
     * @param key 密钥（可以为空）
     * @throws Exception 异常
     */
    public static void encryptFile(String inputFile, String outputPath, String key) throws Exception {
        // 利用提供的文件地址得到一个 Path 类，其实和 File 类似
        Path inputFilePath = Paths.get(inputFile);
        // 创建存放加密文件的地址
        Path encryptedFilePath;
        SnowFlakeUtil snowFlakeUtil = new SnowFlakeUtil(2, 1);
        String fileId = snowFlakeUtil.nextIdByString() + "-";
        if (StringUtils.isNotBlank(outputPath)) {
            encryptedFilePath = Paths.get(outputPath).resolve(fileId + inputFilePath.getFileName());
        } else {
            encryptedFilePath = inputFilePath.getParent().resolve(fileId + inputFilePath.getFileName());
        }

        encryptFileWrite(Files.newInputStream(inputFilePath), Files.newOutputStream(encryptedFilePath), key);
    }

    /**
     * 本地文件解密
     *
     * @param encryptedFile 加密文件地址
     * @param outputPath 解密文件地址（可以为空）
     * @param key 密钥（可以为空）
     * @throws Exception 异常
     */
    public static void decryptFile(String encryptedFile, String outputPath, String key) throws Exception {
        Path encryptedFilePath = Paths.get(encryptedFile);
        Path decryptedFilePath;

        String encryptedFileName = encryptedFilePath.getFileName().toString();
        // 将加密文件文件名前面的雪花id删掉
        int index = encryptedFileName.indexOf("-");
        String fileName;
        if ( index == -1 ) {
            fileName = encryptedFileName;
        } else {
            fileName = encryptedFileName.substring( index+1 );
        }
        if (StringUtils.isNotBlank(outputPath)) {
            decryptedFilePath = Paths.get(outputPath).resolve(fileName);
        } else {
            decryptedFilePath = encryptedFilePath.getParent().resolve(fileName);
        }
        decryptFileWrite(Files.newInputStream(encryptedFilePath), Files.newOutputStream(decryptedFilePath), key);
    }

    /**
     * 具体实现文件读写的操作
     * 本地加密文件（仅仅是为了把这块抽出来）
     *
     * @param inputFilePath 原文件地址
     * @param encryptedFilePath 加密文件地址
     * @param key 密钥
     * @throws Exception 异常
     */
    public static void encryptFileWrite(InputStream inputFilePath, OutputStream encryptedFilePath, String key) throws Exception {
        Cipher cipher = encryptionCipher(key);

        try (InputStream inputStream = inputFilePath;
             OutputStream outputStream = encryptedFilePath;
             CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 具体实现文件读写的操作
     * 本地解密文件（仅仅是为了把这块抽出来）
     *
     * @param encryptedFilePath 加密文件地址
     * @param decryptedFilePath 解密文件地址
     * @param key 密钥
     * @throws Exception 异常
     */
    public static void decryptFileWrite(InputStream encryptedFilePath, OutputStream decryptedFilePath, String key) throws Exception {
        Cipher cipher = decryptCipher(key);

        try (InputStream inputStream = encryptedFilePath;
             OutputStream outputStream = decryptedFilePath;
             CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 上传文件并加密
     *
     * @param inputFile 原文件
     * @param outputPath 输出地址
     * @param key 密钥（可以为空）
     * @throws Exception 异常
     */
    public static String encryptFile(MultipartFile inputFile, String outputPath, String key) throws Exception {
        SnowFlakeUtil snowFlakeUtil = new SnowFlakeUtil(2, 1);
        String fileId = snowFlakeUtil.nextIdByString() + "-";
        // 创建存放加密文件的地址
        Path encryptedFilePath = Paths.get(outputPath).resolve(fileId + inputFile.getOriginalFilename());
        encryptFileWrite(inputFile.getInputStream(), Files.newOutputStream(encryptedFilePath), key);
        return fileId + inputFile.getOriginalFilename();
    }

    /**
     * 上传文件并解密
     *
     * @param encryptedFile 加密文件地址
     * @param outputPath 输出地址
     * @param key 密钥（可以为空）
     * @throws Exception 异常
     */
    public static String decryptFile(MultipartFile encryptedFile, String outputPath, String key) throws Exception {
        SnowFlakeUtil snowFlakeUtil = new SnowFlakeUtil(2, 1);
        String fileId = snowFlakeUtil.nextIdByString() + "-";
        // 创建存放解密文件的地址
        Path decryptedFilePath = Paths.get(outputPath).resolve(fileId + encryptedFile.getOriginalFilename());;
        decryptFileWrite(encryptedFile.getInputStream(), Files.newOutputStream(decryptedFilePath), key);
        return fileId + encryptedFile.getOriginalFilename();
    }

}
