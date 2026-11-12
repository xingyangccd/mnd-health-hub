package com.mnd.healthhub.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页查询结果封装类
 * 用于 Element Plus 分页组件
 */
@Data
@Schema(description = "分页查询结果")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total) {
        this.records = records;
        this.total = total;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total) {
        return new PageResult<>(records, total);
    }
}