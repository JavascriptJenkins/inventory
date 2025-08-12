package com.techvvs.inventory.service.metrc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PagedLocationTypesDto {

    @JsonProperty("Data")
    private List<LocationTypeDto> data;

    @JsonProperty("Total")
    private int total;

    @JsonProperty("TotalRecords")
    private int totalRecords;

    @JsonProperty("PageSize")
    private int pageSize;

    @JsonProperty("RecordsOnPage")
    private int recordsOnPage;

    @JsonProperty("Page")
    private int page;

    @JsonProperty("CurrentPage")
    private int currentPage;

    @JsonProperty("TotalPages")
    private int totalPages;

    // getters/setters
    public List<LocationTypeDto> getData() { return data; }
    public void setData(List<LocationTypeDto> data) { this.data = data; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getRecordsOnPage() { return recordsOnPage; }
    public void setRecordsOnPage(int recordsOnPage) { this.recordsOnPage = recordsOnPage; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
