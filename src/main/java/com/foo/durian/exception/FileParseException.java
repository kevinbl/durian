package com.foo.durian.exception;

import com.foo.durian.io.FileLineCell;

/**
 * 文件解析异常; 通常是针对使用{@link FileLineCell}的分行分列的解析
 *
 * @author f
 * @version v1.0.0
 * @since 17/5/27
 */
public class FileParseException extends BusinessException {

    private long rowNum;

    private long colNum;

    private FileLineCell fileLineCell;

    private Object content;

    public FileParseException(Throwable cause, long rowNum, long colNum, FileLineCell fileLineCell) {
        super(cause);
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.fileLineCell = fileLineCell;
    }

    public FileParseException(Throwable cause, long rowNum, long colNum, FileLineCell fileLineCell, Object content) {
        super(cause);
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.fileLineCell = fileLineCell;
        this.content = content;
    }

    /**
     * 返回详细的错误信息: 行号, 列号, 字段名, 字段值
     *
     * @return 详细的错误信息
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("文件解析失败");
        sb.append("[");
        sb.append("行号=").append(rowNum).append(", 列号=").append(colNum);
        sb.append("]");
        sb.append(fileLineCell == null ? ""
                : "(字段名=" + fileLineCell.getName() + (content == null ? "" : ", 字段值=" + content) + ")");
        return sb.toString();
    }
}
