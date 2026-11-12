package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.common.MinIO;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.db.entity.MedExamPackageEntity;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.controller.form.ExamPackagePageQueryForm;
import com.mnd.healthhub.mis.controller.form.RemoveExamPackageByIdsForm;
import com.mnd.healthhub.mis.controller.form.SaveMedExamPackageForm;
import com.mnd.healthhub.mis.controller.form.UpdateMedExamPackageForm;
import com.mnd.healthhub.mis.service.MedExamPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.Map;

@RestController("misGoodsController")
@RequiredArgsConstructor
@RequestMapping("/mis/goods")
@Slf4j
@Tag(name = "套餐管理")
public class MedExamPackageController {
    private final MedExamPackageService medExamPackageService;
    private final MinIO minIO;

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "根据查询条件分页查询套餐记录")
    @SaCheckPermission(value = {"ROOT", "GOODS:SELECT"}, mode = SaMode.OR)
    public R list(@ParameterObject @Valid ExamPackagePageQueryForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = medExamPackageService.queryPage(params);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping("/images")
    @SaCheckPermission(value = {"ROOT", "GOODS:INSERT", "GOODS:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "上传体检套餐封面图片", description = "上传图片到MinIO服务器，返回图片在MinIO服务器上的相对路径")
    public R uploadImg(@RequestParam("file") MultipartFile file) {
        // 文件不能为空
        if(file == null || file.isEmpty()) {
            return R.error("请选择要上传的图片文件");
        }
        // 文件大小不能超过5MB。
        if(file.getSize() > 5 * 1024 * 1024) {
            return R.error("图片大小不能超过5MB");
        }
        // 文件类型必须是图片【jpg/jpeg/png】
        String contentType = file.getContentType();
        if(contentType == null || !(contentType.equals("image/jpeg")
                || contentType.equals("image/jpg") || contentType.equals("image/png"))) {
            return R.error("仅支持jpg/jpeg/png格式的图片");
        }
        // 文件不为空，文件大小也是小于5MB，文件内容类型也是一个图片
        String path = medExamPackageService.uploadImg(file);
        return R.ok().put("result", path);
    }

    @PostMapping
    @SaCheckPermission(value = {"ROOT", "GOODS:INSERT"}, mode = SaMode.OR)
    @Operation(summary = "新增体检套餐", description = "保存体检套餐信息，包含体检项目列表和特征标签")
    public R save(@RequestBody @Valid SaveMedExamPackageForm form) {
        // 将form转换成实体（由于form中属性的类型与entity对象中属性的类型不一致，因此这里先忽略掉这些属性。）
        MedExamPackageEntity goods = BeanUtil.toBean(form, MedExamPackageEntity.class,
                CopyOptions.create().setIgnoreProperties("departmentExam","labExam","medicalExam","otherExam","tags"));

        // 将List集合转换成JSON格式的字符串。
        if(CollUtil.isNotEmpty(form.getDepartmentExam())){
            // 存在科室检查
            goods.setDepartmentExam(JSONUtil.toJsonStr(form.getDepartmentExam()));
        }
        if(CollUtil.isNotEmpty(form.getLabExam())){
            // 存在实验室检查
            goods.setLabExam(JSONUtil.toJsonStr(form.getLabExam()));
        }
        if(CollUtil.isNotEmpty(form.getMedicalExam())){
            // 存在医技检查
            goods.setMedicalExam(JSONUtil.toJsonStr(form.getMedicalExam()));
        }
        if(CollUtil.isNotEmpty(form.getOtherExam())){
            // 存在其他检查
            goods.setOtherExam(JSONUtil.toJsonStr(form.getOtherExam()));
        }
        if(ArrayUtil.isNotEmpty(form.getTags())){
            // 存在特征标签
            goods.setTags(JSONUtil.toJsonStr(form.getTags()));
        }

        medExamPackageService.save(goods);
        return R.ok("套餐保存成功");

    }

    @GetMapping("/{id}")
    @SaCheckPermission(value = {"ROOT", "GOODS:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "根据ID查询套餐", description = "返回套餐信息，包含详细的体检项目")
    public R getById(
            @Parameter(description = "套餐ID", example = "1", required = true)
            // 校验规则中以往我们通常都会添加一个非空的校验规则，这里不需要。因为不可能为NULL。
            @Min(value = 1, message = "ID不能小于1")
            @PathVariable
            Integer id) {
        Map<String, Object> goods = medExamPackageService.getById(id);
        return R.ok().put("goods", goods);
    }

    @PutMapping
    @SaCheckPermission(value = {"ROOT", "GOODS:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "修改体检套餐", description = "修改体检套餐信息，包含体检项目列表和特征标签")
    public R update(@RequestBody @Valid UpdateMedExamPackageForm form) {

        MedExamPackageEntity goods = BeanUtil.toBean(form, MedExamPackageEntity.class,
                CopyOptions.create().setIgnoreProperties("departmentExam","labExam","medicalExam","otherExam","tags"));

        if(CollUtil.isNotEmpty(form.getDepartmentExam())){
            goods.setDepartmentExam(JSONUtil.toJsonStr(form.getDepartmentExam()));
        }
        if(CollUtil.isNotEmpty(form.getLabExam())){
            goods.setLabExam(JSONUtil.toJsonStr(form.getLabExam()));
        }
        if(CollUtil.isNotEmpty(form.getMedicalExam())){
            goods.setMedicalExam(JSONUtil.toJsonStr(form.getMedicalExam()));
        }
        if(CollUtil.isNotEmpty(form.getOtherExam())){
            goods.setOtherExam(JSONUtil.toJsonStr(form.getOtherExam()));
        }
        if(ArrayUtil.isNotEmpty(form.getTags())){
            goods.setTags(JSONUtil.toJsonStr(form.getTags()));
        }

        medExamPackageService.update(goods);
        return R.ok("套餐修改成功");

    }

    @PostMapping("/{id}/examItems")
    @Operation(summary = "上传检查项目数据", description = "通过excel文件导入套餐相关的体检项目")
    @SaCheckPermission(value = {"ROOT", "GOODS:INSERT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public R uploadExamItems(
            @Parameter(description = "套餐ID", required = true, example = "1") // swagger
            @PathVariable // springmvc
            @NotNull(message = "id不能为空") // bean validation校验框架
            @Min(value = 1, message = "id不能小于1")
            Integer id,
            @Parameter(description = "导入的excel文件", required = true)
            @RequestParam("file") // 这里的"file"不是随便写的，和前端提交数据时的name一致，如果前端没有提交name，name的默认值：file
            MultipartFile file) {
        // 校验文件是否合法
        if(file == null || file.isEmpty()){
            throw new HisException("文件不能为空", 400);
        }
        // 校验文件的扩展名必须是 xlsx / xls
        String filename = file.getOriginalFilename();
        if(filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))){
            throw new HisException("只支持.xlsx或.xls 格式的Excel文件", 400);
        }

        // 校验excel文件是否超过20MB。（不能超过20MB）
        if(file.getSize() > 20 * 1024 * 1024){
            throw new HisException("文件大小不能超过20MB", 400);
        }

        // 合法的excel文件
        medExamPackageService.uploadExamItems(id, file);

        return R.ok("体检项目导入成功");
    }

    @GetMapping("/{id}/examItems")
    @Operation(summary = "下载体检项目文件", description = "根据套餐ID下载对应的Excel文件")
    @SaCheckPermission(value = {"ROOT", "GOODS:SELECT", "GOODS:INSERT", "GOODS:UPDATE"}, mode = SaMode.OR)
    public void downloadExamItems(
            @Parameter(description = "套餐ID", required = true, example = "1")
            @PathVariable
            @NotNull(message = "id不能为空")
            @Min(value = 1, message = "id不能小于1")
            Integer id,
            HttpServletResponse response) {
        try {
            String fileName = id + ".xlsx";
            // 设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/x-download");
            response.setCharacterEncoding("UTF-8");

            // minio服务器上的文件路径（建议字符串定义为常量）
            String path = "mis/goods/exam_items/" + fileName;

            try(InputStream in = minIO.downloadFile(path);
                BufferedInputStream bis = new BufferedInputStream(in);
                ServletOutputStream out = response.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(out)) {

                // 工具的作用：一边读一边写。
                // 从minio上读，向浏览器上写。
                IoUtil.copy(bis, bos);
            }

        }catch (Exception e){
            log.error("文档下载失败：id={}", id);
            throw new HisException("文档下载失败");
        }
    }

    // RESTFul接口规范要求，路径中不能有动词，有动词不符合规范。
    // RESTFul接口中要求路径能够体现出来服务器端的唯一的一个资源（路径起到的作用：服务器端资源的标识。）
    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission(value = {"ROOT", "GOODS:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "套餐上架或下架", description = "上架或下架指定的套餐")
    public R updateStatus(
            @Parameter(description = "体检套餐ID", required = true, example = "1")
            @PathVariable
            @NotNull(message = "id不能为空")
            @Min(value = 1, message = "id不能小于1")
            Integer id,
            @Parameter(description = "套餐状态（true-上架，false-下架）", required = true, example = "true")
            @PathVariable
            @NotNull(message = "status不能为空")
            Boolean status) {
        medExamPackageService.updateStatus(Map.of("id", id, "status", status));
        return R.ok("套餐状态更新成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除套餐", description = "根据id批量删除套餐数据，同时删除minio上的套餐封面图片")
    @SaCheckPermission(value = {"ROOT", "GOODS:DELETE"}, mode = SaMode.OR)
    public R deleteBatch(@Valid @RequestBody RemoveExamPackageByIdsForm form) {
        medExamPackageService.removeByIds(form.getIds());
        return R.ok("删除成功");
    }

}

















