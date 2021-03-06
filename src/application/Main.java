/*
 * 
 */
package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jfoenix.controls.JFXTabPane;

import application.settings.window.ApplicationSettingsController;
import application.users.LoginMode;
import application.users.User;
import application.users.UserMode;
import borderless.BorderlessScene;
import database.LocalDBManager;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import librarysystema.LibraryMode;
import services.FilesFilterService;
import services.VacuumProgress;
import smartcontroller.MediaContextMenu;
import smartcontroller.PlayedMediaList;
import smartcontroller.SmartControllerSearcher.AdvancedSearch;
import tools.ActionTool;
import tools.InfoTool;
import tools.NotificationType;
import treeview.TreeViewManager;
import webBrowser.WebBrowserController;
import windows.AboutWindowController;
import windows.ConsoleWindowController;
import windows.ExportWindowController;
import windows.RenameWindow;
import windows.SearchWindow;
import windows.StarWindow;
import xplayer.presenter.XPlayersList;
import xr3capture.CaptureWindow;

/**
 * The Main class from which the application is starting.
 *
 * @author GOXR3PLUS
 */
public class Main extends Application {

    /** Application logger. */
    public static final Logger logger = Logger.getGlobal();

    //    /** The Constant tagWindow. */
    //    // public static final TagWindow tagWindow = new TagWindow()
    //    
    //    /** The capture window. */
    //    // public static CaptureWindow captureWindow
    // public static final RadioStationsController stationsInfostructure 

    /** The speech reader. */
    // public static RemoteAppsController speechReader

    //----------------START: The below have not depencities on other ---------------------------------//

    //

    /**
     * The SnapShot Window
     */

    /** The star window. */
    public static final StarWindow starWindow = new StarWindow();

    /** The rename window. */
    public static final RenameWindow renameWindow = new RenameWindow();

    /** This window is being used to export files from the application to the outside world */
    public static final ExportWindowController exportWindow = new ExportWindowController();

    /** The About Window of the Application */
    public static final AboutWindowController aboutWindow = new AboutWindowController();

    /** The console Window of the Application */
    public static final ConsoleWindowController consoleWindow = new ConsoleWindowController();

    /**
     * This Window contains the settings for the whole application
     */
    public static ApplicationSettingsController settingsWindow = new ApplicationSettingsController();

    /**
     * This class is used to capture the computer Screen or a part of it [ Check XR3Capture package]
     */
    public static CaptureWindow captureWindow = new CaptureWindow();

    /** The Search Window of the application */
    public static SearchWindow searchWindow = new SearchWindow();

    //

    /** The Top Bar of the Application */
    public static final TopBar topBar = new TopBar();

    /** The Side Bar of The Application */
    public static final SideBar sideBar = new SideBar();

    /** Application Update Screen */
    public static final UpdateScreen updateScreen = new UpdateScreen();

    /** The TreeView of DJMode */
    public static final TreeViewManager treeManager = new TreeViewManager();

    /** The Constant advancedSearch. */
    public static final AdvancedSearch advancedSearch = new AdvancedSearch();

    //

    /** The Constant songsContextMenu. */
    public static final MediaContextMenu songsContextMenu = new MediaContextMenu();

    /** The Constant specialChooser. */
    public static final SpecialChooser specialChooser = new SpecialChooser();

    //

    /** XPlayList holds the instances of XPlayerControllers */
    public static final XPlayersList xPlayersList = new XPlayersList();

    /** The Constant playedSongs. */
    public static final PlayedMediaList playedSongs = new PlayedMediaList();

    //

    /**
     * The WebBrowser of the Application
     */
    public static WebBrowserController webBrowser;

    //----------------END: The above have not depencities on other ---------------------------------//

    //----------------START: Vary basic for the application---------------------------------------//

    /** The window. */
    public static Stage window;

    /** The scene. */
    public static BorderlessScene scene;

    /** The stack pane root. */
    public static final StackPane applicationStackPane = new StackPane();

    /** The root. */
    public static final BorderPane root = new BorderPane();

    //

    /**
     * The current update of XR3Player
     */
    public static final int currentVersion = 65;

    /**
     * This application version release date
     */
    public static final String releaseDate = "03/05/2017";

    /**
     * The Thread which is responsible for the update check
     */
    private static Thread updaterThread;

    /** The can save data. */
    public static boolean canSaveData = true;

    //---------------END:Vary basic for the application---------------------------------//

    // --------------START: The below have depencities on others------------------------

    /** The Constant dbManager. */
    public static LocalDBManager dbManager;

    /** The Constant libraryMode. */
    public static LibraryMode libraryMode = new LibraryMode();

    /** The Constant djMode. */
    public static DJMode djMode = new DJMode();

    /** The Constant multipleTabs. */
    public static MultipleTabs multipleTabs = new MultipleTabs();

    /** The Login Mode where the user of the applications has to choose an account to login */
    public static LoginMode loginMode = new LoginMode();

    /**
     * Entering in this mode you can change the user settings and other things that have to do with the user....
     */
    public static UserMode userMode = new UserMode();

    /***
     * This BorderPane has in the center the root , at the left the SideBar and on the Top the TopBar
     */
    // private static BorderPane applicationBorderPane = new BorderPane();

    public static JFXTabPane specialJFXTabPane = new JFXTabPane();

    // --------------END: The below have depencities on others------------------------

    @Override
    public void start(Stage primaryStage) throws Exception {

	try {

	    //new Tester()

	    // logger.info("XR3Player Application Started")
	    System.out.println("XR3Player Application Started");

	    //Initialize some classes which heavily rely on JavaFX Thread here
	    webBrowser = new WebBrowserController();

	    //ApplicationStackPane
	    applicationStackPane.getChildren().addAll(root, loginMode, updateScreen);

	    //ApplicationBorderPane	    
	    //applicationBorderPane.setStyle("-fx-background-color:black;")
	    // applicationBorderPane.setCenter(root)

	    //LoginMode    
	    loginMode.setLeft(sideBar);
	    sideBar.prepareForLoginMode(true);

	    // root
	    root.setTop(topBar);

	    //Misc
	    updateScreen.setVisible(false);
	    topBar.setVisible(false);

	    // Window
	    window = primaryStage;
	    starWindow.getWindow().initOwner(window);
	    renameWindow.getWindow().initOwner(window);
	    exportWindow.getWindow().initOwner(window);
	    consoleWindow.getWindow().initOwner(window);
	    settingsWindow.getWindow().initOwner(window);
	    aboutWindow.getWindow().initOwner(window);
	    searchWindow.getWindow().initOwner(window);
	    topBar.addXR3LabelBinding();

	    // captureWindow
	    // captureWindow = new
	    // CaptureWindow(InfoTool.getScreenWidth(),InfoTool.getScreenHeight(),
	    // window)

	    window.setTitle("XR3Player V." + currentVersion);
	    loginMode.getXr3PlayerLabel().setText("~" + window.getTitle() + "~");
	    // -------------------
	    // -------Due to a bug i need the width%2==0---------
	    int width = (int) (InfoTool.getVisualScreenWidth() * 0.77);
	    width = (width % 2 == 0) ? width : width + 1;
	    // -------------------
	    window.setWidth(width);
	    window.setHeight(InfoTool.getVisualScreenHeight() * 0.91);
	    window.centerOnScreen();
	    window.getIcons().add(InfoTool.getImageFromDocuments("icon.png"));
	    window.centerOnScreen();
	    window.setOnCloseRequest(exit -> {
		exitQuestion();
		exit.consume();
	    });

	    // Root
	    // root.setStyle(
	    //	    "-fx-background-color:rgb(0,0,0,0.9); -fx-background-size:100% 100%; -fx-background-image:url('/image/background.jpg'); -fx-background-position: center center; -fx-background-repeat:stretch;");

	    // Scene
	    scene = new BorderlessScene(window, StageStyle.UNDECORATED, applicationStackPane, 650, 500);
	    scene.setMoveControl(loginMode.getXr3PlayerLabel());
	    scene.getStylesheets().add(getClass().getResource(InfoTool.STYLES + InfoTool.APPLICATIONCSS).toExternalForm());
	    searchWindow.getWindow().getScene().getStylesheets().addAll(scene.getStylesheets());

	    // Scene and Show
	    determineBackgroundImage();
	    window.setScene(scene);
	    window.show();

	    //Do this in order to now have problems with SongsContextMenu
	    songsContextMenu.show(window, 0, 0);
	    songsContextMenu.hide();
	    libraryMode.librariesContextMenu.show(window, 0, 0);
	    libraryMode.librariesContextMenu.hide();

	    //Check if dataBase Folder exists
	    File dataBaseFolder = new File(InfoTool.getAbsoluteDatabasePathPlain());
	    if (!dataBaseFolder.exists()) {
		//If it can not be created [FATAL ERROR]
		if (!dataBaseFolder.mkdir())
		    ActionTool.showNotification("Fatal Error!",
			    "Fatal Error Occured trying to create \n the root database folder [ XR3DataBase] \n Maybe the application has not the permission to create this folder.",
			    Duration.seconds(45), NotificationType.ERROR);
	    } //If it does
	    else {

		//Create the List with the Available Users
		AtomicInteger counter = new AtomicInteger();
		loginMode.teamViewer.addMultipleUsers(Files.walk(Paths.get(InfoTool.getAbsoluteDatabasePathPlain()), 1)
			.filter(path -> path.toFile().isDirectory() && !(path + "").equals(InfoTool.getAbsoluteDatabasePathPlain()))
			.map(path -> new User(path.getFileName() + "", counter.getAndAdd(1), loginMode)).collect(Collectors.toList()));

		//avoid error
		if (!loginMode.teamViewer.getItemsObservableList().isEmpty())
		    loginMode.teamViewer.setCenterIndex(loginMode.teamViewer.getItemsObservableList().size() / 2);
	    }

	    //Create Original xr3database singature file	    
	    if (dataBaseFolder.exists() && !InfoTool.getDatabaseSignatureFile().exists())
		try {
		    //I need to fix this for errors
		    InfoTool.getDatabaseSignatureFile().createNewFile();
		} catch (IOException ex) {
		    Main.logger.log(Level.WARNING, ex.getMessage(), ex);
		}

	    //Users Search Box
	    loginMode.userSearchBox.registerListeners(window);

	    //Check Compatibility
	    checkJavaCombatibility();

	    //Check for updates
	    Main.checkForUpdates(false);

	    //Main.songsContextMenu.show(window)
	    // Main.songsContextMenu.hide()
	    // throw new Exception("xd")
	    // ScenicView.show(scene)

	} catch (Exception ex) {
	    logger.log(Level.SEVERE, "Application has serious problem and can't start", ex);
	    ActionTool.showNotification("Fatal Error", "Fatal Error happened trying to run the application... :(", Duration.millis(10000),
		    NotificationType.ERROR);
	}

    }

    @Override
    public void init() {
	System.out.println("Hello from init");
    }

    /**
     * The user has the ability to change the Library Image
     * 
     */
    public static void changeBackgroundImage() {

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
	if (image.getWidth() < 1000 || image.getHeight() < 1000) {
	    ActionTool.showNotification("Warning", "Minimum Size Allowed 1200*1200 \n Current is:" + image.getWidth() + "x" + image.getHeight(),
		    Duration.millis(2000), NotificationType.WARNING);
	    return;
	}

	BackgroundImage bgImg = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
		new BackgroundSize(window.getWidth(), window.getHeight(), true, true, true, true));
	//loginMode.setBackground(new Background(bgImg))
	root.setBackground(new Background(bgImg));

	//Start a Thread to copy the File
	new Thread(() -> {
	    try (Stream<Path> paths = Files.walk(Paths.get(new File(InfoTool.getAbsoluteDatabasePathPlain()).getPath()), 1)) {
		paths.forEach(path -> {
		    File file = path.toFile();
		    if (file.getName().contains("background") && InfoTool.isImage(file.getAbsolutePath()) && !file.isDirectory())
			file.delete(); //fuck if failed... Good programming practice xoaoxaoxoaoxo -> to be fixed
		});
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }

	    if (!ActionTool.copy(imageFile.getAbsolutePath(),
		    InfoTool.getAbsoluteDatabasePathWithSeparator() + "background." + InfoTool.getFileExtension(imageFile.getAbsolutePath())))
		Platform.runLater(() -> ActionTool.showNotification("Failed saving background image", "Failed to change the background image...",
			Duration.millis(2500), NotificationType.SIMPLE));

	}).start();
    }

    static boolean backgroundFound;

    /**
     * Determines the background image of the application based on if a custom image exists inside the database .If not then the default image is
     * being added :)
     * 
     */
    private static void determineBackgroundImage() {
	backgroundFound = false;

	//Check if a background image exists
	if (new File(InfoTool.getAbsoluteDatabasePathPlain()).exists())
	    try (Stream<Path> paths = Files.walk(Paths.get(new File(InfoTool.getAbsoluteDatabasePathPlain()).getPath()), 1)) {
		paths.forEach(path -> {
		    File file = path.toFile();
		    if (file.getName().contains("background") && InfoTool.isImage(file.getAbsolutePath()) && !file.isDirectory()) {

			Image img = new Image(file.toURI() + "");
			BackgroundImage bgImg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.DEFAULT, new BackgroundSize(window.getWidth(), window.getHeight(), true, true, true, true));
			//loginMode.setBackground(new Background(bgImg))
			root.setBackground(new Background(bgImg));

			//Found?
			backgroundFound = true;

		    }
		});
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }

	//Check if background is found
	if (backgroundFound)
	    return;

	Image img = new Image("/image/visualizer.jpg");
	BackgroundImage bgImg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
		new BackgroundSize(window.getWidth(), window.getHeight(), true, true, true, true));
	root.setBackground(new Background(bgImg));

	//Junk COde
	//		    loginMode.setStyle(
	//			    "-fx-background-color:rgb(0,0,0,0.9); -fx-background-size:100% 100%; -fx-background-image:url('/image/background.jpg'); -fx-background-position: center center; -fx-background-repeat:stretch;")
	//	root.setStyle(loginMode.getStyle())

    }

    /**
     * Starts the application for this specific user
     * 
     * @param u
     */
    public static void startAppWithUser(User u) {

	//Close the LoginMode
	loginMode.setVisible(false);
	updateScreen.setVisible(true);

	//SideBar
	sideBar.setVisible(false);
	sideBar.setManaged(false);
	sideBar.prepareForLoginMode(false);
	root.setLeft(sideBar);

	//userNameLabel	
	sideBar.getUserNameLabel().setText("Hello -> " + u.getUserName() + " <- !");

	//Top Bar is the new Move Control
	scene.setMoveControl(topBar);

	//Do a pause so the login mode dissapears
	PauseTransition pause = new PauseTransition(Duration.millis(500));
	pause.setOnFinished(f -> {

	    //----------------START:initialize everything needed------------------------------------------

	    //Create this in a Thread
	    Thread s = new Thread(() -> dbManager = new LocalDBManager(u.getUserName()));
	    s.start();

	    //Do the below until the database is initialized
	    userMode.setUser(u);
	    libraryMode.add(Main.multipleTabs, 0, 1);
	    sideBar.setVisible(true);
	    sideBar.setManaged(true);
	    topBar.setVisible(true);

	    specialJFXTabPane.getTabs().add(new Tab("tab1", libraryMode));
	    specialJFXTabPane.getTabs().add(new Tab("tab2", djMode));
	    specialJFXTabPane.getTabs().add(new Tab("tab3", userMode));
	    specialJFXTabPane.getTabs().add(new Tab("tab4", webBrowser));
	    specialJFXTabPane.setTabMaxWidth(0);
	    specialJFXTabPane.setTabMaxHeight(0);

	    //Add listeners to each tab
	    final AtomicInteger counter = new AtomicInteger(-1);
	    specialJFXTabPane.getTabs().forEach(tab -> {
		final int index = counter.addAndGet(1);
		tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
		    if (specialJFXTabPane.getTabs().get(index).isSelected() && !topBar.isTabSelected(index))
			topBar.selectTab(index);
		    //System.out.println("Entered Tab " + index) //this is inside curly braces with the above if

		});
	    });
	    root.setCenter(specialJFXTabPane);

	    //---------------END:initialize everything needed---------------------------------------------

	    //----------------START: Important Work-------------------------------------------------------

	    // Register some listeners to the main window
	    libraryMode.librariesSearcher.registerListeners(window);

	    //When Top Bar to be visible?
	    //topBar.visibleProperty()
	    //    .bind(libraryMode.sceneProperty().isNotNull().or(djMode.sceneProperty().isNotNull()))
	    //sideBar.visibleProperty().bind(topBar.visibleProperty())

	    //Important binding 
	    libraryMode.multipleLibs.emptyLabel.textProperty()
		    .bind(Bindings.when(Main.libraryMode.teamViewer.getViewer().itemsWrapperProperty().emptyProperty())
			    .then("Click here to create a library...").otherwise("Click here to open the first available library..."));

	    //Load the DataBase - After the DBManager has been initialized of course ;)
	    try {
		s.join();
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	    dbManager.loadApplicationDataBase();

	    //  dbManager.recreateJSonDataBase()
	    //  dbManager.loadOpenedLibraries()
	    //  dbManager.updateLibrariesInformation(null)

	    //Filter Thread (Inspecting the Files if existing)
	    new FilesFilterService().start();

	    //---------------END:Important Work-----------------------------------------------------------

	});
	pause.playFromStart();
    }

    /**
     * Checks if the Current Java Version is the appropriate for the application
     */
    public void checkJavaCombatibility() {
	//String minimumJavaVersion = "1.8.0_111"
	String[] javaVersionElements = System.getProperty("java.runtime.version").split("\\.|_|-b");

	//String discard = javaVersionElements[0]
	String major = javaVersionElements[1];
	//String minor = javaVersionElements[2]
	String update = javaVersionElements[3];
	//String build = javaVersionElements[4]
	//System.out.println(Arrays.asList(javaVersionElements));

	if (Integer.parseInt(major) < 8 || (Integer.parseInt(major) < 8 && Integer.parseInt(update) < 111))
	    ActionTool.showNotification("Java Version Problem",
		    "XR3Player needs at least Java Version:1.8.0_111  -> Your current Java Version is:" + System.getProperty("java.version")
			    + "\nThe application may crash or not work at all!\nPlease Update your Java Version :)",
		    Duration.seconds(40), NotificationType.ERROR);
    }

    /**
     * Terminate the application.
     *
     * @param vacuum
     *            the vacuum
     */
    public static void terminate(boolean vacuum) {

	//I need to check it in case no user is logged in 
	if (dbManager != null) {
	    if (libraryMode.multipleLibs.isFree(true)) {

		// Stop SpeechReader
		//	    if (speechReader != null)
		//		speechReader.stopSpeechRec(true)

		// vacuum?
		if (vacuum) {
		    VacuumProgress vService = new VacuumProgress();
		    updateScreen.label.textProperty().bind(vService.messageProperty());
		    updateScreen.progressBar.setProgress(-1);
		    updateScreen.progressBar.progressProperty().bind(vService.progressProperty());
		    updateScreen.setVisible(true);

		    vService.start(new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + "user" + File.separator + "dbFile.db"),
			    new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + "user" + File.separator + "dbFile.db-journal"));

		    // Go
		    dbManager.commitAndVacuum();
		} else
		    System.exit(0);
	    }
	} else
	    System.exit(0);

    }

    /**
     * Exit App Confirmation.
     */
    public static void exitQuestion() {
	Alert alert = new Alert(AlertType.CONFIRMATION);
	alert.initStyle(StageStyle.UTILITY);
	alert.initOwner(window);
	alert.setTitle("Terminate the application?");

	alert.setHeaderText("Vacuum is clearing junks from database\n(In future updates it will be automatical)");
	alert.setContentText("Pros:\nThe database file may be shrinked \n\nCons:\nIt may take some seconds to be done\n");
	ButtonType exit = new ButtonType("Exit", ButtonData.OK_DONE);
	ButtonType vacuum = new ButtonType("Vacuum + Exit", ButtonData.OK_DONE);
	ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

	// alert.getDialogPane()
	// .getScene()
	// .setFill(Color.TRANSPARENT)
	// alert.getDialogPane()
	// .getStylesheets()
	// .add(Main.class.getResource(InfoTool.styLes +
	// InfoTool.applicationCss)
	// ((Button) alert.getDialogPane()
	// .lookupButton(ButtonType.CANCEL)).setDefaultButton(true)
	alert.getButtonTypes().setAll(vacuum, exit, cancel);

	alert.showAndWait().ifPresent(answer -> {
	    if (answer == exit)
		terminate(false);
	    else if (answer == vacuum)
		terminate(true);

	});
    }

    /**
     * Calling this method restarts the application
     * 
     * @param askUser
     *            Ask the User if he/she wants to restart the application
     */
    public static void restartTheApplication(boolean askUser) {

	// Restart XR3Player
	new Thread(() -> {
	    String path = InfoTool.getBasePathForClass(Main.class);
	    String applicationPath = new File(path + "XR3Player.jar").getAbsolutePath();

	    //Show message that application is restarting
	    Platform.runLater(() -> ActionTool.showNotification("Message",
		    "Restarting XR3Player....\n Current directory path is:[ " + applicationPath
			    + " ] \n If this takes more than 20 seconds either the computer is slow or it has failed....",
		    Duration.seconds(25), NotificationType.INFORMATION));

	    try {

		System.out.println("XR3PlayerPath is : " + applicationPath);

		ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		// Wait 20 seconds
		PauseTransition pause = new PauseTransition(Duration.seconds(20));
		pause.setOnFinished(f -> {

		    // Show failed message
		    Platform.runLater(
			    () -> ActionTool
				    .showNotification(
					    "Message", "Failed to restart XR3Player!\nBuilder Directory:" + applicationPath + "\nTrying to start:"
						    + path + "XR3Player.jar\nTry to do it manually...",
					    Duration.seconds(10), NotificationType.ERROR));

		    // Ask the user
		    if (askUser)
			Platform.runLater(() -> {
			    if (ActionTool.doQuestion("Restart failed.... force shutdown?"))
				terminate(false);
			});
		    else {
			// Terminate after showing the message for a while
			PauseTransition forceTerminate = new PauseTransition(Duration.seconds(2));
			forceTerminate.setOnFinished(fn -> terminate(false));
			forceTerminate.play();
		    }

		});
		pause.play();

		// Continuously Read Output
		String line;
		while (process.isAlive())
		    while ((line = bufferedReader.readLine()) != null) {
			if (line.isEmpty())
			    break;
			if (line.contains("XR3Player Application Started"))
			    terminate(false);
		    }

		// process.waitFor()
		// i(process.exitValue() != 0)
		// else
		// Main.terminate(false)

	    } catch (IOException ex) {
		Logger.getLogger(Main.class.getName()).log(Level.INFO, null, ex);
		Platform.runLater(() -> {
		    Main.updateScreen.setVisible(false);

		    // Show failed message		  
		    Platform.runLater(
			    () -> ActionTool
				    .showNotification(
					    "Message", "Failed to restart XR3Player!\nBuilder Directory:" + applicationPath + "\nTrying to start:"
						    + path + "XR3Player.jar\nTry to do it manually...",
					    Duration.seconds(10), NotificationType.ERROR));
		});
	    }
	}, "Restart Application Thread").start();
    }

    /**
     * This method is fetching data from github to check if the is a new update for XR3Player
     * 
     * @param showTheWindow
     *            If not update is available then don't show the window
     */
    public static synchronized void checkForUpdates(boolean showTheWindow) {

	// Not already running
	if (updaterThread == null || !updaterThread.isAlive()) {
	    updaterThread = new Thread(() -> {
		Platform.runLater(() -> ActionTool.showNotification("Searching for Updates", "Fetching informations from server...",
			Duration.millis(1000), NotificationType.INFORMATION));

		if (InfoTool.isReachableByPing("www.google.com")) {

		    try {

			Document doc = Jsoup.connect("https://raw.githubusercontent.com/goxr3plus/XR3Player/master/XR3PlayerUpdatePage.html").get();

			//Document doc = Jsoup.parse(new File("XR3PlayerUpdatePage.html"), "UTF-8", "http://example.com/");

			Element lastArticle = doc.getElementsByTag("article").last();

			// Not disturb the user every time the application starts if there is not new update
			if (Integer.valueOf(lastArticle.id()) <= currentVersion && !showTheWindow)
			    return;

			// Update is available or not?
			Platform.runLater(() -> {
			    Alert alert = new Alert(AlertType.CONFIRMATION);
			    alert.setTitle("Update Window");
			    if (Integer.valueOf(lastArticle.id()) <= currentVersion) {
				alert.setHeaderText("You are up too date :)");
				alert.setContentText("Your current version is: ->( " + currentVersion + " )<-");
			    } else {
				alert.setHeaderText("New Update available!!!");
				alert.setContentText("Update ->( " + lastArticle.id() + " )<- is available!\n\t\t\t\t\tYour current version is: ->( "
					+ currentVersion + " )<-");
			    }
			    alert.initStyle(StageStyle.UTILITY);
			    alert.initOwner(Main.window);

			    // Label label = new Label("Information about the
			    // latest update :)")

			    InlineCssTextArea textArea = new InlineCssTextArea();
			    textArea.setEditable(false);
			    textArea.setFocusTraversable(false);
			    // textArea.setWrapText(true)

			    VirtualizedScrollPane<InlineCssTextArea> vsPane = new VirtualizedScrollPane<>(textArea);
			    vsPane.setMinSize(450, 550);
			    vsPane.setMaxWidth(Double.MAX_VALUE);
			    vsPane.setMaxHeight(Double.MAX_VALUE);
			    GridPane.setVgrow(vsPane, Priority.ALWAYS);
			    GridPane.setHgrow(vsPane, Priority.ALWAYS);

			    GridPane expContent = new GridPane();
			    expContent.setMaxWidth(Double.MAX_VALUE);
			    expContent.setMaxHeight(Double.MAX_VALUE);
			    // expContent.add(label, 0, 0)
			    expContent.add(vsPane, 0, 0);

			    InlineCssTextArea textArea2 = new InlineCssTextArea();
			    textArea2.setEditable(false);
			    textArea2.setFocusTraversable(false);
			    textArea2.setWrapText(true);

			    VirtualizedScrollPane<InlineCssTextArea> vsPane2 = new VirtualizedScrollPane<>(textArea2);
			    vsPane2.setMinSize(450, 550);
			    vsPane2.setMaxWidth(Double.MAX_VALUE);
			    vsPane2.setMaxHeight(Double.MAX_VALUE);
			    GridPane.setVgrow(vsPane2, Priority.ALWAYS);
			    GridPane.setHgrow(vsPane2, Priority.ALWAYS);

			    expContent.add(vsPane2, 1, 0);

			    // -- TextArea 
			    String style = "-fx-font-weight:bold; -fx-font-size:14; -fx-fill:black;";
			    doc.getElementsByTag("article").forEach(element -> {

				// Append the text to the textArea
				textArea.appendText("\n\n-------------Start of Update (" + element.id() + ")-------------\n");

				// Information
				textArea.appendText("->Information: ");
				textArea.setStyle(textArea.getLength() - 13, textArea.getLength() - 1, style.replace("black", "#202020"));
				textArea.appendText(element.getElementsByClass("about").text() + "\n");

				// Release Date
				textArea.appendText("->Release Date: ");
				textArea.setStyle(textArea.getLength() - 14, textArea.getLength() - 1, style.replace("black", "firebrick"));
				textArea.appendText(element.getElementsByClass("releasedate").text() + "\n");

				// Minimum JRE
				textArea.appendText("->Minimum Java Version: ");
				textArea.setStyle(textArea.getLength() - 22, textArea.getLength() - 1, style.replace("black", "orange"));
				textArea.appendText(element.getElementsByClass("minJavaVersion").text() + "\n");

				// ChangeLog
				textArea.appendText("->ChangeLog:\n");
				textArea.setStyle(textArea.getLength() - 11, textArea.getLength() - 1, style.replace("black", "green"));
				final AtomicInteger counter = new AtomicInteger(-1);
				Arrays.asList(element.getElementsByClass("changelog").text().split("\\*")).forEach(el -> {
				    if (counter.addAndGet(1) >= 1) {
					String s = "\t" + counter + ")";
					textArea.appendText(s);
					textArea.setStyle(textArea.getLength() - s.length(), textArea.getLength() - 1, style);
					textArea.appendText(el + "\n");
				    }
				});

			    });

			    textArea.moveTo(textArea.getLength());
			    textArea.requestFollowCaret();

			    // -- TextArea 2
			    doc.getElementsByTag("section").forEach(section -> {

				// Append the text to the textArea
				textArea2.appendText("\n\n-------------Upcoming Features for XR3Player-------------\n\n");

				// Information
				textArea2.appendText("->Coming:\n");
				textArea2.setStyle(textArea2.getLength() - 8, textArea2.getLength() - 1, style.replace("black", "green"));
				final AtomicInteger counter = new AtomicInteger(-1);
				Arrays.asList(section.getElementById("info").text().split("\\*")).forEach(el -> {
				    if (counter.addAndGet(1) >= 1) {
					String s = "\t" + counter + ")";
					textArea2.appendText(s);
					textArea2.setStyle(textArea2.getLength() - s.length(), textArea2.getLength() - 1, style);
					textArea2.appendText(el + "\n");
				    }
				});

				//Last Updated
				textArea2.appendText("->Last Updated: ");
				textArea2.setStyle(textArea2.getLength() - 14, textArea2.getLength() - 1, style.replace("black", "firebrick"));
				textArea2.appendText(section.getElementById("lastUpdated").text());
			    });

			    textArea2.moveTo(textArea2.getLength());
			    textArea2.requestFollowCaret();

			    // Set the default buttons
			    //ButtonType autoUpdate = new ButtonType("Auto Update", ButtonData.YES)
			    ButtonType download = new ButtonType("Download", ButtonData.OK_DONE);
			    ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			    alert.getButtonTypes().setAll(download, cancel);

			    // Set expandable Exception into the dialog pane.
			    alert.getDialogPane().setExpandableContent(expContent);
			    alert.getDialogPane().setExpanded(true);
			    alert.getDialogPane().setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

			    textArea.requestFocus();
			    // Show and Wait
			    alert.showAndWait().ifPresent(answer -> {
				if (answer == download)
				    ActionTool.openWebSite("https://sourceforge.net/projects/xr3player/");
			    });

			});
		    } catch (IOException ex) {
			Platform.runLater(() -> ActionTool.showNotification("Error", "Trying to fetch update information a problem occured",
				Duration.millis(2500), NotificationType.ERROR));
			logger.log(Level.WARNING, "", ex);
		    }

		} else
		    Platform.runLater(() -> ActionTool.showNotification("Can't Connect",
			    "Can't connect to the update site :\n1) Maybe there is not internet connection\n2)GitHub is down for maintenance",
			    Duration.millis(2500), NotificationType.ERROR));

	    }, "Application Update Thread");

	    updaterThread.setDaemon(true);
	    updaterThread.start();
	}
    }

    /**
     * Main Method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
	launch(args);
    }
}
