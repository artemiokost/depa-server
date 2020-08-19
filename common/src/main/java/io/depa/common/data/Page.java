package io.depa.common.data;

import java.util.List;

/**
 * Object that represents page.
 *
 * @author Artem Kostritsa
 */
public class Page<T> {

    private Integer number;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private List<T> list;

    public Page() {
    }

    public Page(Integer number, Integer size, Integer totalElements, List<T> list) {
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil(totalElements.doubleValue() / size);
        this.list = list;
    }

    public Integer getNumber() {
        return number;
    }
    public Integer getSize() {
        return size;
    }
    public Integer getTotalElements() {
        return totalElements;
    }
    public Integer getTotalPages() {
        return totalPages;
    }
    public List<T> getList() {
        return list;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    public void setList(List<T> list) {
        this.list = list;
    }
}