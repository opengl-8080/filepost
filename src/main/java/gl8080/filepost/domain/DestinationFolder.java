package gl8080.filepost.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
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

    public List<File> collectAllImages(Predicate<Path> filter) throws IOException {
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

    public int moveInto(LinkedHashSet<File> files, BiFunction<File, List<File>, Optional<SimilarImageStrategy>> similarImageListener) {
        AtomicInteger movedCount = new AtomicInteger(0);
        
        files.forEach(src -> {
            try {
                SimilarImageStrategy strategy = this.decideStrategy(src, similarImageListener);
                
                if (strategy == SimilarImageStrategy.SKIP) {
                    return;
                } else if (strategy == SimilarImageStrategy.REMOVE) {
                    Files.delete(src.toPath());
                    return;
                }
                
                File dest = new File(this.destDir, src.getName());
                Files.move(src.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);
                movedCount.incrementAndGet();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return movedCount.get();
    }

    private SimilarImageStrategy decideStrategy(File src, BiFunction<File, List<File>, Optional<SimilarImageStrategy>> similarImageListener) throws IOException {
        if (this.doesNotHaveImageFiles()) {
            return SimilarImageStrategy.MOVE;
        }

        List<File> similarImages = new SimilarImageFinder().findSimilarImages(src, this.name);

        if (!similarImages.isEmpty()) {
            return similarImageListener.apply(src, similarImages).orElse(SimilarImageStrategy.SKIP);
        }
        
        return SimilarImageStrategy.MOVE;
    }
    
    private boolean doesNotHaveImageFiles() throws IOException {
        return Files.list(this.destDir.toPath()).filter(this::isImageFile).count() == 0;
    }
}
