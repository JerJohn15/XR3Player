/*
 * 
 */
package streamplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import application.Main;
import javafx.application.Platform;
import javazoom.spi.PropertiesContainer;
import tools.ActionTool;
import tools.NotificationType;

/**
 * StreamPlayer is a class based on JavaSound API. It has
 * been successfully tested under J2SE 1.3.x, 1.4.x,1.5.x,1.6.x,1.7.x,1.8.x.
 */
public class StreamPlayer implements Runnable {
	
	/**
	 * Status of Stream Player.
	 *
	 * @author GOXR3PLUS
	 */
	public enum Status {
		
		/** UNKOWN STATUS. */
		UNKNOWN,
		
		/** In the process of opening the AudioInputStream. */
		OPENING,
		
		/** AudioInputStream is opened. */
		OPENED,
		
		/** play event has been fired. */
		PLAYING,
		
		/** player is stopped. */
		STOPPED,
		
		/** player is paused. */
		PAUSED,
		
		/** resume event is fired. */
		RESUMED,
		
		/** player is in the process of seeking. */
		SEEKING,
		
		/** seek work has been done. */
		SEEKED,
		
		/** EOM stands for "END OF MEDIA". */
		EOM,
		
		/** player pan has changed. */
		PAN,
		
		/** player gain has changed. */
		GAIN;
		
	}
	
	/** The Constant UNAVAILABLE. */
	private static final int UNAVAILABLE = -1;
	
	/** The Constant EXTERNAL_BUFFER_SIZE. */
	private static final int EXTERNAL_BUFFER_SIZE = 4000 * 4;
	
	/** The Constant SKIP_INACCURACY_SIZE. */
	private static final int SKIP_INACCURACY_SIZE = 1200;
	
	/** The thread. */
	private Thread thread = null;
	
	/** The data source. */
	private Object dataSource;
	
	/** The audio input stream. */
	private AudioInputStream audioInputStream;
	
	/** The encoded audio input stream. */
	private AudioInputStream encodedAudioInputStream;
	
	/** The encoded audio length. */
	private int encodedAudioLength = -1;
	
	/** The audio file format. */
	private AudioFileFormat audioFileFormat;
	
	/** The source data line. */
	private SourceDataLine sourceDataLine;
	
	/** The gain control. */
	// Controls
	private FloatControl gainControl;
	
	/** The pan control. */
	private FloatControl panControl;
	
	/** The balance control. */
	private FloatControl balanceControl;
	
	/** The sample rate control. */
	private FloatControl sampleRateControl;
	
	/** The mute control. */
	private BooleanControl muteControl;
	
	/** The current line buffer size. */
	// protected String mixerName = null
	private int currentLineBufferSize = -1;
	
	/** The line buffer size. */
	private int lineBufferSize = -1;
	
	/** The status. */
	private Status status = Status.UNKNOWN;
	
	/** The listeners. */
	// Listeners to be notified.
	private ArrayList<StreamPlayerListener> listeners = new ArrayList<>();
	
	/** The empty map. */
	private Map<String,Object> emptyMap = new HashMap<>();
	
	/** Properties when the File/URL/InputStream is opened. */
	private Map<String,Object> audioProperties;
	
	/**
	 * Constructor.
	 */
	public StreamPlayer() {
		reset();
		
	}
	
	/**
	 * Freeing the resources.
	 */
	private void reset() {
		status = Status.UNKNOWN;
		if (audioInputStream != null)
			synchronized (audioInputStream) {
				closeStream();
			}
		
		audioInputStream = null;
		audioFileFormat = null;
		encodedAudioInputStream = null;
		encodedAudioLength = -1;
		if (sourceDataLine != null) {
			sourceDataLine.stop();
			sourceDataLine.close();
			sourceDataLine = null;
		}
		
		gainControl = null;
		panControl = null;
		balanceControl = null;
		sampleRateControl = null;
	}
	
	/**
	 * Notify listeners about a BasicPlayerEvent.
	 *
	 * @param playerStatus event code.
	 * @param encodedStreamPosition in the stream when the event occurs.
	 * @param description the description
	 */
	protected void notifyEvent(Status playerStatus , int encodedStreamPosition , Object description) {
		new StreamPlayerEventLauncher(this, playerStatus, encodedStreamPosition, description,
		        new ArrayList<StreamPlayerListener>(listeners)).start();
	}
	
	/**
	 * Add a listener to be notified.
	 *
	 * @param listener the listener
	 */
	public void addStreamPlayerListener(StreamPlayerListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove registered listener.
	 *
	 * @param listener the listener
	 */
	public void removeStreamPlayerListener(StreamPlayerListener listener) {
		if (listeners != null)
			listeners.remove(listener);
		
	}
	
	/**
	 * Open the specific object which can be File,URL or InputStream.
	 *
	 * @param object the object
	 * @throws StreamPlayerException the stream player exception
	 */
	public void open(Object object) throws StreamPlayerException {
		
		Main.logger.info("open(" + object + ")\n");
		if (object != null) {
			dataSource = object;
			initAudioInputStream();
		}
	}
	
	/**
	 * Create AudioInputStream and AudioFileFormat from the data source.
	 *
	 * @throws StreamPlayerException the stream player exception
	 */
	private void initAudioInputStream() throws StreamPlayerException {
		try {
			
			Main.logger.info("Entered initAudioInputStream\n");
			
			reset();
			notifyEvent(Status.OPENING, getEncodedStreamPosition(), dataSource);
			
			// Audio resources from file||URL||inputStream.
			initAudioInputStreamPart2();
			
			// Create the Line
			createLine();
			
			// Determine Properties
			determineProperties();
			
			// System out all properties
			// System.out.println(properties.size())
			// properties.keySet().forEach(key -> {
			// System.out.println(key + ":" + properties.get(key));
			// })
			
			status = Status.OPENED;
			notifyEvent(Status.OPENED, getEncodedStreamPosition(), null);
			
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
			Main.logger.log(Level.INFO, ex.getMessage(), ex);
			Platform.runLater(() -> ActionTool.showNotification("Warning", ex.getMessage(), NotificationType.WARNING));
			throw new StreamPlayerException(ex);
		}
		
		Main.logger.info("Exited initAudioInputStream\n");
	}
	
	/**
	 * Audio resources from File||URL||InputStream.
	 *
	 * @throws UnsupportedAudioFileException the unsupported audio file
	 *         exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void initAudioInputStreamPart2() throws UnsupportedAudioFileException , IOException {
		
		Main.logger.info("Entered initAudioInputStreamPart->2\n");
		
		if (dataSource instanceof URL) {
			audioInputStream = AudioSystem.getAudioInputStream((URL) dataSource);
			audioFileFormat = AudioSystem.getAudioFileFormat((URL) dataSource);
			
		} else if (dataSource instanceof File) {
			audioInputStream = AudioSystem.getAudioInputStream((File) dataSource);
			audioFileFormat = AudioSystem.getAudioFileFormat((File) dataSource);
			
		} else if (dataSource instanceof InputStream) {
			audioInputStream = AudioSystem.getAudioInputStream((InputStream) dataSource);
			audioFileFormat = AudioSystem.getAudioFileFormat((InputStream) dataSource);
		}
		
		Main.logger.info("Exited initAudioInputStreamPart->2\n");
		
	}
	
	/**
	 * Determines Properties when the File/URL/InputStream is opened.
	 */
	private void determineProperties() {
		
		Main.logger.info("Entered determineProperties()!\n");
		
		// Add AudioFileFormat properties.
		// Expect if it is null(something bad happened).
		if (audioFileFormat != null) {
			audioProperties = null;
			if (audioFileFormat instanceof TAudioFileFormat) {
				
				// Tritonus SPI compliant audio file format.
				audioProperties = ( (TAudioFileFormat) audioFileFormat ).properties();
				
				// Clone the Map because it is not mutable.
				audioProperties = deepCopy(audioProperties);
				
			} else {
				audioProperties = new HashMap<>();
			}
			
			// Add JavaSound properties.
			if (audioFileFormat.getByteLength() > 0)
				audioProperties.put("audio.length.bytes", audioFileFormat.getByteLength());
			if (audioFileFormat.getFrameLength() > 0)
				audioProperties.put("audio.length.frames", audioFileFormat.getFrameLength());
			if (audioFileFormat.getType() != null)
				audioProperties.put("audio.type", audioFileFormat.getType().toString());
			
			// AudioFormat properties.
			AudioFormat audioFormat = audioFileFormat.getFormat();
			if (audioFormat.getFrameRate() > 0)
				audioProperties.put("audio.framerate.fps", audioFormat.getFrameRate());
			if (audioFormat.getFrameSize() > 0)
				audioProperties.put("audio.framesize.bytes", audioFormat.getFrameSize());
			if (audioFormat.getSampleRate() > 0)
				audioProperties.put("audio.samplerate.hz", audioFormat.getSampleRate());
			if (audioFormat.getSampleSizeInBits() > 0)
				audioProperties.put("audio.samplesize.bits", audioFormat.getSampleSizeInBits());
			if (audioFormat.getChannels() > 0)
				audioProperties.put("audio.channels", audioFormat.getChannels());
			// Tritonus SPI compliant audio format.
			if (audioFormat instanceof TAudioFormat)
				audioProperties.putAll( ( (TAudioFormat) audioFormat ).properties());
			
			// Add SourceDataLine
			audioProperties.put("basicplayer.sourcedataline", sourceDataLine);
			
			// Notify all registered StreamPlayerListeners
			listeners.forEach(listener -> listener.opened(dataSource, audioProperties));
			
			Main.logger.info("Exited determineProperties()!\n");
		}
	}
	
	/**
	 * Initiating Audio resources from AudioSystem.<br>
	 *
	 * @throws LineUnavailableException the line unavailable exception
	 */
	private void initLine() throws LineUnavailableException {
		
		Main.logger.info("Initiating the line...");
		
		if (sourceDataLine == null)
			createLine();
		if (!sourceDataLine.isOpen())
			openLine();
		else {
			AudioFormat lineAudioFormat = sourceDataLine.getFormat();
			AudioFormat audioInputStreamFormat = audioInputStream == null ? null : audioInputStream.getFormat();
			if (!lineAudioFormat.equals(audioInputStreamFormat)) {
				sourceDataLine.close();
				openLine();
			}
		}
	}
	
	/** The frame size. */
	private int frameSize;
	
	/**
	 * Inits a DateLine.<br>
	 * 
	 * From the AudioInputStream, i.e. from the sound file, we fetch information
	 * about the format of the audio data. These information include the
	 * sampling frequency, the number of channels and the size of the samples.
	 * There information are needed to ask JavaSound for a suitable output line
	 * for this audio file. Furthermore, we have to give JavaSound a hint about
	 * how big the internal buffer for the line should be. Here, we say
	 * AudioSystem.NOT_SPECIFIED, signaling that we don't care about the exact
	 * size. JavaSound will use some default value for the buffer size.
	 *
	 * @throws LineUnavailableException the line unavailable exception
	 */
	private void createLine() throws LineUnavailableException {
		
		Main.logger.info("Entered CreateLine()!:\n");
		
		if (sourceDataLine == null) {
			AudioFormat sourceFormat = audioInputStream.getFormat();
			
			Main.logger.info("Create Line : Source format : " + sourceFormat.toString() + "\n");
			
			// Calculate the Sample Size in bits
			int nSampleSizeInBits = sourceFormat.getSampleSizeInBits();
			if (sourceFormat.getEncoding() == AudioFormat.Encoding.ULAW
			        || sourceFormat.getEncoding() == AudioFormat.Encoding.ALAW || nSampleSizeInBits <= 0
			        || nSampleSizeInBits != 8)
				nSampleSizeInBits = 16;
			
			AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(),
			        nSampleSizeInBits, sourceFormat.getChannels(),
			        sourceFormat.getChannels() * ( nSampleSizeInBits / 8 ), sourceFormat.getSampleRate(), false);
			
			frameSize = sourceFormat.getChannels() * ( nSampleSizeInBits / 8 );
			
			Main.logger.info("Sample Rate =" + targetFormat.getSampleRate() + ",Frame Rate="
			        + targetFormat.getFrameRate() + ",Bit Rate=" + targetFormat.getSampleSizeInBits()
			        + "Target format: " + targetFormat + "\n");
			
			// Keep a reference on encoded stream to progress notification.
			encodedAudioInputStream = audioInputStream;
			try {
				// Get total length in bytes of the encoded stream.
				encodedAudioLength = encodedAudioInputStream.available();
			} catch (IOException e) {
				Main.logger.warning("Cannot get m_encodedaudioInputStream.available()\n" + e);
			}
			
			// Create decoded Stream
			audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioInputStream.getFormat(),
			        AudioSystem.NOT_SPECIFIED);
			
			/* Mixer mixer = getMixer(m_mixerName); if (mixer != null) { //
			 * System.out.println("Mixer!=null");
			 * log.info("Mixer : " + mixer.getMixerInfo().toString()); m_line =
			 * (SourceDataLine) mixer.getLine(info);
			 * } else { // System.out.println("Mixer==null"); */
			
			sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
			// mixerName = null
			// }
			// ------------------------------------------
			Main.logger.info("Line : " + sourceDataLine.toString());
			// ------------------------------------------
			Main.logger.info("Line Info : " + sourceDataLine.getLineInfo().toString());
			// ------------------------------------------
			Main.logger.info("Line AudioFormat: " + sourceDataLine.getFormat().toString() + "\n");
			
			Main.logger.info("Exited CREATELINE()!:\n");
		} else {
			Main.logger.warning("Warning Source DataLine is not null!\n");
		}
	}
	
	/**
	 * Open the line.
	 *
	 * @throws LineUnavailableException the line unavailable exception
	 */
	private void openLine() throws LineUnavailableException {
		
		Main.logger.info("Entered OpenLine()!:\n");
		
		if (sourceDataLine != null) {
			AudioFormat audioFormat = audioInputStream.getFormat();
			int bufferSize = ( bufferSize = lineBufferSize ) < 0 ? sourceDataLine.getBufferSize() : bufferSize;
			sourceDataLine.open(audioFormat, currentLineBufferSize = bufferSize);
			
			// opened?
			if (sourceDataLine.isOpen()) {
				Main.logger.info("Open Line Buffer Size=" + bufferSize + "\n");
				
				/*-- Display supported controls --*/
				// Control[] c = m_line.getControls()
				
				// Master_Gain Control?
				if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN))
					gainControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
				else
					gainControl = null;
				
				// PanControl?
				if (sourceDataLine.isControlSupported(FloatControl.Type.PAN))
					panControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.PAN);
				else
					panControl = null;
				
				// SampleRate?
				if (sourceDataLine.isControlSupported(FloatControl.Type.SAMPLE_RATE))
					sampleRateControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.SAMPLE_RATE);
				else
					sampleRateControl = null;
				
				// Mute?
				if (sourceDataLine.isControlSupported(BooleanControl.Type.MUTE))
					muteControl = (BooleanControl) sourceDataLine.getControl(BooleanControl.Type.MUTE);
				else
					muteControl = null;
				
				// Speakers Balance?
				if (sourceDataLine.isControlSupported(FloatControl.Type.BALANCE))
					balanceControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.BALANCE);
				else
					balanceControl = null;
			}
			
		}
		
		Main.logger.info("Exited OpenLine()!:\n");
	}
	
	/**
	 * Stops the play back.<br>
	 *
	 * Player Status = STOPPED.<br>
	 * Thread should free Audio resources.
	 */
	public void stop() {
		if (isPausedOrPlaying()) {
			if (isPlaying())
				pause();
			if (sourceDataLine != null) {
				sourceDataLine.stop();
				sourceDataLine.flush();
			}
			status = Status.STOPPED;
			notifyEvent(Status.STOPPED, getEncodedStreamPosition(), null);
			synchronized (audioInputStream) {
				closeStream();
			}
			Main.logger.info("stopPlayback() completed");
		}
	}
	
	/**
	 * Pauses the play back.<br>
	 * 
	 * Player Status = PAUSED.
	 * * @return False if failed(so simple...)
	 *
	 * @return true, if successful
	 */
	public boolean pause() {
		if (sourceDataLine != null && status == Status.PLAYING) {
			sourceDataLine.stop();
			sourceDataLine.flush();
			status = Status.PAUSED;
			Main.logger.info("pausePlayback() completed");
			notifyEvent(Status.PAUSED, getEncodedStreamPosition(), null);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Resumes the play back.<br>
	 *
	 * Player Status = PLAYING*
	 * 
	 * @return False if failed(so simple...)
	 */
	public boolean resume() {
		if (sourceDataLine != null && status == Status.PAUSED) {
			sourceDataLine.start();
			status = Status.PLAYING;
			Main.logger.info("resumePlayback() completed");
			notifyEvent(Status.RESUMED, getEncodedStreamPosition(), null);
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * Starts the play back.
	 *
	 * @throws StreamPlayerException the stream player exception
	 */
	public void play() throws StreamPlayerException {
		if (status == Status.STOPPED)
			initAudioInputStream();
		if (status == Status.OPENED) {
			if (! ( thread == null || !thread.isAlive() )) {
				Main.logger.info("WARNING: old thread still running!!");
				int counter = 0;
				while (status != Status.OPENED) {
					try {
						if (thread != null) {
							
							Main.logger.info("Waiting ... " + counter);
							counter++;
							Thread.sleep(400);
							if (counter > 2) {
								thread.interrupt();
							}
						}
					} catch (InterruptedException e) {
						throw new StreamPlayerException(StreamPlayerException.PlayerException.WAIT_ERROR, e);
					}
				}
			}
			
			// Open SourceDataLine.
			try {
				initLine();
			} catch (LineUnavailableException e) {
				throw new StreamPlayerException(StreamPlayerException.PlayerException.CAN_NOT_INIT_LINE, e);
			}
			
			// ---- log.info("Creating new thread")
			thread = new Thread(this, "BasicPlayer");
			thread.start();
			if (sourceDataLine != null) {
				sourceDataLine.start();
				status = Status.PLAYING;
				notifyEvent(Status.PLAYING, getEncodedStreamPosition(), null);
			}
		}
	}
	
	/**
	 * Main loop.
	 *
	 * Player Status == STOPPED || SEEKING => End of Thread + Freeing Audio
	 * Resources.<br>
	 * Player Status == PLAYING => Audio stream data sent to Audio line.<br>
	 * Player Status == PAUSED => Waiting for another status.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		int readBytes = 1;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		// Lock stream while playing.
		synchronized (audioInputStream) {
			// Main play/pause loop.
			while ( ( readBytes != -1 )
			        && ! ( status == Status.STOPPED || status == Status.SEEKING || status == Status.UNKNOWN )) {
				if (status == Status.PLAYING) {
					try {
						
						// Reads up a specified maximum number of bytes
						// from audio stream ,putting them into the given
						// byte array
						if ( ( readBytes = audioInputStream.read(abData, 0, abData.length) ) >= 0) {
							
							// Copy data from [ nBytesRead ] to [ PCM ] array
							byte[] pcm = new byte[readBytes];
							System.arraycopy(abData, 0, pcm, 0, readBytes);
							
							// Check for under run
							if (sourceDataLine.available() >= sourceDataLine.getBufferSize())
								Main.logger.info("Underrun :" + sourceDataLine.available() + "/"
								        + sourceDataLine.getBufferSize());
								
							// Write data to mixer via the source data line
							// System.out.println("ReadBytes:" + readBytes + "
							// Line Level:" + sourceDataLine.getLevel()
							// + " Frame Size:" + frameSize);
							if (readBytes % frameSize == 0)
								sourceDataLine.write(abData, 0, readBytes);
							
							// Compute position in bytes in encoded stream.
							int nEncodedBytes = getEncodedStreamPosition();
							
							// Notify all registered Listeners
							listeners.forEach(listener -> {
								if (audioInputStream instanceof PropertiesContainer) {
									// Pass audio parameters such as instant
									// bit rate, ...
									listener.progress(nEncodedBytes, sourceDataLine.getMicrosecondPosition(), pcm,
									        ( (PropertiesContainer) audioInputStream ).properties());
								} else
									listener.progress(nEncodedBytes, sourceDataLine.getMicrosecondPosition(), pcm,
									        emptyMap);
							});
						}
						
					} catch (IOException e) {
						Main.logger.warning("Thread cannot run()\n" + e);
						stop();
						status = Status.STOPPED;
						notifyEvent(Status.STOPPED, getEncodedStreamPosition(), null);
					}
					
				} else if (status == Status.PAUSED) { // Paused
					try {
						while (status == Status.PAUSED) {
							Thread.sleep(200);
						}
					} catch (InterruptedException ex) {
						thread.interrupt();
						Main.logger.warning("Thread cannot sleep.\n" + ex);
					}
				}
			}
			
			// Free audio resources.
			if (sourceDataLine != null) {
				sourceDataLine.drain();
				sourceDataLine.stop();
				sourceDataLine.close();
				sourceDataLine = null;
			}
			
			// Notification of "End Of Media"
			if (readBytes == -1)
				notifyEvent(Status.EOM, getEncodedStreamPosition(), null);
			
			// Close stream.
			closeStream();
		}
		status = Status.STOPPED;
		// stream in closed so not purpose to get EncodedStreamPosition()
		notifyEvent(Status.STOPPED, UNAVAILABLE, null);
		Main.logger.info("Thread completed...");
		
	}
	
	/**
	 * Skip bytes in the File input stream. It will skip N frames matching to
	 * bytes, so it will never skip given bytes length exactly.
	 *
	 * @param bytes the bytes
	 * @return value>0 for File and value=0 for URL and InputStream
	 * @throws StreamPlayerException the stream player exception
	 */
	public long seek(long bytes) throws StreamPlayerException {
		long totalSkipped = 0;
		if (dataSource instanceof File) {
			Main.logger.info("Bytes to skip : " + bytes);
			Status previousStatus = status;
			status = Status.SEEKING;
			long skipped = 0;
			try {
				synchronized (audioInputStream) {
					notifyEvent(Status.SEEKING, UNAVAILABLE, null);
					initAudioInputStream();
					if (audioInputStream != null) {
						
						// Loop until bytes are really skipped.
						while (totalSkipped < ( bytes - SKIP_INACCURACY_SIZE )) {
							skipped = audioInputStream.skip(bytes - totalSkipped);
							if (skipped == 0)
								break;
							totalSkipped = totalSkipped + skipped;
							Main.logger.info("Skipped : " + totalSkipped + "/" + bytes);
							if (totalSkipped == -1)
								throw new StreamPlayerException(
								        StreamPlayerException.PlayerException.SKIP_NOT_SUPPORTED);
							
							Main.logger.info("Skeeping:" + totalSkipped);
						}
					}
				}
				notifyEvent(Status.SEEKED, getEncodedStreamPosition(), null);
				status = Status.OPENED;
				if (previousStatus == Status.PLAYING) {
					play();
				} else if (previousStatus == Status.PAUSED) {
					play();
					pause();
				}
				
			} catch (IOException ex) {
				Main.logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
		return totalSkipped;
	}
	
	/**
	 * Calculates the current position of the encoded audio based on
	 * <br>
	 * <b>nEncodedBytes = encodedAudioLength -
	 * encodedAudioInputStream.available();</b>
	 * 
	 * @return The Position of the encoded stream in term of bytes
	 */
	public int getEncodedStreamPosition() {
		int nEncodedBytes = -1;
		if (dataSource instanceof File && encodedAudioInputStream != null) {
			try {
				nEncodedBytes = encodedAudioLength - encodedAudioInputStream.available();
			} catch (IOException ex) {
				Main.logger.log(Level.WARNING, "Cannot get m_encodedaudioInputStream.available()", ex);
				Platform.runLater(
				        () -> ActionTool.showNotification("Error", ex.getMessage(), NotificationType.WARNING));
				stop();
			}
		}
		return nEncodedBytes;
	}
	
	/**
	 * Close stream.
	 */
	private void closeStream() {
		try {
			if (audioInputStream != null) {
				audioInputStream.close();
				Main.logger.info("Stream closed");
			}
		} catch (IOException e) {
			Main.logger.warning("Cannot close stream\n" + e);
		}
	}
	
	/**
	 * Return SourceDataLine buffer size.
	 * 
	 * @return -1 maximum buffer size.
	 */
	public int getLineBufferSize() {
		return lineBufferSize;
	}
	
	/**
	 * Return SourceDataLine current buffer size.
	 * 
	 * @return The current line buffer size
	 */
	public int getLineCurrentBufferSize() {
		return currentLineBufferSize;
	}
	
	/**
	 * Returns all available mixers.
	 *
	 * @return A List of available Mixers
	 */
	public List<String> getMixers() {
		List<String> mixers = new ArrayList<>();
		
		// Obtains an array of mixer info objects that represents the set of
		// audio mixers that are currently installed on the system.
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		
		if (mixerInfos != null)
			Arrays.stream(mixerInfos).forEach(mInfo -> {
				// line info
				Line.Info lineInfo = new Line.Info(SourceDataLine.class);
				Mixer mixer = AudioSystem.getMixer(mInfo);
				
				// if line supported
				if (mixer.isLineSupported(lineInfo))
					mixers.add(mInfo.getName());
				
			});
		
		return mixers;
	}
	
	/**
	 * Returns the mixer with this name.
	 *
	 * @param name the name
	 * @return The Mixer with that name
	 */
	public Mixer getMixer(String name) {
		Mixer mixer = null;
		
		// Obtains an array of mixer info objects that represents the set of
		// audio mixers that are currently installed on the system.
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		
		if (name != null && mixerInfos != null) {
			for (int i = 0; i < mixerInfos.length; i++)
				if (mixerInfos[i].getName().equals(name)) {
					mixer = AudioSystem.getMixer(mixerInfos[i]);
					break;
				}
		}
		return mixer;
	}
	
	/**
	 * Check if the <b>Control</b> is Supported by m_line.
	 *
	 * @param control the control
	 * @param component the component
	 * @return true, if successful
	 */
	private boolean hasControl(Type control , Control component) {
		
		if (component != null && ( sourceDataLine != null ) && ( sourceDataLine.isControlSupported(control) ))
			return true;
		
		return false;
	}
	
	/**
	 * Returns Gain value.
	 * 
	 * @return The Gain Value
	 */
	public float getGainValue() {
		
		if (hasControl(FloatControl.Type.MASTER_GAIN, gainControl))
			return gainControl.getValue();
		else
			return 0.0F;
	}
	
	/**
	 * Returns maximum Gain value.
	 * 
	 * @return The Maximum Gain Value
	 */
	public float getMaximumGain() {
		if (hasControl(FloatControl.Type.MASTER_GAIN, gainControl))
			return gainControl.getMaximum();
		else
			return 0.0F;
		
	}
	
	/**
	 * Returns minimum Gain value.
	 * 
	 * @return The Minimum Gain Value
	 */
	public float getMinimumGain() {
		
		if (hasControl(FloatControl.Type.MASTER_GAIN, gainControl))
			return gainControl.getMinimum();
		else
			return 0.0F;
		
	}
	
	/**
	 * Returns Pan precision.
	 * 
	 * @return The Precision Value
	 */
	public float getPrecision() {
		if (hasControl(FloatControl.Type.PAN, panControl))
			return panControl.getPrecision();
		else
			return 0.0F;
		
	}
	
	/**
	 * Returns Pan value.
	 * 
	 * @return The Pan Value
	 */
	public float getPan() {
		if (hasControl(FloatControl.Type.PAN, panControl))
			return panControl.getValue();
		else
			return 0.0F;
		
	}
	
	/**
	 * Return the mute Value(true || false).
	 *
	 * @return True if muted , False if not
	 */
	public boolean getMute() {
		if (hasControl(BooleanControl.Type.MUTE, muteControl))
			return muteControl.getValue();
		
		return false;
	}
	
	/**
	 * Return the balance Value.
	 *
	 * @return The Balance Value
	 */
	public float getBalance() {
		if (hasControl(FloatControl.Type.BALANCE, balanceControl))
			return balanceControl.getValue();
		
		return 0f;
	}
	
	/****
	 * Return the total size of this file in bytes.
	 * 
	 * @return encodedAudioLength
	 */
	public long getTotalBytes() {
		return encodedAudioLength;
	}
	
	/**
	 * Gets the source data line.
	 *
	 * @return The SourceDataLine
	 */
	public SourceDataLine getSourceDataLine() {
		return sourceDataLine;
	}
	
	/**
	 * Deep copy of a Map.
	 *
	 * @param src the src
	 * @return the map
	 */
	protected Map<String,Object> deepCopy(Map<String,Object> src) {
		HashMap<String,Object> map = new HashMap<>();
		if (src != null)
			src.keySet().forEach(key -> map.put(key, src.get(key)));
		return map;
	}
	
	/**
	 * Set SourceDataLine buffer size. It affects audio latency. (the delay
	 * between line.write(data) and real sound). Minimum value should be over
	 * 10000 bytes.
	 * 
	 * @param size
	 *        -1 means maximum buffer size available.
	 */
	public void setLineBufferSize(int size) {
		lineBufferSize = size;
	}
	
	/**
	 * Sets Pan value. Line should be opened before calling this method. Linear
	 * scale : -1.0 <--> +1.0
	 *
	 * @param fPan the new pan
	 */
	public void setPan(double fPan) {
		
		if (hasControl(FloatControl.Type.PAN, panControl) && fPan >= -1.0 && fPan <= 1.0) {
			Main.logger.info("Pan : " + fPan);
			panControl.setValue((float) fPan);
			notifyEvent(Status.PAN, getEncodedStreamPosition(), null);
		}
		
	}
	
	/**
	 * Sets Gain value. Line should be opened before calling this method. Linear
	 * scale 0.0 <--> 1.0 Threshold Coef. : 1/2 to avoid saturation.
	 *
	 * @param fGain the new gain
	 */
	public void setGain(double fGain) {
		if (isPlaying() || isPaused())
			if (hasControl(FloatControl.Type.MASTER_GAIN, gainControl)) {
				/* //Main.logger.info("Gain : " + fGain); // double minGainDB =
				 * getMinimumGain(); // double ampGainDB = ((10.0f / 20.0f) *
				 * getMaximumGain()) - // getMinimumGain(); // double cste =
				 * Math.log(10.0) / 20; // double valueDB = minGainDB + (1 /
				 * cste) *
				 * Math.log(1 + // (Math.exp(cste * ampGainDB) - 1) * fGain); //
				 * log.debug("Gain : " + valueDB); //
				 * m_gainControl.setValue((float)
				 * valueDB); */
				
				// Better type
				gainControl.setValue((float) ( 20 * Math.log10(fGain == 0.0 ? 0.0000 : fGain) ));
				// OR (Math.log(fGain == 0.0 ? 0.0000 : fGain) / Math.log(10.0))
				notifyEvent(Status.GAIN, getEncodedStreamPosition(), null);
			}
		
	}
	
	/**
	 * Set the mute of the Line. Note that mute status does not affect gain.
	 *
	 * @param mute the new mute
	 */
	public void setMute(boolean mute) {
		if (hasControl(BooleanControl.Type.MUTE, muteControl) && muteControl.getValue() != mute)
			muteControl.setValue(mute);
	}
	
	/**
	 * Represents a control for the relative balance of a stereo signal between
	 * two stereo speakers. The valid range of values is -1.0 (left channel
	 * only) to 1.0 (right channel only). The default is 0.0 (centered).
	 *
	 * @param fBalance the new balance
	 */
	public void setBalance(float fBalance) {
		if (hasControl(FloatControl.Type.BALANCE, balanceControl) && fBalance >= -1.0 && fBalance <= 1.0)
			balanceControl.setValue(fBalance);
		else
			try {
				throw new StreamPlayerException(StreamPlayerException.PlayerException.BALANCE_CONTROL_NOT_SUPPORTED);
			} catch (StreamPlayerException ex) {
				Main.logger.log(Level.WARNING, ex.getMessage(), ex);
			}
	}
	
	/**
	 * Changes specific values from equalizer.
	 *
	 * @param array the array
	 * @param stop the stop
	 */
	public void setEqualizer(float[] array , int stop) {
		if (isPausedOrPlaying() && audioInputStream instanceof PropertiesContainer) {
			Map<?,?> map = ( (PropertiesContainer) audioInputStream ).properties();
			float[] equalizer = (float[]) map.get("mp3.equalizer");
			for (int i = 0; i < stop; i++)
				equalizer[i] = array[i];
		}
		
	}
	
	/**
	 * Changes a value from equalizer.
	 *
	 * @param value the value
	 * @param key the key
	 */
	public void setEqualizerKey(float value , int key) {
		if (isPausedOrPlaying() && audioInputStream instanceof PropertiesContainer) {
			Map<?,?> map = ( (PropertiesContainer) audioInputStream ).properties();
			float[] equalizer = (float[]) map.get("mp3.equalizer");
			equalizer[key] = value;
		}
		
	}
	
	/**
	 * Checks if is unknown.
	 *
	 * @return If Status==STATUS.UNKNOWN.
	 */
	public boolean isUnknown() {
		return status == Status.UNKNOWN;
	}
	
	/**
	 * Checks if is playing.
	 *
	 * @return <b>true</b> if player is playing ,<b>false</b> if not.
	 */
	public boolean isPlaying() {
		return status == Status.PLAYING;
	}
	
	/**
	 * Checks if is paused.
	 *
	 * @return <b>true</b> if player is paused ,<b>false</b> if not.
	 */
	public boolean isPaused() {
		return status == Status.PAUSED;
	}
	
	/**
	 * Checks if is paused or playing.
	 *
	 * @return <b>true</b> if player is paused/playing,<b>false</b> if not
	 */
	public boolean isPausedOrPlaying() {
		
		if (isPlaying() || isPaused())
			return true;
		
		return false;
	}
	
	/**
	 * Checks if is stopped.
	 *
	 * @return <b>true</b> if player is stopped ,<b>false</b> if not
	 */
	public boolean isStopped() {
		return status == Status.STOPPED;
	}
	
	/**
	 * Checks if is opened.
	 *
	 * @return <b>true</b> if player is opened ,<b>false</b> if not
	 */
	public boolean isOpened() {
		return status == Status.OPENED;
	}
	
	/**
	 * Checks if is seeking.
	 *
	 * @return <b>true</b> if player is seeking ,<b>false</b> if not
	 */
	public boolean isSeeking() {
		return status == Status.SEEKING;
	}
	
}