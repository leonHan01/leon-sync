package com.hanqi.leonsync.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class FileUploadUtil {
    public static final String NAS_SERVER_IP = "192.168.3.35";
    // 创建一个固定大小的线程池
    static ExecutorService executor = Executors.newFixedThreadPool(1);

    static AtomicInteger atomicInteger = new AtomicInteger(0);
    public static int totalCnt = 0;
    public static void doFileUploadToServer(String path) {
        log.info("开始文件上传");
        File inputDir = new File(path);
        File[] inputFiles = inputDir.listFiles();
        for (File file : inputFiles) {
            if (!file.isDirectory()) {
                totalCnt++;
            }
        }

        for (File file : inputFiles) {
            if (!file.isDirectory()) {
                if (file.getName().endsWith("jpg") || file.getName().endsWith("webp") || file.getName().endsWith("jpeg")) {
                    executor.submit(new Task(file.getAbsolutePath()));
                }
            }
        }
    }

    static class Task implements Runnable {
        private String path;

        public Task(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            Boolean success = uploadFile("http://" + NAS_SERVER_IP + ":8080/upload", path);
            System.out.println("Task " + path + " upload success: " + success + ", count:" + atomicInteger.addAndGet(1) + ", total:" + totalCnt);
            // 成功之后，移动文件
            //moveFile(file.getAbsolutePath(), HanConstant.FILE_UPLOAD_SOURCE_MOVE_PATH + file.getName(), file.getName());
            if (success) {
                deleteFile(path);
            }
        }
    }

    private static Boolean uploadFile(String targetUrl, String filePath) {
        RestTemplate restTemplate = new RestTemplate();

        Resource fileResource = new FileSystemResource(new File(filePath));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);
            log.info("Response: " + response.getBody());
            return response.getStatusCode().equals(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            log.error("uploadFile fail:", e);
            return false;
        }
    }

    private static void deleteFile(String source) {
        try {
            Path sourcePath = Paths.get(source);
            // 文件移动，存在则覆盖
            Files.delete(sourcePath);
            log.info("File moved successfully. ");
        } catch (Exception e) {
            log.error("moveFile fail", e);
        }
    }
}
