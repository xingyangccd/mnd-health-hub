package com.mnd.healthhub.common;

import com.mnd.healthhub.exception.HisException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Component // 纳入IoC容器的管理，它就是一个Bean
@Slf4j
public class MinIO {
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    // minio的核心对象：客户端对象（专门为java项目提供的，它可以和minio服务器交互）
    private MinioClient client;

    // 在Bean的初始化阶段给client赋值，初始化minio客户端对象
    @PostConstruct
    public void init() {
        this.client = new MinioClient.Builder()
                .endpoint(endpoint) // IP地址和端口
                .credentials(accessKey, secretKey) // 指定用户名和密码
                .build();
    }

    /**
     * 上传base64编码的图片到minio服务器。
     *
     * @param path        存储路径
     * @param base64Image base64编码的图片（字符串）。
     */
    public void uploadImage(String path, String base64Image) {

        try {
            // 1. 处理 base64编码的图片字符串。
            // 如果 base64Image 这个字符串以 data:image/png;base64, 或者 data:image/jpeg;base64,  都需要去掉前缀。
            // 浏览器上传输过来的 DataURL 是以 data:image/png;base64, 或者 data:image/jpeg;base64, 作为前缀的。
            // 以他们为前缀才是一个合法的DataURL。因此我们保存图片的时候，图片本身是不需要这个前缀的，以下这个操作的目的就是去掉前缀。
            base64Image = base64Image.replace("data:image/png;base64,", "");
            base64Image = base64Image.replace("data:image/jpeg;base64,", "");
            // 防止base64图片字符串中包含空白以及换行符。保险起见，去掉空白和换行符。
            base64Image = base64Image.replaceAll("\\s+", "");

            // 2. 将base64编码的字符串转换成byte[]数组，因为后面我们需要输入流
            byte[] decode = Base64.getDecoder().decode(base64Image);

            // 3. 封装字节输入流，做文件上传
            try (ByteArrayInputStream in = new ByteArrayInputStream(decode)) {
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .stream(in, decode.length, 5 * 1024 * 1024)
                        .contentType("image/jpeg")
                        .build());
            }

            log.info("向 {} 保存了文件，大小：{}KB", path, decode.length / 1024);

        } catch (Exception e) {
            log.error("保存文件失败，路径：{}", path, e);
            throw new HisException("保存文件失败" + e.getMessage());
        }

    }

    /**
     * 上传图片到minio服务器
     *
     * @param path 图片在minio服务器上存储的相对路径（程序员自己指定的路径：front/goods/a9f496fb31e446b09957ec8f04e374dd.jpg）。
     * @param file 图片本身
     */
    public void uploadImage(String path, MultipartFile file) {
        // 参数校验
        if (path == null || path.trim().isEmpty()) {
            throw new HisException("文件路径不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new HisException("上传文件不能为空");
        }

        // 实现文件上传
        try (InputStream in = file.getInputStream()) { // 从字节输入流当中读取这个图片。
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket) // 传到哪个存储桶当中
                    .object(path) // 存储到minio服务器的哪个目录中（文件的全路径）
                    // 指定文件内容（输入流）、文件大小、分片大小（如果文件特别大，要分片上传，每片最大5MB）
                    .stream(in, file.getSize(), 5 * 1024 * 1024)
                    // 告诉浏览器如何解析和渲染该文件。（文件上传成功之后，用户通过URL访问minio服务器上这个图片时：响应的内容类型）
                    .contentType("image/jpeg")
                    .build());
            log.info("文件上传成功：{}", path);
        } catch (Exception e) {
            log.error("文件上传失败：{}", path, e);
            throw new HisException("文件上传失败");
        }
    }


    /**
     * 将excel文件上传到minio服务器。
     *
     * @param path excel文件的相对路径。
     * @param file excel文件本身。
     */
    public void uploadExcel(String path, MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            String filename = file.getOriginalFilename();
            String mime = filename != null && filename.endsWith(".xlsx")
                    ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    : "application/vnd.ms-excel";

            // 调用minio客户端的方法完成excel上传
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(in, file.getSize(), 20 * 1024 * 1024)
                    .contentType(mime) // 设置将来用户从浏览器上下载这个文件时的响应内容类型
                    .build());
            log.info("Excel文件上传成功：path={},size={}KB", path, file.getSize() / 1024);
        } catch (Exception e) {
            log.error("Excel文件上传失败：path={}", path, e);
            throw new HisException("Excel文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 从minio服务器上下载指定的文件
     *
     * @param path 文件路径
     * @return 文件输入流（通过这个流我们就可以读取这个minio上的文件了）
     */
    public InputStream downloadFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new HisException("下载失败：文件路径不能为空");
        }
        try {
            GetObjectArgs getObjArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path.trim())
                    .build();
            return client.getObject(getObjArgs);
        } catch (Exception e) {
            log.error("文件下载失败，路径：{}", path, e);
            throw new HisException("文件下载失败：" + e.getMessage());
        }
    }

    /**
     * 删除minio服务器上的文件。
     *
     * @param path 文件的路径
     */
    public void deleteFile(String path) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
            log.info("文件删除成功：{}", path);
        } catch (Exception e) {
            log.error("文件删除失败，文件路径：{}", path, e);
            log.info("删除失败的文件 {} 已存储到数据库，等待定时任务删除", path);
            throw new HisException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 上传word文档到minio服务器
     *
     * @param path 存储路径（包含文件名）
     * @param in   文件输入流
     */
    public void uploadWord(String path, InputStream in) {
        long begin = System.currentTimeMillis();
        log.info("开始上传word文档，路径：{}", path);
        // MIME类型：标识.docx文件格式
        // 作用：设置HTTP响应头Content-Type，告知浏览器如何解析文件
        String mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        try (in) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(in, in.available(), 20 * 1024 * 1024)
                    .contentType(mime) // 浏览器在下载minio服务器上的word文件时采用的响应的内容类型
                    .build());
            long end = System.currentTimeMillis();
            log.info("word文档上传成功，路径：{}，耗时{}毫秒", path, (end - begin));
        } catch (Exception e) {
            log.error("上传word文档失败，路径：{}", path, e);
            throw new HisException("上传word文档失败：" + e.getMessage());
        }
    }

}



















