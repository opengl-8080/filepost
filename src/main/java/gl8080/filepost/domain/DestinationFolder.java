package gl8080.filepost.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 保存先フォルダ
 */
public class DestinationFolder {
    
    private File destDir;
    private String name;
    
    public DestinationFolder(File destDir, String name) {
        this.destDir = destDir;
        this.name = name;
    }
    
    public DestinationFolder(File destDir) {
        this(destDir, destDir.getName());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDestPath() {
        return this.destDir.getAbsolutePath();
    }

    public void moveInto(List<File> files) {
        files.forEach(src -> {
            try {
                File dest = new File(this.destDir, src.getName());
                Files.move(src.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
