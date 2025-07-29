package com.api.agent.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {
    
    /**
     * 将 MultipartFile 转换为 File 并返回文件路径
     * @param multipartFile 上传的文件
     * @return 文件的绝对路径
     * @throws IOException IO异常
     */
    public static String convertMultipartToFile(MultipartFile multipartFile) {
        // 创建临时文件
        File tempFile = null;
        // 将 MultipartFile 内容写入临时文件
        try  {
            File.createTempFile("test/",  multipartFile.getOriginalFilename());
            InputStream inputStream = multipartFile.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.flush();
        }catch (IOException e){
            return null;
        }
        
        // 返回文件的绝对路径
        return tempFile.getAbsolutePath();
    }

}
