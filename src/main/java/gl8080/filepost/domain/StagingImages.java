package gl8080.filepost.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StagingImages {
    private Set<File> files = new HashSet<>();

    public boolean isNotEmpty() {
        return !this.files.isEmpty();
    }

    public void add(List<File> files) {
        this.files.addAll(files);
    }
    
    public int size() {
        return this.files.size();
    }

    public void clear() {
        this.files = new HashSet<>();
    }

    public int moveTo(DestinationFolder destinationFolder, MovingImageStrategyDecider strategyDecider) {
        int movedCount = 0;

        try {
            for (File movingTargetImage : this.files) {
                MovingImageStrategy strategy = strategyDecider.decide(destinationFolder, movingTargetImage);
                
                if (strategy == MovingImageStrategy.MOVE) {
                    destinationFolder.moveInto(movingTargetImage);
                    movedCount++;
                } else if (strategy == MovingImageStrategy.REMOVE) {
                    Files.delete(movingTargetImage.toPath());
                }
                // skip
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        
        return movedCount;
    }
}
