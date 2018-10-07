package com.foo.durian.io.excel;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * 动态生成Excel，行记录。
 * Created by foolish on 2017/1/9.
 */
@SuppressWarnings("all")
public class RowRecord implements Serializable {

    private static final long serialVersionUID = 6658501531531991687L;

    private List<ColumnRecord> columnRecordList = Lists.newArrayList();

    public List<ColumnRecord> getColumnRecordList() {
        return columnRecordList;
    }

    public void setColumnRecordList(List<ColumnRecord> columnRecordList) {
        this.columnRecordList = columnRecordList;
    }
}
