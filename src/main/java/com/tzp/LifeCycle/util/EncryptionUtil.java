package com.tzp.LifeCycle.util;

import org.apache.commons.lang3.StringUtils;
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
import java.util.Base64;

/**
 * 加解密工具
 *
 * @author kangxvdong
 */
public class EncryptionUtil {

    /**
     * 16/24/32字节的密钥，AES-128/AES-192/AES-256
     */
    public static final String SECRET_KEY = "D2NnM5+aD7RUhNRWJL8LwvDm";

    /**
     * 加密数据
     *
     * @param data 原数据
     * @return 加密文本
     * @throws Exception 异常
     */
    public static String encryption(String data) throws Exception {
        return encryption(data, SECRET_KEY);
    }

    /**
     * 解密文本
     *
     * @param encryptedText 原数据
     * @return 原始文本
     * @throws Exception 异常
     */
    public static String decrypt(String encryptedText) throws Exception {
        return decrypt(encryptedText, SECRET_KEY);
    }

    /**
     * 创建加密的 Cipher 对象
     */
    public static Cipher encryptionCipher(String key) throws Exception {
        // 设置AES算法和加密模式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // 创建一个AES密钥
        SecretKeySpec secretKeySpec;
        if (StringUtils.isNotBlank(key)) {
            secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        } else {
            secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        }

        // 初始化加密模式
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        return cipher;
    }

    /**
     * 创建解密的 Cipher 对象
     */
    public static Cipher decryptCipher(String key) throws Exception {
        // 设置AES算法和解密模式
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        // 创建一个AES密钥
        SecretKeySpec secretKeySpec;
        if (StringUtils.isNotBlank(key)) {
            secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        } else {
            secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        }

        // 初始化解密模式
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        return cipher;
    }

    /**
     * 使用自定义的密钥加密
     *
     * @param data 需要加密的数据
     * @param key 密钥
     * @return 返回加密的字符
     * @throws Exception 异常
     */
    public static String encryption(String data, String key) throws Exception {
        // 设置AES算法和加密模式
        Cipher cipher = encryptionCipher(key);

        // 执行加密
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // 使用Base64编码将加密后的字节数组转换为字符串
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 解密
     *
     * @param encryptedText 加密的文本
     * @param key 密钥
     * @return 原始文本
     * @throws Exception 异常
     */
    public static String decrypt(String encryptedText, String key) throws Exception {
        // 设置AES算法和解密模式
        Cipher cipher = decryptCipher(key);

        // 将Base64编码的密文解码为字节数组
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

        // 执行解密
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 将解密后的字节数组转换为字符串
        return new String(decryptedBytes, StandardCharsets.UTF_8);
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
        Path decryptedFilePath = Paths.get(outputPath).resolve(fileId + encryptedFile.getOriginalFilename());
        decryptFileWrite(encryptedFile.getInputStream(), Files.newOutputStream(decryptedFilePath), key);
        return fileId + encryptedFile.getOriginalFilename();
    }



}
