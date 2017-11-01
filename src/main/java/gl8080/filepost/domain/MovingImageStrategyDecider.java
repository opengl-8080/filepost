package gl8080.filepost.domain;

import java.io.File;
import java.util.List;

public interface MovingImageStrategyDecider {
    
    MovingImageStrategy decideStrategy(File movingTargetImage, List<File> similarImages);
}
