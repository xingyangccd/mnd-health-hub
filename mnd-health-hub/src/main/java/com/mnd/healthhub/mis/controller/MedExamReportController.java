package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.common.MinIO;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.controller.form.CreateReportForm;
import com.mnd.healthhub.mis.controller.form.PageQueryReportForm;
import com.mnd.healthhub.mis.controller.form.SaveWaybillForm;
import com.mnd.healthhub.mis.service.MedExamReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/mis/report")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "体检报告管理")
public class MedExamReportController {

    private final MedExamReportService medExamReportService;
    private final MinIO minIO;

    @GetMapping("/list")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "分页查询体检报告", description = "根据患者姓名、手机号、快递单号、状态等条件进行分页查询", operationId = "misExamReport")
    public R pageQuery(@ParameterObject @Valid PageQueryReportForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = pageSize * (pageNo - 1);
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = medExamReportService.queryPage(param);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping
    @Operation(summary = "生成体检报告", description = "根据体检报告ID生成对应的Word文档并上传至MinIO")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:CREATE"}, mode = SaMode.OR)
    public R createReport(@RequestBody @Valid CreateReportForm form) {
        boolean result = medExamReportService.createReport(form.getId());
        return R.ok().put("result", result);
    }



    @GetMapping("/download")
    @Operation(summary = "下载体检报告", description = "根据文件路径下载对应的体检报告Word文档")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:SELECT"}, mode = SaMode.OR)
    public void downloadReport(
            @Parameter(description = "文件路径", required = true, example = "report/checkup/REP20260115001.docx")
            @RequestParam String fileUrl,
            @Parameter(description = "体检人姓名", required = true, example = "张三")
            @RequestParam String name,
            HttpServletResponse response) {

        // 参数校验
        if (StrUtil.isBlank(name)) {
            throw new HisException("姓名不能为空");
        }
        if (!ReUtil.isMatch("^[\\u4e00-\\u9fa5]{2,10}$", name)) {
            throw new HisException("姓名格式不正确，请输入2-10位中文");
        }
        if (StrUtil.isBlank(fileUrl)) {
            throw new HisException("文件路径不能为空");
        }

        log.info("开始下载体检报告，文件路径: {}，姓名: {}", fileUrl, name);

        try (InputStream in = minIO.downloadFile(fileUrl); OutputStream out = response.getOutputStream()) {
            // 将response的残留的contentType重置一下。
            response.reset();
            // 重新设置contentType
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            // 拼接文件名，为了避免乱码，文件名采用UTF-8的方式进行编码。
            String filename = URLEncoder.encode(name + "的体检报告.docx", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

            IOUtil.copyCompletely(in, out);
            out.flush();

            log.info("体检报告下载成功，文件路径: {}", fileUrl);

        } catch (Exception e) {
            log.error("下载体检报告失败，文件路径: {}", fileUrl, e);
            throw new HisException("下载体检报告失败: " + e.getMessage());
        }
    }

    @PostMapping("/waybill")
    @Operation(summary = "识别快递运单", description = "上传快递运单图片，OCR识别收件人姓名、电话、运单号")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:UPDATE"}, mode = SaMode.OR)
    public R recognizeWaybill(
            @Parameter(description = "运单图片文件", required = true)
            @RequestParam("file") MultipartFile file) {

        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new HisException("请上传运单图片");
        }

        // 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new HisException("请上传图片文件");
        }

        log.info("开始识别快递运单，文件名: {}，大小: {} bytes", file.getOriginalFilename(), file.getSize());

        try (InputStream in = file.getInputStream()) {
            // 读取图片并转Base64
            BufferedImage image = ImgUtil.read(in);
            String imageBase64 = "data:image/jpg;base64," + ImgUtil.toBase64(image, "jpg");

            Map<String, String> result = medExamReportService.recognizeWaybill(imageBase64);
            log.info("运单识别成功，运单号: {}", result.get("expressNo"));
            return R.ok().put("result", result);

        } catch (Exception e) {
            log.error("识别运单失败", e);
            throw new HisException("识别运单失败: " + e.getMessage());
        }
    }

    @PutMapping("/waybill/save")
    @Operation(summary = "保存运单信息", description = "保存快递单号和快递日期，报告状态自动变更为'已寄出'")
    @SaCheckPermission(value = {"ROOT", "CHECKUP_REPORT:UPDATE"}, mode = SaMode.OR)
    public R saveWaybill(@RequestBody @Valid SaveWaybillForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("status", 3);
        medExamReportService.saveWaybill(param);
        return R.ok("运单保存成功");
    }
}
