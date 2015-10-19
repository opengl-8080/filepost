package gl8080.filepost.domain;

public class DuplicateDirectoryException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private DestinationFolder folder;
    
    public DuplicateDirectoryException(DestinationFolder folder) {
        super("フォルダが既に登録されています > " + folder.getDestPath());
        this.folder = folder;
    }
    
    public DestinationFolder getFolder() {
        return this.folder;
    }
}
