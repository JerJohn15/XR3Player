/**
 * 
 */
package application.users;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import application.Main;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tools.ActionTool;
import tools.InfoTool;
import tools.NotificationType;

/**
 * @author GOXR3PLUS
 *
 */
public class User extends StackPane {

    @FXML
    ImageView imageView;

    @FXML
    Label nameField;

    // --------------------------------------------

    /** The logger for this class */
    private static final Logger logger = Logger.getLogger(User.class.getName());

    /**
     * The position of the User into the List
     */
    private int position;
    private String userName;
    private LoginMode loginMode;

    /** This InvalidationListener is used during the rename of a user */
    private final InvalidationListener renameInvalidator = new InvalidationListener() {
	@Override
	public void invalidated(Observable observable) {

	    // Remove the Listener
	    Main.renameWindow.showingProperty().removeListener(this);

	    // !Showing
	    if (!Main.renameWindow.isShowing()) {

		// old && new -> name
		String oldName = getUserName();
		String newName = Main.renameWindow.getUserInput();
		boolean success = false;

		// Remove Bindings
		nameField.textProperty().unbind();

		// !XPressed
		if (Main.renameWindow.wasAccepted()) {

		    // duplicate?
		    if (!Main.loginMode.teamViewer.getItemsObservableList().stream()
			    .anyMatch(user -> user != User.this && user.getUserName().equalsIgnoreCase(newName))
			    || newName.equalsIgnoreCase(oldName)) {

			File originalFolder = new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + oldName);
			File outputFolder = new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + newName);

			//Check if the Folder can be renamed
			if (originalFolder.renameTo(outputFolder)) { //Success
			    success = true;
			    setUserName(nameField.getText());
			    nameField.getTooltip().setText(getUserName());
			} else
			    ActionTool.showNotification("Error", "An error occured trying to rename the user", Duration.seconds(2),
				    NotificationType.ERROR);

		    }//This user already exists
		    else
			ActionTool.showNotification("Dublicate User", "Name->" + newName + " is already used from another User...",
				Duration.millis(2000), NotificationType.INFORMATION);
		}

		//Succeeded?
		if (!success)
		    resetTheName();

	    }  // !Showing
	}

	/**
	 * Resets the name if the user cancels the rename operation
	 */
	private void resetTheName() {
	    nameField.setText(getUserName());
	}
    };

    /**
     * Constructor
     * 
     * @param userName
     * @param position
     * @param loginMode
     */
    public User(String userName, int position, LoginMode loginMode) {
	this.setUserName(userName);
	this.setPosition(position);
	this.loginMode = loginMode;

	// ----------------------------------FXMLLoader-------------------------------------
	FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.FXMLS + "UserController.fxml"));
	loader.setController(this);
	loader.setRoot(this);

	// -------------Load the FXML-------------------------------
	try {
	    loader.load();
	} catch (IOException ex) {
	    logger.log(Level.WARNING, "", ex);
	}
    }

    /**
     * Called as soon as FXML file has been loaded
     */
    @FXML
    private void initialize() {

	// --Key Listener
	setOnKeyReleased(this::onKeyReleased);

	// --Mouse Listener
	setOnMouseEntered(m -> {
	    if (!isFocused())
		requestFocus();
	});

	// Clip
	Rectangle rect = new Rectangle();
	rect.widthProperty().bind(this.widthProperty());
	rect.heightProperty().bind(this.heightProperty());
	rect.setArcWidth(25);
	rect.setArcHeight(25);
	// rect.setEffect(new Reflection());

	// StackPane -> this
	this.setClip(rect);
	// Reflection reflection = new Reflection();
	// reflection.setInput(new DropShadow(4, Color.WHITE));
	// this.setEffect(reflection);

	//imageView
	String absoluteImagePath = getAbsoluteImagePath();
	imageView.setImage(absoluteImagePath == null ? null : new Image(new File(absoluteImagePath).toURI() + ""));

	//Name
	nameField.setText(getUserName());
	nameField.getTooltip().setText(getUserName());
    }

    /**
     * @return The Position of the user inside the list
     */
    public int getPosition() {
	return position;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
	return userName;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
	this.userName = userName;
	if (nameField != null)
	    nameField.setText(userName);
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(int position) {
	this.position = position;
    }

    /**
     * Renames the current User.
     * 
     * @param node
     *            The node based on which the Rename Window will be position
     */
    public void renameUser(Node node) {

	// Open the Window
	Main.renameWindow.show(getUserName(), node,"User Renaming");

	// Bind 
	nameField.textProperty().bind(Main.renameWindow.inputField.textProperty());

	Main.renameWindow.showingProperty().addListener(renameInvalidator);
    }

    /**
     * This method is called when a key is released.
     *
     * @param e
     *            An event which indicates that a keystroke occurred in a javafx.scene.Node.
     */
    public void onKeyReleased(KeyEvent e) {
	if (getPosition() != loginMode.teamViewer.getCenterIndex())
	    return;

	KeyCode code = e.getCode();
	if (code == KeyCode.R)
	    renameUser(this);
	else if (code == KeyCode.DELETE || code == KeyCode.D)
	    loginMode.deleteUser(this);
	else if (code == KeyCode.E)
	    exportImage();
    }

    //----------------------------------------About Images---------------------------------------------------------------

    /**
     * Reset's the user image back to the default
     */
    public void setDefaultImage() {
	deleteUserImage();
	imageView.setImage(null);
    }

    /**
     * The user has the ability to change the Library Image
     *
     */
    public void setNewImage() {

	File imageFile = Main.specialChooser.prepareToSelectImage(Main.window);
	if (imageFile == null)
	    return;

	//Check the given image
	Image image = new Image(imageFile.toURI() + "");
	if (image.getWidth() > 4800 || image.getHeight() > 4800) {
	    ActionTool.showNotification("Warning", "Maximum Size Allowed 4800*4800 \n Current is:" + image.getWidth() + "x" + image.getHeight(),
		    Duration.millis(2000), NotificationType.WARNING);
	    return;
	}

	//Determine the new path of the image
	String newImagePath = InfoTool.getAbsoluteDatabasePathWithSeparator() + getUserName() + File.separator + "userImage."
		+ InfoTool.getFileExtension(imageFile.getAbsolutePath());

	//Start a Thread to copy the File
	new Thread(() -> {
	    deleteUserImage();

	    //Do the copy procedure
	    if (!ActionTool.copy(imageFile.getAbsolutePath(), newImagePath)) {
		Platform.runLater(() -> ActionTool.showNotification("Failed saving background image", "Failed to change the background image...",
			Duration.millis(2500), NotificationType.SIMPLE));
		return;
	    }

	    //else
	    Platform.runLater(() -> imageView.setImage(new Image(new File(newImagePath).toURI() + "")));

	}).start();
    }

    /**
     * Export the Library image.
     */
    public void exportImage() {
	String absoluteImagePath = getAbsoluteImagePath();
	//Check if image exists
	if (absoluteImagePath == null)
	    return;

	File file = Main.specialChooser.prepareToExportImage(Main.window, absoluteImagePath);

	//Check if user selected a folder for the image to be exported
	if (file != null)
	    new Thread(() -> {
		if (!ActionTool.copy(absoluteImagePath, file.getAbsolutePath()))
		    Platform.runLater(() -> ActionTool.showNotification("Exporting User Image",
			    "Failed to export User image for \n User=[" + getUserName() + "]", Duration.millis(2500), NotificationType.SIMPLE));
	    }).start();

    }

    /**
     * Deletes the user background image
     */
    private boolean deleteUserImage() {

	//Try to delete the image
	try (Stream<Path> paths = Files.walk(Paths.get(new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + getUserName()).getPath()), 1)) {
	    return paths.filter(path -> {
		File file = path.toFile();
		return "userImage".equals(InfoTool.getFileTitle(file.getAbsolutePath())) && InfoTool.isImage(file.getAbsolutePath())
			&& !file.isDirectory();
	    }).findFirst().map(path -> path.toFile().delete()).orElse(false);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}

	return false;
    }

    /**
     * @return The absolute path of the image file of user , or null if not exists
     */
    public String getAbsoluteImagePath() {
	String absolutePath = null;

	//Try to find the image
	try (Stream<Path> paths = Files.walk(Paths.get(new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + getUserName()).getPath()), 1)) {
	    absolutePath = paths.filter(path -> {
		File file = path.toFile();
		return "userImage".equals(InfoTool.getFileTitle(file.getAbsolutePath())) && InfoTool.isImage(file.getAbsolutePath())
			&& !file.isDirectory();
	    }).findFirst().map(path -> path.toAbsolutePath().toString()).orElse(null);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}

	return absolutePath;
    }

}
