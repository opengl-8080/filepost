package gl8080.filepost.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MoveTargetImages {
    private Set<Path> files = new HashSet<>();

    public boolean isNotEmpty() {
        return !this.files.isEmpty();
    }

    public void add(List<File> files) {
        this.files.addAll(files.stream().map(File::toPath).collect(Collectors.toSet()));
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
            for (Path movingTargetImage : this.files) {
                MovingImageStrategy strategy = strategyDecider.decide(movingTargetImage.toFile());
                
                if (strategy == MovingImageStrategy.MOVE) {
                    destinationFolder.moveInto(movingTargetImage);
                    movedCount++;
                } else if (strategy == MovingImageStrategy.REMOVE) {
                    Files.delete(movingTargetImage);
                }
                // skip
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        
        return movedCount;
    }
}
