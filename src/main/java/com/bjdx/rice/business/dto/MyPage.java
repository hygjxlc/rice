package com.bjdx.rice.business.dto;

import com.github.pagehelper.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@ApiModel
public class MyPage<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    //当前页
    @ApiModelProperty("当前页")
    private int pageNum;
    //每页的数量
    @ApiModelProperty("每页的数量")
    private int pageSize;

    //总记录数
    @ApiModelProperty("总记录数")
    private long total;
    //总页数
    @ApiModelProperty("总页数")
    private int pages;
    //结果集
    @ApiModelProperty("结果集")
    private List<T> list;

    //额外的参数
    @ApiModelProperty("额外的参数")
    public Object extraParam;


    public MyPage() {
    }






    /**
     * 包装Page对象
     *
     * @param list
     */
    public MyPage(List<T> list) {
        this(list, 8);
    }

    /**
     * 包装Page对象
     *
     * @param list          page结果
     * @param navigatePages 页码数量
     */
    public MyPage(List<T> list, int navigatePages) {
        if (list instanceof Page) {
            Page page = (Page) list;
            this.pageNum = page.getPageNum();
            this.pageSize = page.getPageSize();

            this.pages = page.getPages();
            this.list = page;
            this.total = page.getTotal();

        } else if (list instanceof Collection) {
            this.pageNum = 1;
            this.pageSize = list.size();

            this.pages = this.pageSize > 0 ? 1 : 0;
            this.list = list;

            this.total = list.size();

        }
    }






    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setExtraParam(Object obj) {
        extraParam = obj;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PageInfo{");
        sb.append("pageNum=").append(pageNum);
        sb.append(", pageSize=").append(pageSize);

        sb.append(", total=").append(total);
        sb.append(", pages=").append(pages);
        sb.append(", list=").append(list);

        sb.append(", navigatepageNums=");
        sb.append('}');
        return sb.toString();
    }
}
