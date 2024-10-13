package com.hanqi.leonsync.controller;

import com.hanqi.leonsync.util.FileUploadUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FileController {

    public static final String FILE_UPLOAD_SOURCE_PATH2 = "/Users/leon/Downloads/";

    @GetMapping(value = "startUpload")
    public String startFileUpload() {
        // FileUploadUtil.doFileUploadToServer(FILE_UPLOAD_SOURCE_PATH2);
        // XHS
        FileUploadUtil.doFileUploadToServer("/data/data/com.termux/files/home/storage/dcim");

        return "ok";
    }

    @PostConstruct
    public void initUploadFile() {
        FileUploadUtil.doFileUploadToServer("/data/data/com.termux/files/home/storage/dcim");
    }

}
