package gl8080.filepost.view;

import gl8080.filepost.infrastructure.similar.IndexCreator;
import gl8080.filepost.infrastructure.similar.NoIndexedImages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

import java.util.concurrent.Executors;

public class IndexingController {
    
    private NoIndexedImages noIndexedImages;
    @FXML
    private ProgressBar progressBar;
    
    private Runnable finishedListener;

    void setNoIndexedImages(NoIndexedImages noIndexedImages) {
        this.noIndexedImages = noIndexedImages;
    }

    void onFinished(Runnable finishedListener) {
        this.finishedListener = finishedListener;
    }
    
    void start() {
        IndexCreator indexCreator = new IndexCreator(this::updateProgressBar);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            indexCreator.createIndex(this.noIndexedImages);
            this.finishedListener.run();
        });
    }
    
    private void updateProgressBar(double progress) {
        Platform.runLater(() -> this.progressBar.setProgress(progress));
    }
}
