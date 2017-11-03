package gl8080.filepost.domain;

import java.io.File;
import java.util.List;

public class MovingImageStrategyDecider {
    private final SimilarImageFinder similarImageFinder;
    private final SimilarImageMovingStrategyDecider similarImageMovingStrategyDecider;

    public MovingImageStrategyDecider(SimilarImageFinder similarImageFinder, SimilarImageMovingStrategyDecider similarImageMovingStrategyDecider) {
        this.similarImageFinder = similarImageFinder;
        this.similarImageMovingStrategyDecider = similarImageMovingStrategyDecider;
    }

    MovingImageStrategy decide(DestinationFolder destinationFolder, File movingTargetImage) {
        if (destinationFolder.doesNotHaveImageFiles()) {
            return MovingImageStrategy.MOVE;
        }

        List<File> similarImages = this.similarImageFinder.findSimilarImages(destinationFolder, movingTargetImage);

        if (similarImages.isEmpty()) {
            return MovingImageStrategy.MOVE;
        }

        return this.similarImageMovingStrategyDecider.decide(movingTargetImage, similarImages);
    }
}
