package com.sky.service.impl;

import com.sky.service.MinIoService;
import com.sky.utils.MinIoUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MinIoServiceImpl implements MinIoService {
    
    @Autowired
    private MinIoUtil minIoUtil;

    public Boolean bucketExists(String bucketName) {
        return minIoUtil.bucketExists(bucketName);
    }

    public void makeBucket(String bucketName) {
        minIoUtil.makeBucket(bucketName);
    }

    @SneakyThrows
    public List<Object> listObjects(String bucketName) {
        return minIoUtil.listObjects(bucketName);
    }


    public Boolean upload(MultipartFile multipartFile) {
        return minIoUtil.putObject(multipartFile);
    }


    public void upload(MultipartFile[] multipartFile) {
        minIoUtil.putObject(multipartFile);
    }


    public Boolean delFile(String bucketName,String fileName) {
        return minIoUtil.removeObject(bucketName, fileName);
    }


    public String getFileUrl(String fileName) {
        return minIoUtil.getObjectUrl(fileName);
    }
}
