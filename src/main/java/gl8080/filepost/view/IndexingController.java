package gl8080.filepost.view;

import gl8080.filepost.domain.IndexCreator;
import gl8080.filepost.domain.NoIndexedImages;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

import java.util.concurrent.Executors;

public class IndexingController {
    
    private NoIndexedImages noIndexedImages;
    @FXML
    private ProgressBar progressBar;
    
    private Runnable listener;

    void setNoIndexedImages(NoIndexedImages noIndexedImages) {
        this.noIndexedImages = noIndexedImages;
    }

    void start() {
        IndexCreator indexCreator = new IndexCreator(progress -> {
            Platform.runLater(() -> {
                this.progressBar.setProgress(progress);
            });
        });
        
        Executors.newSingleThreadExecutor().execute(() -> {
            indexCreator.createIndex(this.noIndexedImages);
            this.listener.run();
        });
    }
    
    void onFinished(Runnable listener) {
        this.listener = listener;
    }
}
