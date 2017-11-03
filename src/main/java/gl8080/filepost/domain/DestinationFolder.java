package gl8080.filepost.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public List<File> collectAllImages() throws IOException {
        return Files.list(this.destDir.toPath())
                .filter(this::isImageFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    public List<File> collectImages(Predicate<Path> filter) throws IOException {
        return Files.list(this.destDir.toPath())
                .filter(this::isImageFile)
                .filter(filter)
                .map(Path::toFile)
                .collect(Collectors.toList());
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

    private static final List<String> IMAGE_FILE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

    private boolean isImageFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }

        String name = path.getFileName().toString().toLowerCase();
        return IMAGE_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
    
    void moveInto(File file) {
        try {
            String fileName = file.getName();
            File dest = new File(this.destDir, fileName);
            Files.move(file.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    boolean doesNotHaveImageFiles() {
        try {
            return Files.list(this.destDir.toPath()).filter(this::isImageFile).count() == 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
