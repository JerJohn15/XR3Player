/*
 * 
 */
package windows;

import java.io.IOException;

import org.controlsfx.control.textfield.TextFields;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tools.ActionTool;
import tools.InfoTool;
import tools.NotificationType;

/**
 * The Class RenameWindow.
 */
public class RenameWindow extends VBox {

    @FXML
    private Label titleLabel;

    @FXML
    private Label charsField;

    @FXML
    private Button okButton;

    @FXML
    private Button closeButton;

    // ----------------

    /**
     * The field inside the user writes the text
     */
    public TextField inputField = TextFields.createClearableTextField();

    // Custom Event Handler
    EventHandler<ActionEvent> myHandler = e -> {

	// can pass?
	if (!inputField.getText().trim().isEmpty())
	    close(true);
	else
	    ActionTool.showNotification("Message", "You have to type something..", Duration.millis(1500), NotificationType.WARNING);

    };

    /** The window */
    private Stage window = new Stage();

    /** If it was accepted */
    private boolean accepted = false;

    /** The not allow. */
    String[] notAllow = new String[] { "/", "\\", ":", "*", "?", "\"", "<", ">", "|", "'", "." };

    /**
     * Constructor
     */
    public RenameWindow() {

	// Window
	window.setTitle("Rename Window");
	window.setMinHeight(100);
	window.setMinWidth(300);
	window.setWidth(440);
	window.setHeight(80);
	window.initModality(Modality.APPLICATION_MODAL);
	window.initStyle(StageStyle.TRANSPARENT);
	window.getIcons().add(InfoTool.getImageFromDocuments("icon.png"));
	window.centerOnScreen();
	window.setOnCloseRequest(ev -> close(false));
	window.setAlwaysOnTop(true);

	// ----------------------------------FXMLLoader
	FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.FXMLS + "RenameWindow.fxml"));
	loader.setController(this);
	loader.setRoot(this);

	try {
	    loader.load();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}

	// ----------------------------------Scene
	window.setScene(new Scene(this, Color.TRANSPARENT));
	//getScene().getStylesheets()
	//	.add(getClass().getResource(InfoTool.styLes + InfoTool.applicationCss).toExternalForm())
	getScene().setOnKeyReleased(key -> {
	    if (key.getCode() == KeyCode.ESCAPE)
		close(false);
	});

    }

    /**
     * Called as soon as .fxml has been initialized
     */
    @FXML
    private void initialize() {

	// CharsField
	charsField.textProperty().bind(inputField.textProperty().length().asString());

	// inputField
	getChildren().add(inputField);
	inputField.setMinSize(420, 32);
	inputField.setTooltip(new Tooltip("Not allowed:(<) (>) (:) (\") (/) (\\) (|) (?) (*) (') (.) \n **Press Escape to Exit**"));
	inputField.setPromptText("Type Here...");
	inputField.setStyle("-fx-font-weight:bold; -fx-font-size:14;");
	//inputField.setPrefColumnCount(200)
	//inputField.prefColumnCountProperty().bind(inputField.textProperty().length().add(1));
	inputField.textProperty().addListener((observable, oldValue, newValue) -> {
	    //Check newValue
	    if (newValue != null) {

		// Allow until 150 characters
		if (newValue.length() > 150)
		    inputField.setText(newValue.substring(0, 150));

		// Strict Mode
		for (String character : notAllow)
		    if (newValue.contains(character))
			inputField.setText(newValue.replace(character, ""));
	    }
	});
	//---prefColumnCountProperty
	//	inputField.prefColumnCountProperty().addListener((observable, oldValue, newValue) -> {
	//	    if (inputField.getWidth() < 450)
	//		window.setWidth(inputField.getWidth() + 50);
	//	});
	inputField.setOnAction(myHandler);

	// okButton
	okButton.setOnAction(myHandler);

	// closeButton
	closeButton.setOnAction(action -> close(false));

	//window.show();
    }

    /**
     * get the input that connectedUser Typed.
     *
     * @return the user input
     */
    public String getUserInput() {
	return inputField.getText();
    }

    /**
     * Checks if it was cancelled
     *
     * @return True if it was cancelled , false if not
     */
    public boolean wasAccepted() {
	return accepted;
    }

    /**
     * Close the Window.
     *
     * @param accepted1
     *            True if accepted , False if not
     */
    public void close(boolean accepted1) {
	this.accepted = accepted1;
	window.close();
    }

    /**
     * Show Window with the given parameters.
     *
     * @param text
     *            the text
     * @param n
     *            the node
     * @param title
     *            The text if the title Label
     */
    public void show(String text, Node n, String title) {

	// Auto Calculate the position
	Bounds bounds = n.localToScreen(n.getBoundsInLocal());
	show(text, bounds.getMinX() + 5, bounds.getMaxY(), title);
    }

    /**
     * Show Window with the given parameters.
     *
     * @param text
     *            the text
     * @param x
     *            the x
     * @param y
     *            the y
     * @param title
     *            The text if the title Label
     */
    public void show(String text, double x, double y, String title) {

	if (x <= -1 && y <= -1)
	    window.centerOnScreen();
	else {
	    if (x + getWidth() > InfoTool.getVisualScreenWidth())
		x = InfoTool.getVisualScreenWidth() - getWidth();
	    else if (x < 0)
		x = 0;

	    if (y + getHeight() > InfoTool.getVisualScreenHeight())
		y = InfoTool.getVisualScreenHeight() - getHeight();
	    else if (y < 0)
		y = 0;

	    window.setX(x);
	    window.setY(y);
	}

	titleLabel.setText(title);
	inputField.setText(text);
	accepted = true;
	window.show();

	//	
	inputField.requestFocus();
	inputField.end();
    }

    /**
     * @return Whether or not this {@code Stage} is showing (that is, open on the user's system). The Stage might be "showing", yet the user might not
     *         be able to see it due to the Stage being rendered behind another window or due to the Stage being positioned off the monitor.
     * 
     *
     * @defaultValue false
     */
    public ReadOnlyBooleanProperty showingProperty() {
	return window.showingProperty();
    }

    /**
     * @return Whether or not this {@code Stage} is showing (that is, open on the user's system). The Stage might be "showing", yet the user might not
     *         be able to see it due to the Stage being rendered behind another window or due to the Stage being positioned off the monitor.
     * 
     */
    public boolean isShowing() {
	return showingProperty().get();
    }

    /**
     * @return the window
     */
    public Stage getWindow() {
	return window;
    }

}
