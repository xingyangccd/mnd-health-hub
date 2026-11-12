package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedExamPackageEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MedExamPackageService {
    /**
     * 分页查询体检套餐
     *
     * @param param 查询条件
     * @return 分页数据
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> param);

    /**
     * 上传套餐封面图片到minio服务器
     *
     * @param file 文件本身
     * @return 文件在minio服务器上存储的相对路径
     */
    String uploadImg(MultipartFile file);

    /**
     * 保存套餐信息
     *
     * @param goods 套餐信息
     */
    void save(MedExamPackageEntity goods);

    /**
     * 根据id查询套餐的信息
     *
     * @param id 套餐的id
     * @return 套餐的信息
     */
    Map<String, Object> getById(Integer id);

    /**
     * 更新套餐信息
     *
     * @param goods 套餐信息
     */
    void update(MedExamPackageEntity goods);

    /**
     * 将excel文件当中具体的体检项目导入到数据库表的exam_items字段上，并且最终要求excel文件也要保存到minio服务器上。
     *
     * @param goodsId 套餐ID
     * @param file    excel文件本身
     */
    void uploadExamItems(Integer goodsId, MultipartFile file);

    /**
     * 更新套餐的状态（上架 下架）
     * @param params 参数包括id和status
     */
    void updateStatus(Map<String, Object> params);

    /**
     * 批量删除套餐
     * @param ids 套餐id
     */
    void removeByIds(Integer[] ids);
}
