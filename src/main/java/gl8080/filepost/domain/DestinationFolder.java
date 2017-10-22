package gl8080.filepost.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

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

    public int moveInto(List<File> files, BiFunction<File, List<File>, Optional<DuplicationStrategy>> duplicationListener) {
        AtomicInteger movedCount = new AtomicInteger(0);
        files.forEach(src -> {
            try {
                DuplicationStrategy strategy = this.decideStrategy(src, duplicationListener);
                
                if (strategy == DuplicationStrategy.SKIP) {
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
    
    private List<File> findDuplications() {
        List<File> duplications = new ArrayList<>();
        File[] files1 = this.destDir.listFiles();
        if (files1 != null) {
            for (int i=0; i<files1.length && i<3; i++) {
                duplications.add(files1[i]);
            }
        }
        return duplications;
    }

    private DuplicationStrategy decideStrategy(File src, BiFunction<File, List<File>, Optional<DuplicationStrategy>> duplicationListener) {
        List<File> duplications = this.findDuplications();

        if (!duplications.isEmpty()) {
            return duplicationListener.apply(src, duplications).orElse(DuplicationStrategy.SKIP);
        }
        
        return DuplicationStrategy.MOVE;
    }
}
