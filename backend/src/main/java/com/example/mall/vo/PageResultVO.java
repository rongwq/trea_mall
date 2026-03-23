package com.example.mall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResultVO<T> implements Serializable {
    private List<T> records;
    private Long total;
    private Long pages;
    private Long current;
    private Long size;

    public static <T> PageResultVO<T> of(List<T> records, Long total, Long current, Long size) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((total + size - 1) / size);
        return result;
    }
}
