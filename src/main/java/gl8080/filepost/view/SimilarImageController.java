package gl8080.filepost.view;

import gl8080.filepost.domain.SimilarImageStrategy;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

public class SimilarImageController {
    
    private Stage stage;
    
    private List<File> similarImages;
    private int similarImagesIndex = 0;
    private SimilarImageStrategy similarImageStrategy;
    
    @FXML
    private ImageView srcImageView;
    @FXML
    private Label srcImageFilePath;
    @FXML
    private Label srcImageSize;
    @FXML
    private Label srcImageFileSize;
    @FXML
    private ImageView similarImageView;
    @FXML
    private Label similarImageFilePath;
    @FXML
    private Label similarImageSize;
    @FXML
    private Label similarImageFileSize;
    @FXML
    private Button nextImageButton;
    @FXML
    private Button prevImageButton;
    
    void init(Stage stage, File file, List<File> similarImages) {
        this.stage = stage;
        this.similarImages = similarImages;
        
        this.setImage(file, this.srcImageView, this.srcImageFilePath, this.srcImageSize, this.srcImageFileSize);
        this.setSimilarImage();
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
    public void onClickPrevImageButton() {
        this.similarImagesIndex--;
        this.setSimilarImage();
    }
    
    @FXML
    public void onClickNextImageButton() {
        this.similarImagesIndex++;
        this.setSimilarImage();
    }
    
    private void setSimilarImage() {
        this.setImage(this.similarImages.get(this.similarImagesIndex), this.similarImageView, this.similarImageFilePath, this.similarImageSize, this.similarImageFileSize);
        this.controlButtonDisable();
    }
    
    private void controlButtonDisable() {
        this.prevImageButton.setDisable(this.similarImagesIndex <= 0);
        this.nextImageButton.setDisable(this.similarImages.size() - 1 <= this.similarImagesIndex);
    }
    
    @FXML
    public void onClickMoveButton() {
        this.similarImageStrategy = SimilarImageStrategy.MOVE;
        this.stage.close();
    }
    
    @FXML
    public void onClickSkipButton() {
        this.similarImageStrategy = SimilarImageStrategy.SKIP;
        this.stage.close();
    }
    
    @FXML
    public void onClickRemoveButton() {
        Alert alert = new Alert(Alert.AlertType.WARNING, "削除します、よろしいですか？", ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle("ファイル削除");
        
        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setDefaultButton(false);
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDefaultButton(true);
        
        ButtonType buttonType = alert.showAndWait().orElse(ButtonType.NO);
        
        if (ButtonType.OK.equals(buttonType)) {
            this.similarImageStrategy = SimilarImageStrategy.REMOVE;
            this.stage.close();
        }
    }
    
    Optional<SimilarImageStrategy> getSimilarImageStrategy() {
        return Optional.ofNullable(this.similarImageStrategy);
    }
}
