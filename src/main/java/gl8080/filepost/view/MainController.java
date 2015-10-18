package gl8080.filepost.view;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import gl8080.filepost.domain.DestinationFolder;
import gl8080.filepost.domain.DestinationFolderRepository;
import gl8080.filepost.infrastructure.DestinationFolderRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class MainController implements Initializable {
    @FXML
    private TextField filter;
    
    @FXML
    private ListView<String> destDirectories;

    private List<DestinationFolder> destinationFolders;
    
    private DestinationFolderRepository repository = new DestinationFolderRepositoryImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.destinationFolders = repository.findAll();
        
        ObservableList<String> list = FXCollections.observableArrayList();
        
        this.destinationFolders.forEach(f -> {
            list.add(f.getName() + "\n" + f.getDestPath());
        });
        
        this.destDirectories.setItems(list.sorted());
        
        this.destDirectories.setOnMouseClicked((e) -> {
            if (this.isDoubleClick(e)) {
                String item = destDirectories.getSelectionModel().getSelectedItem();
            }
        });
    }
    
    @FXML
    public void onChangeFilter() {
        ObservableList<String> list = FXCollections.observableArrayList();
        
        this.destinationFolders.stream().filter(f -> f.getName().contains(this.filter.getText())).forEach(f -> {
            list.add(f.getName() + "\n" + f.getDestPath());
        });
        
        this.destDirectories.setItems(list.sorted());
    }
    
    private boolean isDoubleClick(MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2;
    }
    
    @FXML
    public void onDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        if (dragboard.hasFiles()) {
            boolean allDirectory = dragboard.getFiles().stream().allMatch(File::isDirectory);
            
            if (allDirectory) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        
        event.consume();
    }
    
    @FXML
    public void onDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        dragboard.getFiles().forEach(f -> System.out.println(f));
        
        event.setDropCompleted(true);
        event.consume();
    }
}
