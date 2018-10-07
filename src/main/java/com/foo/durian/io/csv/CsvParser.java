package com.foo.durian.io.csv;

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.foo.durian.exception.FileParseException;
import com.foo.durian.io.FileLineCell;
import com.foo.durian.io.Files;
import com.foo.durian.io.excel.FileType;
import com.foo.durian.io.excel.LineProcessor;
import com.foo.durian.io.excel.parser.TypeParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;

/**
 * version 1.0.0 Created by foolish on 16/9/14 下午3:54.
 */
public class CsvParser {

    private static final Logger logger = LoggerFactory.getLogger(CSVParser.class);

    // 单个sheet
    public static <T> List<T> parse(File file, FileLineCell[] fileLineCells, Class<T> clazz) throws Exception {
        return parse(file, fileLineCells, clazz, null, null);
    }

    public static <T> List<T> parse(File file, FileLineCell[] fileLineCells, Class<T> clazz, CSVFormat format)
            throws Exception {
        return parse(file, fileLineCells, clazz, null, format);
    }

    public static <T> List<T> parse(File file, FileLineCell[] fileLineCells, Class<T> clazz, LineProcessor<T> processor)
            throws Exception {
        return parse(file, fileLineCells, clazz, processor, null);
    }

    public static <T> List<T> parse(File file, FileLineCell[] fileLineCells, Class<T> clazz, LineProcessor<T> processor,
            CSVFormat format) throws Exception {
        validateType(file);
        Closer closer = Closer.create();
        try {

            Reader reader = new BufferedReader(new FileReader(file));
            closer.register(reader);
            return parse(reader, fileLineCells, clazz, processor, format);
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
    @SuppressWarnings("all")
    public static <T> List<T> parse(Reader reader, FileLineCell[] fileLineCells, Class<T> clazz,
            LineProcessor<T> processor, CSVFormat format) throws Exception {

        List<T> result = Lists.newArrayList();

        // 支持自定义格式
        format = (format == null ? CSVFormat.EXCEL.withHeader() : format);
        final CSVParser parser = new CSVParser(reader, format);
        try {
            for (final CSVRecord record : parser) {
                T t = getLine(record, fileLineCells, clazz);
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
        } finally {
            parser.close();
        }

        return result;
    }

    private static <T> T getLine(CSVRecord record, FileLineCell[] headers, Class<T> clazz) {
        String name = null;
        String value = null;
        Field field = null;

        FileLineCell cell = null;
        try {
            T t = clazz.newInstance();
            for (FileLineCell header : headers) {
                cell = header;

                name = header.getName();
                value = record.get(name);
                field = clazz.getDeclaredField(header.getClassFieldName());
                field.setAccessible(true);
                if (header.getPropertyParser() != null) {
                    field.set(t, header.getPropertyParser().parse(value));
                } else {
                    field.set(t, TypeParser.parse(value, field.getType()));
                }
            }
            return t;
        } catch (Exception e) {
            printErrorLog(name, value, field, record, e);
            throw new FileParseException(e, record.getRecordNumber(), -1, cell);
        }
    }

    private static void printErrorLog(String name, String value, Field field, CSVRecord record, Exception throwable) {
        logger.error("name:{} value:{} field:{} line: {}", name, value, field, record.toString(), throwable);
    }

    private static void validateType(File file) {
        String suffix = Files.getSuffix(file);
        FileType type = FileType.of(suffix);
        if (type != FileType.csv) {
            throw new IllegalArgumentException("csv");
        }
    }

}
