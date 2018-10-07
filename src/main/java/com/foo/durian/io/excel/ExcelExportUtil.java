package com.foo.durian.io.excel;

import com.foo.durian.io.excel.annotation.ExcelColumn;
import com.foo.durian.io.excel.annotation.ExcelColumnOrder;
import com.foo.durian.io.excel.annotation.FieldFormat;
import com.foo.durian.io.excel.formatter.Formatter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 导出Excel的通用工具
 *
 * @author mars.mao created on 2014年10月17日 下午7:50:54
 */
public class ExcelExportUtil {

    private static Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);
    private static int EXCEL_MAX_ROW_NO = 65535;
    private static int EXCEL_MAX_SHEET_CNT = 255;
    private final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 导出Excel
     *
     * @param sheetName excle表格名
     * @param excelData 要导出的数据
     * @return
     *
     * @author mars.mao created on 2014年10月17日下午7:36:26
     */
    public static <T> HSSFWorkbook export(String sheetName, List<T> excelData) {
        return export(sheetName, excelData, TableFormatterConfigurer.NONE);
    }

    public static <T> HSSFWorkbook export(String sheetName, List<T> excelData, TableFormatterConfigurer configurer) {
        // 声明一个(HSSFWorkbook)工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        return export(sheetName, excelData, workbook, configurer);
    }

    public static <T> SXSSFWorkbook export(String sheetName, Iterable<T> excelData, TableFormatterConfigurer configurer,
            Class<T> clazz) {
        // 声明一个(SXSSFWorkbook)工作薄
        SXSSFWorkbook workbook = new SXSSFWorkbook(); // SXSSFWorkbook默认只在内存中存放100行
        return export(sheetName, excelData, workbook, configurer, clazz);
    }

    /**
     * 使用该方法导出Iterable类型时不需要指定泛型的Class; 需要注意的是, 该方法适用的Iterable的实现应是能支持方便获取首元素的, 换句话说, 该Iterable的迭代是可重复且幂等的 &&
     * 获取其迭代器不需要太大的代价
     */
    @SuppressWarnings("unchecked")
    public static <T> SXSSFWorkbook export(Iterable<T> excelData, String sheetName,
            TableFormatterConfigurer configurer) {
        // 声明一个(SXSSFWorkbook)工作薄
        SXSSFWorkbook workbook = new SXSSFWorkbook(); // SXSSFWorkbook默认只在内存中存放100行
        T firstItem = Iterables.getFirst(excelData, null);
        if (firstItem != null) {
            return export(sheetName, excelData, workbook, configurer, (Class<T>) firstItem.getClass());
        }
        return workbook;
    }

    /**
     * 导出Excel
     *
     * @param sheetName excel表格名
     * @param excelData 要导出的数据
     * @param workbook 要导出的工作薄
     * @return
     *
     * @author mars.mao created on 2014年10月17日下午7:36:26
     */
    public static <T> HSSFWorkbook export(String sheetName, List<T> excelData, HSSFWorkbook workbook) {
        return export(sheetName, excelData, workbook, TableFormatterConfigurer.NONE);
    }

    public static <T> HSSFWorkbook export(String sheetName, List<T> excelData, HSSFWorkbook workbook,
            TableFormatterConfigurer configurer) {

        if (excelData == null || excelData.isEmpty() || workbook == null || StringUtils.isBlank(sheetName)) {
            return workbook;
        }

        try {

            // 定义标题行字体
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);

            int totalDataSize = excelData.size();
            int sheetCnt = totalDataSize / EXCEL_MAX_ROW_NO + 1;

            if (sheetCnt > EXCEL_MAX_SHEET_CNT) {
                throw new Exception("数据量超过了Excel的容量范围！");
            }

            for (int i = 0; i < sheetCnt; i++) {
                int fromIndex = i * EXCEL_MAX_ROW_NO;
                int toIndex = fromIndex + EXCEL_MAX_ROW_NO;
                toIndex = toIndex > totalDataSize ? totalDataSize : toIndex;
                List<T> sheetData = excelData.subList(fromIndex, toIndex);

                // 生成一个表格
                Sheet sheet = workbook.createSheet(sheetName + "_" + i);

                // 生成标题行
                createHeader(sheetData, sheet, font, configurer);

                // 遍历集合数据，产生数据行
                createBody(sheetData, sheet, configurer);
            }

            logger.info("导出的数据行数(不含表头): writeCount={}", excelData.size());
            return workbook;
        } catch (Exception e) {
            logger.error("导出Excel异常！", e);
        }

        return workbook;
    }

    /**
     * 导出Excel, 使用SXSSFWorkbook支持大数据量的导出
     *
     * @param sheetName excel表格名
     * @param excelData 要导出的数据
     * @param workbook 要导出的工作薄
     * @param configurer 配置数据格式化行为的对象
     * @param clazz 待导出数据的类型信息
     * @return
     */
    public static <T> SXSSFWorkbook export(String sheetName, Iterable<T> excelData, SXSSFWorkbook workbook,
            TableFormatterConfigurer configurer, Class<T> clazz) {
        final Iterator<T> externalIterator;
        if (excelData == null || !(externalIterator = excelData.iterator()).hasNext() || workbook == null
                || StringUtils.isBlank(sheetName)) {
            return workbook;
        }

        try {
            // 定义标题行字体
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);

            int writeCount = 0; // 记录导出的行数
            int i = 0;
            while (externalIterator.hasNext()) {
                // 生成一个表格
                Sheet sheet = workbook.createSheet(sheetName + "_" + i);

                // 生成标题行
                createHeader(sheet, font, configurer, clazz);

                // 遍历集合数据，产生数据行
                writeCount += createBody(new Iterable<T>() {
                    public Iterator<T> iterator() {
                        return new Iterator<T>() {
                            int lineCount = 0;

                            public boolean hasNext() {
                                return lineCount < EXCEL_MAX_ROW_NO && externalIterator.hasNext();
                            }

                            public T next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException("没有更多的元素");
                                }
                                lineCount++;
                                return externalIterator.next();
                            }

                            public void remove() {
                                throw new UnsupportedOperationException("不支持删除操作");
                            }
                        };
                    }
                }, sheet, configurer, clazz);

                if ((++i) == EXCEL_MAX_SHEET_CNT) {
                    throw new Exception("数据量超过了Excel的容量范围！");
                }
            }

            logger.info("导出的数据行数(不含表头): writeCount={}", writeCount);
            return workbook;
        } catch (Exception e) {
            logger.error("导出Excel异常！", e);
        }

        return workbook;
    }

    /**
     * 创建表格数据
     *
     * @param excelData 要导出的数据
     * @param sheet excel表格
     * @param configurer 配置数据格式化行为的对象
     * @return 返回创建的数据行数
     *
     * @author mars.mao created on 2014年10月17日下午3:43:43
     */
    @SuppressWarnings("unchecked")
    private static <T> int createBody(List<T> excelData, Sheet sheet, TableFormatterConfigurer configurer)
            throws Exception {
        if (CollectionUtils.isEmpty(excelData)) {
            return 0;
        }

        Class<?> dataClass = excelData.get(0).getClass();
        return createBody(excelData, sheet, configurer, dataClass);
    }

    /**
     * 创建表格数据
     *
     * @param excelData 要导出的数据
     * @param sheet excel表格
     * @param configurer 配置数据格式化行为的对象
     * @param clazz 要导出的对象的类型信息, 用以获取相关注解以及字段值
     * @return 返回创建的数据行数
     */
    private static <T> int createBody(Iterable<T> excelData, Sheet sheet, TableFormatterConfigurer configurer,
            Class<?> clazz) throws Exception {
        if (excelData == null) {
            return 0;
        }
        List<Field> fields = getExportableFields(clazz, configurer);
        trySort(fields, clazz); // 尝试对列进行排序
        Map<String, Method> methodMap = getFormattingMethods(clazz);

        int dataRowIndex = 1;
        for (T data : excelData) {
            // 创建数据行
            Row dataRow = sheet.createRow(dataRowIndex);

            int columnIndex = 0;
            for (Field field : fields) {
                ExcelColumn columnHeader = field.getAnnotation(ExcelColumn.class);
                // 创建列
                String textValue;
                Method formattingMethod = methodMap.get(field.getName());
                if (formattingMethod != null) {
                    // 优先使用"格式化方法"对属性进行格式化
                    formattingMethod.setAccessible(true);
                    textValue = String.valueOf(formattingMethod.invoke(data));
                } else {
                    // 在没有"格式化方法"时使用@ExcelColumn指定的格式化方式
                    textValue = getTextValue(data, field, columnHeader, configurer);
                }
                Cell cell = dataRow.createCell(columnIndex);
                // HSSFRichTextString text = new HSSFRichTextString(textValue);
                // cell.setCellValue(text);
                cell.setCellValue(textValue);

                columnIndex++;
            }

            dataRowIndex++;
        }

        return dataRowIndex - 1;
    }

    /**
     * 生成Excel的标题行 TO.DO. 代码优化--该方法可删除
     *
     * @param excelData 导出的数据列表
     * @param sheet excel表
     * @return
     *
     * @author mars.mao created on 2014年10月17日下午2:08:41
     */
    @SuppressWarnings("unchecked")
    private static <T> void createHeader(List<T> excelData, Sheet sheet, Font font,
            TableFormatterConfigurer configurer) {
        if (CollectionUtils.isEmpty(excelData)) {
            return;
        }

        Class<?> dataClass = excelData.get(0).getClass();
        createHeader(sheet, font, configurer, dataClass);
    }

    private static void createHeader(Sheet sheet, Font font, TableFormatterConfigurer configurer, Class<?> clazz) {
        List<Field> fields = getExportableFields(clazz, configurer);
        trySort(fields, clazz); // 尝试对列进行排序

        Row headerRow = sheet.createRow(0);
        int columnIndex = 0;
        for (Field field : fields) {
            ExcelColumn columnHeader = field.getAnnotation(ExcelColumn.class);
            // 获取指定的列标题和列宽度
            String columnTitle = getRealTitle(columnHeader.headerName(), configurer); // 允许替换为动态配置的表头
            int columnWidth = columnHeader.columnWidth();

            // 创建列
            Cell cell = headerRow.createCell(columnIndex);
            // 设置列标题
            if (sheet instanceof HSSFSheet) {
                RichTextString text = new HSSFRichTextString(columnTitle);
                text.applyFont(font);
                cell.setCellValue(text);
            } else {
                cell.setCellValue(columnTitle);
            }
            // 设置列宽度
            sheet.setColumnWidth(columnIndex, columnWidth * 256);

            columnIndex++;
        }
    }

    /**
     * 获取格式化的文本内容
     *
     * @param obj 输入对象
     * @param field 对象域
     * @param columnHeader ExcelColumn注解的配置实例
     * @param configurer TableFormatterConfigurer的配置实例
     * @return 格式化后的文本
     */
    @SuppressWarnings("unchecked")
    private static <T> String getTextValue(T obj, Field field, ExcelColumn columnHeader,
            TableFormatterConfigurer configurer) {
        String aimPattern = columnHeader.pattern();
        Object fieldValue = getFieldValue(obj, field); // 反射获取字段的值
        String textValue = null;

        Class<? extends Formatter> formatterClass = columnHeader.formatterClass();
        boolean allowAlternativeFormatter = columnHeader.allowAlternativeFormatter();

        boolean formatted = false;
        try {
            // 1) 尝试使用注册在的formatterMap中的formatter; 以属性名为key的formatterMap优先
            if (allowAlternativeFormatter) {
                Formatter registeredFormatter = configurer.getFormatter(field.getName());
                if (registeredFormatter == null && fieldValue != null) {
                    registeredFormatter = configurer.getFormatter(fieldValue.getClass());
                }
                if (registeredFormatter != null) {
                    textValue = registeredFormatter.format(fieldValue);
                    formatted = true;
                }
            }
            // 2) 没有获取到注册的formatter, 则尝试使用指定的formatterClass
            if (!formatted && formatterClass != Formatter.None.class) {
                Formatter formatter = formatterClass.newInstance();
                textValue = formatter.format(fieldValue);
                formatted = true;
            }
        } catch (Exception e) {
            logger.error("导出Excel使用formatter格式化出错: {}", e.getMessage(), e);
            formatted = false;
        }
        if (!formatted) {
            /*
             * 3) [未指定formatter, 且未开启formatter的注册] 或者 [以上格式化失败]; 使用默认的处理方式...
             */
            textValue = defaultToString(fieldValue, aimPattern);
        }
        return textValue;
    }

    /*
     * 默认的格式处理方式 (与历史版本兼容)
     */
    private static String defaultToString(Object fieldValue, String aimPattern) {
        String textValue = " ";
        if (fieldValue != null) {
            textValue = fieldValue.toString();
        }
        if (fieldValue instanceof Date) {
            try {
                String pattern = StringUtils.isBlank(aimPattern) ? DEFAULT_DATE_FORMAT : aimPattern;
                Date date = (Date) fieldValue;
                textValue = DateFormatUtils.format(date, pattern);
            } catch (Exception e) {
                logger.error("导出Excel日期格式化错误！", e);
            }
        } else if (fieldValue instanceof DateTime) { // 添加对DateTime类型的兼容(采用与Date类型一样的pattern设置) [by foolish
                                                     // 2016/09/05]
            try {
                String pattern = StringUtils.isBlank(aimPattern) ? DEFAULT_DATE_FORMAT : aimPattern;
                DateTime dateTime = (DateTime) fieldValue;
                textValue = dateTime.toString(pattern);
            } catch (Exception e) {
                logger.error("导出Excel日期格式化错误！", e);
            }
        } else if (fieldValue instanceof Number) {
            if (StringUtils.isNotBlank(aimPattern)) {
                try {
                    double doubleValue = Double.parseDouble(fieldValue.toString());
                    DecimalFormat df1 = new DecimalFormat(aimPattern);
                    textValue = df1.format(doubleValue);
                } catch (Exception e) {
                    logger.error("导出Excel数字格式化错误！", e);
                }
            }
        }
        return textValue;
    }

    /**
     * 反射获取字段的值
     *
     * @param obj 对象
     * @param field 字段
     * @return
     *
     * @author mars.mao created on 2014年10月17日下午2:58:53
     */
    private static <T> Object getFieldValue(T obj, Field field) {
        Object fieldValue = " ";

        try {
            field.setAccessible(true);
            fieldValue = field.get(obj);
            if (fieldValue != null) {
                return fieldValue;
            }
        } catch (Exception e) {
            logger.error("导出Excel动态获取字段值异常", e);
        }
        return fieldValue;
    }

    /**
     * 根据给定类的信息获取其所有字段 (包括继承而来的字段!); 字段按照一定的顺序排列
     */
    private static List<Field> getExportableFields(Class<?> clazz, final TableFormatterConfigurer configurer) {
        List<Field> fields = Lists.newArrayList();
        /* 循环向上查找, 以支持父类中的属性导出 [by foolish 2016/09/05] */
        for (Class<?> dataClass : getClassesInHierarchy(clazz)) {
            fields.addAll(Arrays.asList(dataClass.getDeclaredFields()));
        }

        // 过滤不需要导出的属性
        fields = Lists.newArrayList(Iterables.filter(fields, new Predicate<Field>() {
            @Override
            public boolean apply(Field field) {
                ExcelColumn columnHeader = field.getAnnotation(ExcelColumn.class);
                if (columnHeader == null || configurer.getHeadersToBeFiltered().contains(columnHeader.headerName())) {
                    return false;
                }
                return true;
            }
        }));
        return fields;
    }

    /**
     * 按指定的字段顺序排序, 该方法是对{@code @JsonPropertyOrder}注解效果的模拟; 字段顺序同{@link ExcelColumnOrder#orderByFields()}中的顺序
     */
    private static void trySort(List<Field> fields, Class<?> clazz) {
        ExcelColumnOrder columnOrder = null;
        // 取最后一个标注了@ExcelColumnOrder注解的类
        List<Class<?>> classes = getClassesInHierarchy(clazz);
        Collections.reverse(classes);
        for (Class<?> aClass : classes) {
            if ((columnOrder = aClass.getAnnotation(ExcelColumnOrder.class)) != null) {
                break;
            }
        }
        /*
         * 根据ExcelColumnOrder.orderByFields()的字段顺序进行排序
         */
        if (columnOrder != null && columnOrder.orderByFields().length != 0) {
            final List<String> fieldsInOrder = Arrays.asList(columnOrder.orderByFields());

            // 仅取注解中指定的字段列表
            List<Field> fieldsToBeSorted = ImmutableList.copyOf(Collections2.filter(fields, new Predicate<Field>() {
                @Override
                public boolean apply(Field input) {
                    return fieldsInOrder.contains(input.getName());
                }
            }));
            // 按指定的字段(名)顺序排序
            Ordering<Field> orderingByFields = Ordering.explicit(fieldsInOrder)
                    .onResultOf(new Function<Field, String>() {
                        @Override
                        public String apply(Field input) {
                            return input.getName();
                        }
                    });
            fields.removeAll(fieldsToBeSorted);
            fields.addAll(0, orderingByFields.sortedCopy(fieldsToBeSorted));
        }
    }

    /**
     * 替换表头
     */
    private static String getRealTitle(String title, TableFormatterConfigurer configurer) {
        String replacement = configurer.getHeadersMapping().get(title);
        return StringUtils.isNotBlank(replacement) ? replacement : title;
    }

    private static Map<String, Method> getFormattingMethods(Class<?> clazz) {
        List<Method> methods = Lists.newArrayList();
        /* 循环向上查找, 以支持父类中的格式化方法 [by foolish 2017/02/09] */
        for (Class<?> dataClass : getClassesInHierarchy(clazz)) {
            methods.addAll(Arrays.asList(dataClass.getDeclaredMethods()));
        }

        // 过滤和格式化无关的方法
        methods = Lists.newArrayList(Iterables.filter(methods, new Predicate<Method>() {
            @Override
            public boolean apply(Method method) {
                return method.getAnnotation(FieldFormat.class) != null;
            }
        }));

        return Maps.uniqueIndex(methods, new Function<Method, String>() {
            @Override
            public String apply(Method method) {
                return method.getAnnotation(FieldFormat.class).fieldName();
            }
        });
    }

    /**
     * 获取继承体系中的类
     */
    private static List<Class<?>> getClassesInHierarchy(Class<?> clazz) {
        List<Class<?>> classes = Lists.newArrayList();

        Stack<Class<?>> classStack = new Stack<Class<?>>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            classStack.push(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        while (!classStack.isEmpty()) {
            classes.add(classStack.pop());
        }
        return classes;
    }

    public static Workbook dynamicExport(String sheetName, List<RowRecord> excelData) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(excelData) || StringUtils.isBlank(sheetName)) {
            return null;
        }
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 标题行字体
        Font font = workbook.createFont();
        font.setBold(true);
        // 定义单元格格式
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 分割sheet
        int totalCnt = excelData.size();
        int sheetCnt = totalCnt / EXCEL_MAX_ROW_NO + 1;
        if (sheetCnt > EXCEL_MAX_SHEET_CNT) {
            throw new RuntimeException("数据量超过了Excel的容量范围！");
        }

        for (int i = 0; i < sheetCnt; i++) {
            int fromIndex = i * EXCEL_MAX_ROW_NO;
            int toIndex = fromIndex + EXCEL_MAX_ROW_NO;
            toIndex = toIndex > totalCnt ? totalCnt : toIndex;
            List<RowRecord> sheetData = excelData.subList(fromIndex, toIndex);
            // 生成一个表格
            Sheet sheet = workbook.createSheet(sheetName + "_" + i);
            // 生成标题行
            createHeader(sheetData, sheet, font, cellStyle);
            // 遍历集合数据，产生数据行
            createBody(sheetData, sheet);
        }

        return workbook;
    }

    private static void createHeader(List<RowRecord> excelData, Sheet sheet, Font font, CellStyle cellStyle) {

        if (org.apache.commons.collections4.CollectionUtils.isEmpty(excelData)) {
            return;
        }
        RowRecord rowRecord = excelData.get(0);

        createHeader(sheet, rowRecord, font, cellStyle);
    }

    private static void createHeader(Sheet sheet, RowRecord rowRecord, Font font, CellStyle cellStyle) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(rowRecord.getColumnRecordList())) {
            return;
        }
        // 获取表头节点的层数
        List<ColumnRecord> columnRecordList = rowRecord.getColumnRecordList();
        int HeaderLayerCnt = 1;
        for (ColumnRecord columnRecord : columnRecordList) {
            if (HeaderLayerCnt < columnRecord.getColumnNameList().size()) {
                HeaderLayerCnt = columnRecord.getColumnNameList().size();
            }
        }
        int i = 0;
        int columnIndex;
        int preColumnIndex;
        String preHeader;
        while (i < HeaderLayerCnt) {
            preHeader = null;
            preColumnIndex = columnIndex = 0;
            Row headerRow = sheet.createRow(i);
            for (ColumnRecord columnRecord : rowRecord.getColumnRecordList()) {
                if (columnRecord.getColumnNameList().size() < i + 1) {
                    columnIndex++;
                    preColumnIndex++;
                    continue;
                }
                if (preHeader == null || !columnRecord.getColumnNameList().get(i).equals(preHeader)) {
                    // 获取指定的列标题和列
                    if (columnIndex - preColumnIndex > 1) {
                        sheet.addMergedRegion(new CellRangeAddress(i, i, preColumnIndex, columnIndex - 1));
                    }
                    preColumnIndex = columnIndex;
                    String columnTitle = columnRecord.getColumnNameList().get(i);
                    // 创建列
                    Cell cell = headerRow.createCell(columnIndex);
                    // 设置列标题
                    RichTextString text = new HSSFRichTextString(columnTitle);
                    text.applyFont(font);
                    cell.setCellValue(text);
                    cell.setCellStyle(cellStyle);
                    preHeader = columnRecord.getColumnNameList().get(i);
                }
                // 最后一列判定
                if ((columnIndex == rowRecord.getColumnRecordList().size() - 1)
                        && (columnIndex - preColumnIndex >= 1)) {
                    sheet.addMergedRegion(new CellRangeAddress(i, i, preColumnIndex, columnIndex));
                }
                columnIndex++;
            }
            i++;
        }

    }

    private static void createBody(List<RowRecord> rowRecords, Sheet sheet) {
        int rowIndex = sheet.getLastRowNum() + 1;
        for (RowRecord rowRecord : rowRecords) {
            Row dataRow = sheet.createRow(rowIndex);
            int columnIndex = 0;
            for (ColumnRecord columnRecord : rowRecord.getColumnRecordList()) {
                String textValue = getTextValue(columnRecord);
                Cell cell = dataRow.createCell(columnIndex);
                cell.setCellValue(textValue);
                sheet.autoSizeColumn(columnIndex, true);
                columnIndex++;
            }
            rowIndex++;
        }
    }

    private static String getTextValue(ColumnRecord columnRecord) {
        Class<? extends Formatter> formatterClass = columnRecord.getFormatter();
        if (formatterClass.equals(Formatter.DefaultFormatter.class)) {
            return columnRecord.getPropertyValue().toString();
        }
        try {
            Formatter<Object> formatter = formatterClass.newInstance();
            return formatter.format(columnRecord.getPropertyValue());
        } catch (Exception e) {
            logger.error("导出Excel使用formatter格式化出错: {}", e.getMessage(), e);
        }

        return null;
    }

}
