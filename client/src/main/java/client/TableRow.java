
package client;

public class TableRow
{
    private String fileName;
    private String isDir;
    private String size;
    private String dateCreate;
    private String dateModify;
    
    public TableRow(final String fileName, final String isDir, final String size, final String dateCreate, final String dateModify) {
        this.fileName = fileName;
        this.isDir = isDir;
        this.size = size;
        this.dateCreate = dateCreate;
        this.dateModify = dateModify;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public String getIsDir() {
        return this.isDir;
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
