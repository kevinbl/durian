package com.foo.durian.io.excel;

import com.foo.durian.exception.BusinessException;
import com.foo.durian.exception.FileParseException;
import com.foo.durian.io.FileLineCell;
import com.foo.durian.io.Files;
import com.foo.durian.io.excel.parser.PropertyParser;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.monitorjbl.xlsx.StreamingReader;
import com.monitorjbl.xlsx.impl.StreamingRow;
import com.monitorjbl.xlsx.impl.StreamingSheet;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * version 1.0.0 Created by f on 16/8/1 下午7:29. update by zhaozheng,zhao
 */
@SuppressWarnings("ALL")
public class ExcelParser {

    private static final Logger logger = LoggerFactory.getLogger(ExcelParser.class);

    // 单个sheet
    public static <T> List<T> parse(File file, FileLineCell[] fileLineCells, Class<T> clazz) throws Exception {
        return doParse(file, fileLineCells, clazz, false, null);
    }

    public static <T> List<T> parse(File file, FileLineCell[] fileLineCells, Class<T> clazz,
            boolean requireMatchedHeaders) throws Exception {
        return doParse(file, fileLineCells, clazz, requireMatchedHeaders, null);
    }

    public static <T> void parse(File file, FileLineCell[] fileLineCells, Class<T> clazz, boolean requireMatchedHeaders,
            LineProcessor<T> processor) throws Exception {
        doParse(file, fileLineCells, clazz, requireMatchedHeaders, processor);
    }

    private static <T> List<T> doParse(File file, FileLineCell[] fileLineCells, Class<T> clazz,
            boolean requireMatchedHeaders, LineProcessor<T> processor) throws Exception {
        validateType(file);
        Closer closer = Closer.create();
        try {
            InputStream inp = new BufferedInputStream(new FileInputStream(file));
            closer.register(inp);
            return parse(inp, fileLineCells, clazz, requireMatchedHeaders, processor);
        } finally {
            closer.close();
        }
    }

    // 多个sheet
    public static <T> List<List<T>> parse(File file, FileLineCell[][] fileLineCells, Class[] clazzs) throws Exception {
        return doParse(file, fileLineCells, clazzs, false, null);
    }

    public static <T> List<List<T>> parse(File file, FileLineCell[][] fileLineCells, Class[] clazzs,
            boolean requireMatchedHeaders) throws Exception {
        return doParse(file, fileLineCells, clazzs, requireMatchedHeaders, null);
    }

    public static <T> void parse(File file, FileLineCell[][] fileLineCells, Class[] clazzs,
            boolean requireMatchedHeaders, LineProcessor<T> processor) throws Exception {
        doParse(file, fileLineCells, clazzs, requireMatchedHeaders, processor);
    }

    private static <T> List<List<T>> doParse(File file, FileLineCell[][] fileLineCells, Class[] clazzs,
            boolean requireMatchedHeaders, LineProcessor<T> processor) throws Exception {
        validateType(file);
        Closer closer = Closer.create();
        try {
            InputStream inp = new BufferedInputStream(new FileInputStream(file));
            closer.register(inp);
            return parse(inp, fileLineCells, clazzs, requireMatchedHeaders, processor);
        } finally {
            closer.close();
        }
    }

    /**
     * 读取excel 中的第一个sheet
     * 
     * @return 当processor 为null 的时候，整表读取返回一个list，当不为null 的时候返回一个空集合
     * @throws Exception
     */
    public static <T> List<T> parse(InputStream inp, FileLineCell[] fileLineCells, Class<T> clazz,
            boolean requireMatchedHeaders, LineProcessor<T> processor) throws Exception {

        Workbook wb = WorkbookFactory.create(inp);
        int num = wb.getNumberOfSheets();
        if (num == 0) {
            return Collections.EMPTY_LIST;
        }

        return readSheet(wb.getSheetAt(0), fileLineCells, clazz, requireMatchedHeaders, processor);

    }

    /**
     * 读取excel 中的所有sheet
     * 
     * @return 当processor 为null 的时候，整表读取返回一个list，当不为null 的时候返回一个空集合
     * @throws Exception
     */
    public static <T> List<List<T>> parse(InputStream inp, FileLineCell[][] fileLineCells, Class[] clazzs,
            boolean requireMatchedHeaders, LineProcessor<T> processor) throws Exception {

        List<List<T>> result = Lists.newArrayList();
        Workbook wb = WorkbookFactory.create(inp);
        int num = wb.getNumberOfSheets();
        if (num == 0) {
            return Collections.EMPTY_LIST;
        }
        int sheetCount = wb.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            List list = readSheet(wb.getSheetAt(i), fileLineCells[i], clazzs[i], requireMatchedHeaders, processor);
            result.add(list);
        }
        return result;
    }

    /**
     * 读取excel中的第一个sheet, 实际的读取和解析过程封装在了返回结果中; 注意, Iterable的低内存消耗是否生效取决于excel的版本
     *
     * @return 返回一个封装了逐行解析的逻辑的Iterable对象, 效果类似LineProcessor, 但更具可操作性
     * @throws Exception
     */
    public static <T> Iterable<T> parse(InputStream inp, FileLineCell[] fileLineCells, Class<T> clazz,
            boolean requireMatchedHeaders) throws Exception {

        Workbook wb;
        // 为了避免使用UserModel创建workbook的大量内存消耗, 此处引入一个StreamingWorkbook类型 (仅支持xlsx)
        // check excel type
        if (!inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }
        if (!POIFSFileSystem.hasPOIFSHeader(inp)) { // excel2007+
            logger.info("Not of excel2003 or lower version, try using StreamingWorkbook...");
            wb = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(inp); // defaults to read the only
                                                                                         // sheet at 0
            int num = wb.getNumberOfSheets();
            if (num == 0) {
                return Collections.EMPTY_LIST;
            }
            return readSheet((StreamingSheet) wb.getSheetAt(0), fileLineCells, clazz, requireMatchedHeaders); // streaming
                                                                                                              // sheet
        } else { // excel2003-
            logger.info("Is of excel2003 or lower version, use default Workbook...");
            wb = WorkbookFactory.create(inp);
            int num = wb.getNumberOfSheets();
            if (num == 0) {
                return Collections.EMPTY_LIST;
            }
            return readSheet(wb.getSheetAt(0), fileLineCells, clazz, requireMatchedHeaders);
        }
    }

    @Deprecated
    private <T> List<T> parse2007(File file, FileLineCell[] fileLineCells, Class<T> clazz) {
        return null;
    }

    /**
     * @return 如果processor 不为空，则返回一个空集合
     * @throws Exception
     */
    private static <T> List<T> readSheet(Sheet sheet, FileLineCell[] fileLineCells, Class<T> clazz,
            boolean requireMatchedHeaders, LineProcessor<T> processor) throws Exception {

        List<T> result = Lists.newArrayList();
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        if (firstRow == lastRow && firstRow == 0) {
            return result;
        }
        Map<Integer, FileLineCell> headers = getHeaders(sheet.getRow(firstRow), fileLineCells);
        checkHeaders(headers, fileLineCells, requireMatchedHeaders);

        int readCount = 0; // 记录读取的行数
        try {
            for (int i = firstRow + 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                T t = getRow(row, headers, clazz);
                readCount++;
                // 逐行处理
                if (processor != null) {
                    boolean flag = processor.process(t);
                    if (!flag) {
                        break;
                    }
                } else if (t != null) {
                    // 整表读取
                    result.add(t);
                }
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            logger.info("读取的数据行数(不含表头): readCount={}", readCount);
        }
        return result;
    }

    /**
     * @return 返回一个元素迭代器, 按行读取所需元素
     */
    private static <T> Iterable<T> readSheet(final Sheet sheet, FileLineCell[] fileLineCells, final Class<T> clazz,
            boolean requireMatchedHeaders) {
        List<T> result = Lists.newArrayList();
        final int firstRow = sheet.getFirstRowNum();
        final int lastRow = sheet.getLastRowNum();
        if (firstRow == lastRow && firstRow == 0) {
            return result;
        }
        final Map<Integer, FileLineCell> headers = getHeaders(sheet.getRow(firstRow), fileLineCells);
        checkHeaders(headers, fileLineCells, requireMatchedHeaders);

        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    int index = firstRow + 1;
                    int readCount = 0; // 记录读取的行数

                    public boolean hasNext() {
                        return index <= lastRow;
                    }

                    public T next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException("当前excel sheet已读完");
                        }
                        try {
                            T nextRow = getRow(sheet.getRow(index++), headers, clazz);
                            readCount++;
                            return nextRow;
                        } catch (Exception e) {
                            printErrorLog(sheet, index - 1, clazz, e);
                            throw Throwables.propagate(e);
                        } finally {
                            if (!hasNext()) {
                                logger.info("读取的数据行数(不含表头): readCount={}", readCount);
                            }
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("不支持删除操作");
                    }
                };
            }
        };
    }

    private static <T> Iterable<T> readSheet(final StreamingSheet sheet, FileLineCell[] fileLineCells,
            final Class<T> clazz, boolean requireMatchedHeaders) {
        final Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) {
            return Collections.EMPTY_LIST;
        }

        Row firstRow = rowIterator.next();
        final Map<Integer, FileLineCell> headers = getHeaders((StreamingRow) firstRow, fileLineCells); // streaming row
        checkHeaders(headers, fileLineCells, requireMatchedHeaders);

        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    int readCount = 0; // 记录读取的行数

                    public boolean hasNext() {
                        return rowIterator.hasNext();
                    }

                    public T next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException("当前excel sheet已读完");
                        }
                        try {
                            T nextRow = getRow((StreamingRow) rowIterator.next(), headers, clazz); // streaming row
                            readCount++;
                            return nextRow;
                        } catch (Exception e) {
                            printErrorLog(sheet, readCount + 1, clazz, e); // +1是因为header是一行
                            throw Throwables.propagate(e);
                        } finally {
                            if (!hasNext()) {
                                logger.info("读取的数据行数(不含表头): readCount={}", readCount);
                            }
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("不支持删除操作");
                    }
                };
            }
        };
    }

    /*
     * 校验读取的表头是否和excelCells中定义的names一致
     */
    private static void checkHeaders(Map<Integer, FileLineCell> headers, FileLineCell[] fileLineCells,
            boolean requireMatchedHeaders) {
        if (requireMatchedHeaders && headers.size() != fileLineCells.length) {
            throw new BusinessException("excel表头与给定的表头不匹配");
        }
    }

    private static Map<Integer, FileLineCell> getHeaders(Row row, FileLineCell[] fileLineCells) {
        int firstCell = row.getFirstCellNum();
        int lastCell = row.getLastCellNum();
        Map<Integer, FileLineCell> result = Maps.newHashMapWithExpectedSize(fileLineCells.length);
        for (int i = firstCell; i < lastCell; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String content = cell.getStringCellValue();
                if (StringUtils.isNotBlank(content)) {
                    content = content.trim();
                    for (FileLineCell fileLineCell : fileLineCells) {
                        if (fileLineCell.getName().equals(content)) {
                            result.put(i, fileLineCell);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /*
     * get headers from streaming row
     */
    private static Map<Integer, FileLineCell> getHeaders(StreamingRow row, FileLineCell[] fileLineCells) {
        Map<Integer, FileLineCell> result = Maps.newHashMapWithExpectedSize(fileLineCells.length);
        for (Cell cell : row) {
            if (cell != null) {
                String content = cell.getStringCellValue();
                if (StringUtils.isNotBlank(content)) {
                    content = content.trim();
                    for (FileLineCell fileLineCell : fileLineCells) {
                        if (fileLineCell.getName().equals(content)) {
                            int columnIndex = cell.getColumnIndex(); // get column index from cell
                            result.put(columnIndex, fileLineCell);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static <T> T getRow(Row row, Map<Integer, FileLineCell> headers, Class<T> clazz) throws Exception {
        if (row == null) {
            return null;
        }
        int firstCell = row.getFirstCellNum();
        int lastCell = row.getLastCellNum();
        if (firstCell == lastCell && firstCell == 0) {
            return null;
        }
        T t = clazz.newInstance();
        Cell cell = null;
        Field field = null;
        FileLineCell fileLineCell = null;

        try {

            for (int i = firstCell; i < lastCell; i++) {
                fileLineCell = headers.get(i);
                if (fileLineCell == null) {
                    continue;
                }
                cell = row.getCell(i);
                if (cell == null) {
                    continue;
                }
                // field = clazz.getDeclaredField(fileLineCell.getClassFieldName());
                field = getField(clazz, fileLineCell.getClassFieldName());
                field.setAccessible(true);
                field.set(t, getCellValue(cell, fileLineCell)); // set value
            }
        } catch (Exception e) {
            printErrorLog(cell, field, fileLineCell, e);
            throw new FileParseException(e, cell.getRowIndex(), cell.getColumnIndex(), fileLineCell); // TO.DO. cell &
                                                                                                      // fileLineCell
                                                                                                      // may be null
        }
        return t;
    }

    /*
     * get row object from streaming row
     */
    private static <T> T getRow(StreamingRow row, Map<Integer, FileLineCell> headers, Class<T> clazz) throws Exception {
        if (row == null) {
            return null;
        }
        Iterator<Cell> cellIterator = row.iterator();
        if (!cellIterator.hasNext()) {
            return null;
        }
        T t = clazz.newInstance();
        Cell cell = null;
        Field field = null;
        FileLineCell fileLineCell = null;

        try {
            for (Cell c : row) {
                if ((cell = c) == null) {
                    continue;
                }
                fileLineCell = headers.get(cell.getColumnIndex());
                if (fileLineCell == null) {
                    continue;
                }
                // field = clazz.getDeclaredField(fileLineCell.getClassFieldName());
                field = getField(clazz, fileLineCell.getClassFieldName());
                field.setAccessible(true);
                field.set(t, getCellValue(cell, fileLineCell)); // set value, may trigger some exception if cell type is
                                                                // FORMULA
            }
        } catch (Exception e) {
            printErrorLog(cell, field, fileLineCell, e);
            throw new FileParseException(e, cell.getRowIndex(), cell.getColumnIndex(), fileLineCell); // TO.DO. cell &
                                                                                                      // fileLineCell
                                                                                                      // may be null
        }
        return t;
    }

    /*
     * 获取各种类型的cell内容
     */
    private static Object getCellValue(Cell cell, FileLineCell fileLineCell) {
        Object cellValue = null;

        boolean isValidType = true;
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_ERROR:
            isValidType = false;
            break;
        case Cell.CELL_TYPE_BLANK:
            isValidType = false;
            break;
        case Cell.CELL_TYPE_STRING:
            String stringCellVaule = cell.getStringCellValue();
            cellValue = (stringCellVaule == null ? null : stringCellVaule.trim()); // 字符串类型非null则trim一下
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                cellValue = cell.getDateCellValue();
            } else {
                cellValue = cell.getNumericCellValue();
            }
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            cellValue = cell.getBooleanCellValue();
            break;
        case Cell.CELL_TYPE_FORMULA:
            cellValue = cell.getCellFormula();
            break;
        default:
            throw new RuntimeException("错误的格式");
        }

        PropertyParser propertyParser;
        if (isValidType && (propertyParser = fileLineCell.getPropertyParser()) != null) {
            cellValue = propertyParser.parse(cellValue);
        }
        return cellValue;
    }

    /*
     * 行级别的日志
     */
    private static void printErrorLog(Sheet sheet, int row, Class<?> clazz, Throwable throwable) {
        logger.error("sheet:{} row:{} class:{} {}", sheet.getSheetName(), row, clazz, throwable);
    }

    private static void printErrorLog(Cell cell, Field field, FileLineCell fileLineCell, Throwable throwable) {
        Object value = null;
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_ERROR:
            break;
        case Cell.CELL_TYPE_BLANK:
            break;
        case Cell.CELL_TYPE_STRING:
            value = cell.getStringCellValue();
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                value = cell.getDateCellValue();
            } else {
                value = cell.getNumericCellValue();
            }
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            value = cell.getBooleanCellValue();
            break;
        case Cell.CELL_TYPE_FORMULA:
            value = cell.getCellFormula();
            break;
        default:
            value = "未知的格式";
        }
        logger.error("value:{} type:{} field:{} {} ", value, cell.getCellType(), field, fileLineCell, throwable);
    }

    private static void validateType(File file) {
        String suffix = Files.getSuffix(file);
        FileType type = FileType.of(suffix);
        if (type != FileType.excel2003 && type != FileType.excel2007) {
            throw new IllegalArgumentException("不是excel 2003 或者 2007");
        }
    }

    public static Field getField(Class<?> clz, String name) throws NoSuchFieldException {
        Field fieldResult = null;
        while (clz != null && clz != Object.class) {
            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    fieldResult = field;
                    break;
                }
            }
            if (fieldResult != null) {
                break;
            }
            clz = clz.getSuperclass();
        }
        if (fieldResult == null) {
            throw new NoSuchFieldException(name);
        }
        return fieldResult;
    }

}
