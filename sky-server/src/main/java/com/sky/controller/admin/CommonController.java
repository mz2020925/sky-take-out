package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.MinIoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private MinIoService minIoService;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file);
        try{
            // 原始文件名
            String fileName = file.getOriginalFilename();
            assert fileName != null;

            // 根据业务设计，设置存储路径：按天创建目录
            String objectName = new SimpleDateFormat("yyyy-MM-dd+").format(new Date())
                    + UUID.randomUUID().toString()
                    + fileName.substring(fileName.lastIndexOf("."));  // 截取原始文件名的后缀,构造新文件名称，新文件名称保证文件名唯一

            minIoService.upload(file, objectName);
            log.info("文件格式为：{}", file.getContentType());
            log.info("文件原名称为：{}", fileName);
            log.info("文件对象路径为：{}", objectName);
            return Result.success(minIoService.getFileUrl(fileName));
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }



    }

}
