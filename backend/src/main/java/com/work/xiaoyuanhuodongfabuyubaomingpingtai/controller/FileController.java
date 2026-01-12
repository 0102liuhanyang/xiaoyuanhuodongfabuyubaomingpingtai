package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ApiResponse.failure("文件为空");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File("uploads/" + datePath);
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }
        String filename = UUID.randomUUID().toString().replaceAll("-", "");
        if (ext != null) {
            filename += "." + ext;
        }
        File dest = new File(dir, filename);
        file.transferTo(dest);
        String url = "/files/" + datePath + "/" + filename;
        return ApiResponse.success(Map.of("url", url));
    }
}
