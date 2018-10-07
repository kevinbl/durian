package com.foo.durian.io.excel.view;


import com.foo.durian.io.excel.ExcelExportUtil;
import com.foo.durian.io.excel.TableFormatterConfigurer;
import com.foo.durian.io.excel.annotation.ExcelTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public class ExcelView<T> extends AbstractExcelView {

    private static Logger logger = LoggerFactory.getLogger(ExcelView.class);

    private static String DEFAULTTABLENAME = "DefaultExcel";

    // Excel中的数据
    private List<T> dataList;
    // Excel文件名
    private String fileName;

    /*
      格式化相关的配置对象
     */
    private TableFormatterConfigurer tableFormatterConfigurer = TableFormatterConfigurer.NONE;

    /**
     * 使用注解的文件名生成Excel
     * @param dataList 数据
     */
    public ExcelView(List<T> dataList) {
        this.dataList = dataList;
    }

    public ExcelView(List<T> dataList, TableFormatterConfigurer configurer) {
        this(dataList);
        this.tableFormatterConfigurer = configurer;
    }

    /**
     * 使用指定的文件名生成Excel
     * @param dataList 数据
     * @param fileName 文件名
     */
    public ExcelView(List<T> dataList, String fileName) {
        this.dataList = dataList;
        this.fileName = fileName;
    }

    public ExcelView(List<T> dataList, String fileName, TableFormatterConfigurer configurer) {
        this(dataList, fileName);
        this.tableFormatterConfigurer = configurer;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        if (dataList == null || dataList.isEmpty()) {
            logger.info("ExcelView.buildExcelDocument model is empty");
            return;
        }

        logger.info("ExcelView.buildExcelDocument");
        try {
            if (StringUtils.isBlank(fileName)) {
                fileName = DEFAULTTABLENAME;
                Class<? extends Object> dataCls = dataList.get(0).getClass();
                ExcelTable excelTable = dataCls.getAnnotation(ExcelTable.class);
                if(excelTable != null) {
                    fileName = excelTable.fileName();
                }
            }
            // 使用可定制格式化方式的导出方法
            ExcelExportUtil.export("sheet", dataList, workbook, tableFormatterConfigurer);

            String rtn = "filename=\"" + new String((fileName + ".xls").getBytes("utf-8"), "ISO8859-1") + "\"";
            response.setHeader("Content-disposition", "attachment;" + rtn);
        } catch (Exception e) {
            logger.error("ExcelView.buildExcelDocument error!", e);
        }
    }

}
