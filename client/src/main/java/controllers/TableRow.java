package controllers;

public class TableRow {

    private String fileName;

    private String isDir;

    private String size;

    private String dateCreate;

    private String dateModify;

    public TableRow(String fileName, String isDir, String size, String dateCreate, String dateModify) {
        this.fileName = fileName;
        this.isDir = isDir;
        this.size = size;
        this.dateCreate = dateCreate;
        this.dateModify = dateModify;
    }

    public String getFileName() {
        return fileName;
    }

    public String getIsDir() {
        return isDir;
    }

    public String getSize() {
        return size;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public String getDateModify() {
        return dateModify;
    }
}
