package com.example.mall.common;

import com.example.mall.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    private Integer code;
    private String message;
    private PageData<T> data;

    public static <T> PageResult<T> success(List<T> list, Long total, Long pageNum, Long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");

        PageData<T> pageData = new PageData<>();
        pageData.setList(list);
        pageData.setTotal(total);
        pageData.setPageNum(pageNum);
        pageData.setPageSize(pageSize);
        pageData.setPages((total + pageSize - 1) / pageSize);

        result.setData(pageData);
        return result;
    }

    public static <T> PageResult<T> success(PageVO<T> pageVO) {
        PageResult<T> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");

        PageData<T> pageData = new PageData<>();
        pageData.setList(pageVO.getList());
        pageData.setTotal(pageVO.getTotal());
        pageData.setPageNum(pageVO.getPageNum());
        pageData.setPageSize(pageVO.getPageSize());
        pageData.setPages(pageVO.getPages());

        result.setData(pageData);
        return result;
    }

    @Data
    public static class PageData<T> {
        private List<T> list;
        private Long total;
        private Long pageNum;
        private Long pageSize;
        private Long pages;
    }
}
