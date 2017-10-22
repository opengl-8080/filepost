package gl8080.filepost.view;

import gl8080.filepost.domain.DuplicationStrategy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

public class DuplicationController {
    
    private Stage stage;
    
    private File file;
    private List<File> duplications;
    private int duplicationImageIndex = 0;
    private DuplicationStrategy duplicationStrategy;
    
    @FXML
    private ImageView srcImageView;
    @FXML
    private Label srcImageFilePath;
    @FXML
    private Label srcImageSize;
    @FXML
    private Label srcImageFileSize;
    @FXML
    private ImageView duplicationImageView;
    @FXML
    private Label duplicationImageFilePath;
    @FXML
    private Label duplicationImageSize;
    @FXML
    private Label duplicationImageFileSize;
    @FXML
    private Button nextDuplicationButton;
    @FXML
    private Button prevDuplicationButton;
    
    void init(Stage stage, File file, List<File> duplications) {
        this.stage = stage;
        this.file = file;
        this.duplications = duplications;
        
        this.setImage(this.file, this.srcImageView, this.srcImageFilePath, this.srcImageSize, this.srcImageFileSize);
        this.setDuplicationImage();
    }
    
    private void setImage(File imageFile, ImageView imageView, Label filePathLabel, Label imageSizeLabel, Label fileSizeLabel) {
        Image image = new Image(String.valueOf(imageFile.toURI()));
        imageView.setImage(image);

        filePathLabel.setText(imageFile.toString());

        int width = (int)image.getWidth();
        int height = (int)image.getHeight();
        imageSizeLabel.setText(width + " x " + height);

        if (1024 < width) {
            imageView.setFitWidth(1024);
        }
        if (1024 < height) {
            imageView.setFitHeight(1024);
        }

        DecimalFormat format = new DecimalFormat("#,###");
        long fileSize = imageFile.length() / 1024L;
        fileSizeLabel.setText(format.format(fileSize) + "KB");
    }
    
    @FXML
    public void onClickPrevDuplicationButton() {
        this.duplicationImageIndex--;
        this.setDuplicationImage();
    }
    
    @FXML
    public void onClickNextDuplicationButton() {
        this.duplicationImageIndex++;
        this.setDuplicationImage();
    }
    
    private void setDuplicationImage() {
        this.setImage(this.duplications.get(this.duplicationImageIndex), this.duplicationImageView, this.duplicationImageFilePath, this.duplicationImageSize, this.duplicationImageFileSize);
        this.controlButtonDisable();
    }
    
    private void controlButtonDisable() {
        this.prevDuplicationButton.setDisable(this.duplicationImageIndex <= 0);
        this.nextDuplicationButton.setDisable(this.duplications.size() - 1 <= this.duplicationImageIndex);
    }
    
    @FXML
    public void onClickMoveButton() {
        this.duplicationStrategy = DuplicationStrategy.MOVE;
        this.stage.close();
    }
    
    @FXML
    public void onClickSkipButton() {
        this.duplicationStrategy = DuplicationStrategy.SKIP;
        this.stage.close();
    }
    
    Optional<DuplicationStrategy> getDuplicationStrategy() {
        return Optional.ofNullable(this.duplicationStrategy);
    }
}
