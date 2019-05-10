package com.cloudera.phoenixdemo.entity;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zc on 2018/4/10.
 */

@Data
@SuppressWarnings("unchecked")
public class PageModel<T> {
    /*
    分页模型
    * */
    @NotNull
//    @AssertTrue(message = "分页参数错误(paging不能为空)")
    private boolean paging = true;

    //请求页码
    @NotNull(message = "分页参数错误(pageNum不能为空)")
    @Min(1)
    private Integer pageNum;

    //每页多少条
    @NotNull(message = "分页参数错误(limit不能为空)")
    @Min(0)
    private Integer limit;

    //分页总数
    private int pageCount;

    //总记录数
    private long total;

    //是否是最后一页
    private boolean lastPage;

    //排序规则
    private String orderBy = "create_time desc";

    //聚合数据
    private Object totalData;

    //数据
    private List<T> results = new ArrayList<>();
    //数据
    private String condition;

    private Integer[] ids;

    public void setPaging(boolean paging) {
        if (!paging) {
            pageNum = 1;
            limit = 0;
        }
        this.paging = paging;
    }

    public static <T> PageModel<T> convertToPageModel(PageInfo<T> pageResult) {
        if (pageResult.getTotal() <= 0) {
            //throw new NotFoundException("没有找到相关数据");
        }
        PageModel pageModel = new PageModel();
        pageModel.setPageNum(pageResult.getPageNum());
        pageModel.setLimit(pageResult.getPageSize());
        pageModel.setPageCount(pageResult.getPages());
        pageModel.setTotal(pageResult.getTotal());
        pageModel.setResults(pageResult.getList());
        pageModel.setLastPage(pageResult.isIsLastPage());
        return pageModel;
    }


}
