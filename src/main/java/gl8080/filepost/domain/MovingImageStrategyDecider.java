package gl8080.filepost.domain;

import java.io.File;
import java.util.List;

public class MovingImageStrategyDecider {
    private final DestinationFolder destinationFolder;
    private final SimilarImageFinder similarImageFinder;
    private final SimilarImageMovingStrategyDecider similarImageMovingStrategyDecider;

    public MovingImageStrategyDecider(DestinationFolder destinationFolder, SimilarImageFinder similarImageFinder, SimilarImageMovingStrategyDecider similarImageMovingStrategyDecider) {
        this.destinationFolder = destinationFolder;
        this.similarImageFinder = similarImageFinder;
        this.similarImageMovingStrategyDecider = similarImageMovingStrategyDecider;
    }

    MovingImageStrategy decide(File movingTargetImage) {
        if (this.destinationFolder.doesNotHaveImageFiles()) {
            return MovingImageStrategy.MOVE;
        }

        List<File> similarImages = this.similarImageFinder.findSimilarImages(movingTargetImage);

        if (!similarImages.isEmpty()) {
            return this.similarImageMovingStrategyDecider.decide(movingTargetImage, similarImages);
        }

        return MovingImageStrategy.MOVE;
    }
}
