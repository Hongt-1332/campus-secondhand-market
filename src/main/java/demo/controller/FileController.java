package demo.controller;

import demo.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    // 本地存储根目录
    private static final String UPLOAD_PATH = "C:/upload/";
    // 前端访问图片的URL前缀
    private static final String IMG_PREFIX = "http://localhost:8080/api/";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 按日期分文件夹存储
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateDir = sdf.format(new Date());
            File dir = new File(UPLOAD_PATH + dateDir);
            if (!dir.exists()) dir.mkdirs();

            // 生成唯一文件名，防止重名覆盖
            String originalName = file.getOriginalFilename();
            String suffix = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + suffix;

            // 保存文件到本地
            File targetFile = new File(dir, fileName);
            file.transferTo(targetFile);

            // 返回可访问的图片完整地址，存入数据库
            String imgUrl = IMG_PREFIX + dateDir + "/" + fileName;
            return Result.success(imgUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("图片上传失败");
        }
    }
}