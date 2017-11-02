package gl8080.filepost.domain;

import java.io.File;
import java.util.List;

@FunctionalInterface
public interface SimilarImageMovingStrategyDecider {
    
    MovingImageStrategy decide(File movingTargetImage, List<File> similarImages);
}
