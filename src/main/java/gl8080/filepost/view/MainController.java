package gl8080.filepost.view;

import gl8080.filepost.domain.DestinationFolder;
import gl8080.filepost.domain.DestinationFolderRepository;
import gl8080.filepost.domain.SimilarImageStrategy;
import gl8080.filepost.infrastructure.DestinationFolderRepositoryImpl;
import gl8080.filepost.infrastructure.similar.IndexedSimilarImageFinder;
import gl8080.filepost.infrastructure.similar.NoIndexedImages;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class MainController implements Initializable {
    
    @FXML
    private Label targetFilesLabel;
    @FXML
    private Button clearButton;
    @FXML
    private TextField filterTextField;
    @FXML
    private ListView<ListItem> destDirectoryListView;
    
    private LinkedHashSet<File> targetFiles;
    private List<DestinationFolder> destinationFolders;
    
    private DestinationFolderRepository repository = new DestinationFolderRepositoryImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.destinationFolders = repository.findAll();
        this.refreshDestFolderListView();
        this.clear();
    }
    
    @FXML
    public void selectDestFolderByMouse(MouseEvent e) {
        if (!this.targetFiles.isEmpty() && this.isDoubleClick(e)) {
            this.selectDestFolder();
        }
    }
    
    @FXML
    public void selectDestFolderByKeyboad(KeyEvent e) {
        if (!this.targetFiles.isEmpty() && (" ".equals(e.getCharacter()) || "\r".equals(e.getCharacter()))) {
            this.selectDestFolder();
        }
    }
    
    private void selectDestFolder() {
        ListItem item = destDirectoryListView.getSelectionModel().getSelectedItem();
        
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("ファイル移動");
        confirm.setHeaderText(item.folder.getDestPath() + " に移動します。");
        confirm.setOnCloseRequest(event -> {
            if (confirm.getResult() != ButtonType.OK) {
                return;
            }

            DestinationFolder destinationFolder = item.folder;

            try {
                IndexedSimilarImageFinder finder = new IndexedSimilarImageFinder(destinationFolder);
                
                NoIndexedImages noIndexedImages = finder.findNoIndexedImages();
                if (noIndexedImages.isNotEmpty()) {
                    this.showIndexingDialog(noIndexedImages);
                }

                int movedCount = destinationFolder.moveInto(this.targetFiles, this::openDuplicationWindow);
                
                this.showCompletedDialog(movedCount, destinationFolder);
                
                this.clear();
            } catch (UncheckedIOException | IOException e) {
                this.showFailedMessageToSaveImage(e);
            }
        });
        confirm.show();
    }
    
    private void showCompletedDialog(int movedCount, DestinationFolder destinationFolder) {
        Alert complete = new Alert(AlertType.INFORMATION);
        complete.setTitle("移動完了");
        complete.setHeaderText(movedCount + " 件のファイルを移動しました。");
        complete.setContentText("移動先：" + destinationFolder.getDestPath());
        complete.show();
    }
    
    private void showIndexingDialog(NoIndexedImages noIndexedImages) throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/indexing-dialog.fxml"));
        Parent root = loader.load();

        IndexingController controller = loader.getController();
        controller.setNoIndexedImages(noIndexedImages);

        Scene scene = new Scene(root);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("インデックス作成");
        stage.setResizable(false);
        stage.setOnShown(event -> controller.start());
        controller.onFinished(() -> Platform.runLater(stage::close));

        stage.showAndWait();
    }
    
    private Optional<SimilarImageStrategy> openDuplicationWindow(File srcFile, List<File> duplications) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/duplication.fxml"));
            Parent root = loader.load();
            
            SimilarImageController controller = loader.getController();
            
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("重複ファイル");
            stage.setMaximized(true);
            
            controller.init(stage, srcFile, duplications);

            stage.showAndWait();
            
            return controller.getSimilarImageStrategy();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void showFailedMessageToSaveImage(Exception ex) {
        Alert error = new Alert(AlertType.ERROR);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        TextArea textArea = new TextArea(sw.toString());

        error.getDialogPane().setExpandableContent(textArea);
        error.setContentText("ファイル保存中にエラーが発生しました。");
        error.show();
    }
    
    @FXML
    public void refreshDestFolderListView() {
        ObservableList<ListItem> list = FXCollections.observableArrayList();

        String filterText = this.filterTextField.getText().toLowerCase();
        String[] filterTexts = filterText.split("[ 　]");

        this.destinationFolders
            .stream()
            .filter(f -> Stream.of(filterTexts).allMatch(text -> f.getName().toLowerCase().contains(text)))
            .forEach(f -> list.add(new ListItem(f)));
        
        this.destDirectoryListView.setItems(list.sorted());
    }
    
    private boolean isDoubleClick(MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2;
    }
    
    @FXML
    public void onDragOverFile(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        if (dragboard.hasFiles()) {
            boolean allFile = dragboard.getFiles().stream().allMatch(File::isFile);
            
            if (allFile) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        
        event.consume();
    }
    
    @FXML
    public void onDragDroppedFile(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        if (this.targetFiles.isEmpty()) {
            this.targetFiles = new LinkedHashSet<>(dragboard.getFiles());
        } else {
            this.targetFiles.addAll(dragboard.getFiles());
        }
        
        this.targetFilesLabel.setText(this.targetFiles.size() + " 件選択されています");
        this.filterTextField.setDisable(false);
        this.filterTextField.requestFocus();
        
        event.setDropCompleted(true);
        event.consume();
    }
    
    @FXML
    public void clear() {
        this.targetFiles = new LinkedHashSet<>();
        this.targetFilesLabel.setText("ここにファイルをドロップ");
        this.filterTextField.setDisable(true);
    }
    
    @FXML
    public void onDragOverFolder(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        if (dragboard.hasFiles()
                && dragboard.getFiles().size() == 1
                && dragboard.getFiles().get(0).isDirectory()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        
        event.consume();
    }
    
    @FXML
    public void onDragDroppedFolder(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        File folder = dragboard.getFiles().get(0);
    
        DestinationFolder dest = new DestinationFolder(folder);
        
        if (this.repository.existsSameFolder(dest)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("エラー");
            alert.setHeaderText("既にフォルダが登録されています。");
            alert.getDialogPane().setContentText(dest.getDestPath());
            alert.show();
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("フォルダ追加");
            dialog.setContentText("フォルダ名");
            dialog.setHeaderText(dest.getDestPath());
            dialog.getEditor().setText(dest.getName());
            
            dialog.setOnCloseRequest(e -> {
                TextInputDialog d = (TextInputDialog)e.getTarget();
                String result = d.getResult();
                
                if (result != null) {
                    if (result.isEmpty()) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("エラー");
                        alert.setHeaderText("フォルダ名に空は指定できません。");
                        alert.show();
                    } else {
                        dest.setName(result);
                        this.repository.registerFolder(dest);
                        this.destinationFolders = repository.findAll();
                        this.refreshDestFolderListView();
                    }
                }
            });
            
            dialog.show();
        }
        
        event.setDropCompleted(true);
        event.consume();
    }
    
    public static class ListItem {
        private final DestinationFolder folder;
        
        private ListItem(DestinationFolder folder) {
            this.folder = folder;
        }

        @Override
        public String toString() {
            return this.folder.getName() + "\n  > " + this.folder.getDestPath();
        }
    }
}
