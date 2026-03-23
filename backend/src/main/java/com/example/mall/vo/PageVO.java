package com.example.mall.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页视图对象
 */
@Data
public class PageVO<T> {

    /**
     * 当前页
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> list;

    public static <T> PageVO<T> of(Long pageNum, Long pageSize, Long total, Long pages, List<T> list) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotal(total);
        pageVO.setPages(pages);
        pageVO.setList(list);
        return pageVO;
    }
}
