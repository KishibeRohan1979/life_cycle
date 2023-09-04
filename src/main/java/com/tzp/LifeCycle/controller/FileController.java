package com.tzp.LifeCycle.controller;

import com.tzp.LifeCycle.util.AESUtil;
import com.tzp.LifeCycle.util.MsgUtil;
import com.tzp.LifeCycle.util.SM4Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kangxvdong
 */
@RestController
@RequestMapping("/file")
@Api(value = "FileController", tags = "加解密文件")
public class FileController {

    @ApiOperation("自定义密钥加密文件")
    @PostMapping("/uploadFileAndEncrypt")
    public MsgUtil<Object> uploadFileAndEncrypt(@RequestParam("files") MultipartFile[] files,
                                                @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        List<String> resultList = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            try {
                // 获取当前运行的Jar包所在路径（当前工作目录）
                String fileDirPath = System.getProperty("user.dir");
                // 相同目录下创建一个 file 的文件夹，再在 file 文件夹下创建加密文件夹（encryptFiles）和解密文件夹（decryptFiles）
                File encryptFiles = new File(fileDirPath + "/file/encryptFiles");
                if (!encryptFiles.exists()) {
                    boolean b = encryptFiles.mkdirs();
                }
                // 加密操作
                String fileName = AESUtil.encryptFile(multipartFile, encryptFiles.getPath(), key);
                String filePath = encryptFiles.getPath() + "/" + fileName;
                // Windows 特有的路径是两个斜杠
                resultList.add(filePath.replaceAll("\\\\", "/"));
            } catch (Exception e) {
                e.printStackTrace();
                return MsgUtil.fail();
            }
        }
        return MsgUtil.success("加密成功", resultList);
    }

    @ApiOperation("自定义密钥解密文件")
    @PostMapping("/uploadFileAndDecryptByUserKey")
    public MsgUtil<Object> uploadFileAndDecryptByUserKey(@RequestParam("files") MultipartFile[] files,
                                                         @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        List<String> resultList = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            try {
                // 获取当前运行的Jar包所在路径（当前工作目录）
                String fileDirPath = System.getProperty("user.dir");
                // 相同目录下创建一个 file 的文件夹，再在 file 文件夹下创建加密文件夹（encryptFiles）和解密文件夹（decryptFiles）
                File decryptFiles = new File(fileDirPath + "/file/decryptFiles");
                if (!decryptFiles.exists()) {
                    boolean b = decryptFiles.mkdirs();
                }

                // 解密操作
                String fileName = AESUtil.decryptFile(multipartFile, decryptFiles.getPath(), key);
                String filePath = decryptFiles.getPath() + "/" + fileName;
                // Windows 特有的路径是两个斜杠
                resultList.add(filePath.replaceAll("\\\\", "/"));
            } catch (Exception e) {
                e.printStackTrace();
                return MsgUtil.fail("解密失败", e);
            }
        }
        return MsgUtil.success("解密成功", resultList);
    }

    @ApiOperation("自定义密钥加密字符串")
    @PostMapping("/encryptStringByUserKey")
    public MsgUtil<Object> encryptStringByUserKey(@RequestParam("strData") String strData,
                                                  @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        try {
            String encryptString = AESUtil.encryption(strData, key);
            return MsgUtil.success("加密成功", encryptString);
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail();
        }
    }

    @ApiOperation("自定义密钥解密字符串")
    @PostMapping("/decryptStringByUserKey")
    public MsgUtil<Object> decryptStringByUserKey(@RequestParam("strData") String strData,
                                                  @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        try {
            String encryptString = AESUtil.decrypt(strData, key);
            return MsgUtil.success("解密成功", encryptString);
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail("解密失败", "密钥错误");
        }
    }

    @ApiOperation("下载文件")
    @GetMapping("/download")
    public void downloadFile(@RequestParam("filePath") String filePath,
                             HttpServletResponse response) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        // 配置文件下载
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        // 下载文件能正常显示中文
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            // 实现文件下载
            byte[] buffer = new byte[1024];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private MsgUtil<String> checkKey(String key) {
        if (StringUtils.isNotBlank(key)) {
            // AES密钥必须满足16或24或者32位长度
            if (key.length() != 16 && key.length() != 24 && key.length() != 32) {
                return MsgUtil.fail("校验失败", "AES密钥必须满足16或24或者32位长度");
            }
        }
        return MsgUtil.success("校验通过");
    }

    @ApiOperation("自定义密钥加密文件SM4")
    @PostMapping("/uploadFileAndEncryptBySM4")
    public MsgUtil<Object> uploadFileAndEncryptBySm4(@RequestParam("files") MultipartFile[] files,
                                                     @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        List<String> resultList = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            try {
                // 获取当前运行的Jar包所在路径（当前工作目录）
                String fileDirPath = System.getProperty("user.dir");
                // 相同目录下创建一个 file 的文件夹，再在 file 文件夹下创建加密文件夹（encryptFiles）和解密文件夹（decryptFiles）
                File encryptFiles = new File(fileDirPath + "/file/encryptFiles");
                if (!encryptFiles.exists()) {
                    boolean b = encryptFiles.mkdirs();
                }
                // 加密操作
                String fileName = SM4Util.encryptFile(multipartFile, encryptFiles.getPath(), key);
                String filePath = encryptFiles.getPath() + "/" + fileName;
                // Windows 特有的路径是两个斜杠
                resultList.add(filePath.replaceAll("\\\\", "/"));
            } catch (Exception e) {
                e.printStackTrace();
                return MsgUtil.fail();
            }
        }
        return MsgUtil.success("加密成功", resultList);
    }

    @ApiOperation("自定义密钥解密文件SM4")
    @PostMapping("/uploadFileAndDecryptByUserKeyBySM4")
    public MsgUtil<Object> uploadFileAndDecryptByUserKeyBySM4(@RequestParam("files") MultipartFile[] files,
                                                              @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        List<String> resultList = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            try {
                // 获取当前运行的Jar包所在路径（当前工作目录）
                String fileDirPath = System.getProperty("user.dir");
                // 相同目录下创建一个 file 的文件夹，再在 file 文件夹下创建加密文件夹（encryptFiles）和解密文件夹（decryptFiles）
                File decryptFiles = new File(fileDirPath + "/file/decryptFiles");
                if (!decryptFiles.exists()) {
                    boolean b = decryptFiles.mkdirs();
                }

                // 解密操作
                String fileName = SM4Util.decryptFile(multipartFile, decryptFiles.getPath(), key);
                String filePath = decryptFiles.getPath() + "/" + fileName;
                // Windows 特有的路径是两个斜杠
                resultList.add(filePath.replaceAll("\\\\", "/"));
            } catch (Exception e) {
                e.printStackTrace();
                return MsgUtil.fail("解密失败", e);
            }
        }
        return MsgUtil.success("解密成功", resultList);
    }

    @ApiOperation("自定义密钥加密字符串SM4")
    @PostMapping("/encryptStringByUserKeySM4")
    public MsgUtil<Object> encryptStringByUserKeySM4(@RequestParam("strData") String strData,
                                                     @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        try {
            String encryptString = SM4Util.encryption(strData, key);
            return MsgUtil.success("加密成功", encryptString);
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail();
        }
    }

    @ApiOperation("自定义密钥解密字符串SM4")
    @PostMapping("/decryptStringByUserKeySM4")
    public MsgUtil<Object> decryptStringByUserKeySM4(@RequestParam("strData") String strData,
                                                     @RequestParam(name = "key", required = false) String key) {
        MsgUtil<String> checkMsg = checkKey(key);
        if (!checkMsg.getFlag()) {
            return MsgUtil.fail("请求失败", checkMsg.getData());
        }
        try {
            String encryptString = SM4Util.decrypt(strData, key);
            return MsgUtil.success("解密成功", encryptString);
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail("解密失败", "密钥错误");
        }
    }

}
