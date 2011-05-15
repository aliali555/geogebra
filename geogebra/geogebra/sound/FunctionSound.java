package geogebra.sound;

import geogebra.kernel.GeoFunction;
import geogebra.main.Application;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Class for playing function-generated sounds.
 * @author G. Sturr
 *
 */
public final class FunctionSound implements LineListener {

	private Application app;
	
	// threaded class to play function
	private SoundThread soundThread;
	
	// streaming audio fields
	private static  AudioFormat af;
	private static SourceDataLine sdl;   
	
	// audio parameter fields
	private static final int DEFAULT_SAMPLE_RATE = 8000;
	private static final int DEFAULT_BIT_RATE = 8;
	private int bitDepth;
	private int sampleRate;
	private int maxVolume;
	
	// sound function fields
	private  volatile GeoFunction f;
	private double min;
	private double max;
	private double t;  // records current time, used with pause/resume
	
	

	/**
	 * Constructs instance of FunctionSound
	 * @throws Exception
	 */
	public FunctionSound(Application app) throws Exception { 

		this.app = app;
		bitDepth = DEFAULT_BIT_RATE;
		sampleRate = DEFAULT_SAMPLE_RATE;
		// set maximum volume to 50%  (possible range is 0 - 100)
		maxVolume = 50;  
		if(!initStreamingAudio(sampleRate, bitDepth)){
			throw new Exception("Cannot initialize streaming audio");
		}
	}

	/**
	 * Initializes instances of AudioFormat and SourceDataLine 
	 * 
	 * @param sampleRate = 8000, 16000, 11025, 16000, 22050, or 44100
	 * @param bitDepth = 8 or 16
	 * @return
	 */
	private boolean initStreamingAudio(int sampleRate, int bitDepth){

		if(sampleRate != 8000 && sampleRate != 16000 && sampleRate != 11025 
				&& sampleRate != 22050 && sampleRate != 44100)
			return false;
		if(bitDepth != 8 && bitDepth != 16)
			return false;

		boolean success = true;
		this.sampleRate = sampleRate;
		this.bitDepth = bitDepth;

		af = new AudioFormat(sampleRate, bitDepth,1,true,true);
		try {
			sdl = AudioSystem.getSourceDataLine(af);
			// add listener when debugging
			// sdl.addLineListener(this);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	
	/**
	 * Plays a sound generated by the time valued GeoFunction f(t), from t = min
	 * to t = max in seconds. The function is assumed to have range [-1,1] and
	 * will be clipped to this range otherwise.
	 * 
	 * @param geoFunction
	 * @param min
	 * @param max
	 */
	public void playFunction( GeoFunction geoFunction, double min, double max){
		playFunction(geoFunction, min, max, DEFAULT_SAMPLE_RATE, DEFAULT_BIT_RATE);
	}

	
	/**
	 * Plays a sound generated by the time valued GeoFunction f(t), from t = min
	 * to t = max in seconds. The function is assumed to have range [-1,1] and
	 * will be clipped to this range otherwise.
	 * 
	 * @param geoFunction
	 * @param min
	 * @param max
	 * @param sampleRate
	 * @param bitDepth
	 */
	public void playFunction(final GeoFunction geoFunction, final double min, final double max, final int sampleRate, final int bitDepth){
		f =  geoFunction;
		this.min = min;
		this.max = max;
		if(sampleRate != DEFAULT_SAMPLE_RATE  || bitDepth != DEFAULT_BIT_RATE )
			if(!initStreamingAudio(sampleRate, bitDepth))
				return;

		// close current sound thread and prepare sdl
		if(soundThread != null){
			soundThread.interrupt();
			sdl.flush();
			sdl.close();
		}
		
		// spawn a new SoundThread to play the function sound
		soundThread = new SoundThread();
		soundThread.start();

	}

	/**
	 * Pauses/resumes sound generation
	 * @param doPause
	 */
	public void pause(boolean doPause){

		if(doPause){
			min = t;
			soundThread.stopSound();
		}else{
			playFunction(f, min, max, sampleRate, bitDepth);
		}
	}

	
	
	/**
	 * Listener for line events, used for debugging.
	 */
	public void update(LineEvent le) {

		LineEvent.Type type = le.getType();
		
		if (type == LineEvent.Type.OPEN) {
			System.out.println("OPEN");
		} else if (type == LineEvent.Type.CLOSE) {
			System.out.println("CLOSE");
		} else if (type == LineEvent.Type.START) {
			System.out.println("START");
		} else if (type == LineEvent.Type.STOP) {
			System.out.println("STOP");
		}
	}


	
	
	
	
	/**********************************************************
	 * Class SoundThread
	 * 
	 * Plays sounds from time-valued functions.
	 *********************************************************/
	private class SoundThread extends Thread{

		private volatile boolean stopped = false;
		private double samplePeriod;
		private byte[] buf;

		public SoundThread(){
		}

		public void run() {
			generateFunctionSound();
		}

		private void generateFunctionSound(){
			
			stopped = false;
			
			//time between samples
			samplePeriod = 1.0 / sampleRate;

			// create internal buffer for mathematically generated sound data
			// a small buffer minimizes latency when the function changes dynamically
			// TODO: find optimal buffer size
			int frameSetSize = sampleRate/50;  // 20ms ok?
			if(bitDepth == 8)
				buf = new byte[frameSetSize];
			else
				buf = new byte[2*frameSetSize];

			// generate the function sound
			try {
				
				// open the sourceDataLine 
				// TODO: the sdl buffer size is relative to our internal buffer
				// need to experiment for best sizing factor
				sdl.open(af, 10*buf.length);
				sdl.start();

				if(bitDepth == 16) {
					t = min;
					loadBuffer16(t);
					doFade(buf[0], false);
					sdl.write(buf,0,buf.length);
					do {	
						t = t + samplePeriod * frameSetSize;
						loadBuffer16(t);
						sdl.write(buf,0,buf.length);
					} while(t < max && !stopped);
					
					doFade(buf[buf.length-1], true);

				} else {  
					// use 8-bit samples
					t = min;
					loadBuffer8(t);
					doFade(buf[0], false);
					sdl.write(buf,0,buf.length);
					do {
						t = t + samplePeriod * frameSetSize;
						loadBuffer8(t);
						sdl.write(buf,0,buf.length);
					}while(t < max && !stopped);
					
					if(!stopped)
					doFade(buf[buf.length-1], true);
					
				}

				// finish transfer of bytes from internal buffer to the sdl buffer
				sdl.drain();
				
				// stop and close the sourceDataLine
				sdl.stop(); 
				sdl.close();
				
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}

		}

		
		/**
		 * Fills the internal buffer with sound data generated by time-valued
		 * GeoFunction f(t) starting at time t. 
		 * Uses 8-bit mono samples.
		 * 
		 * @param t
		 */
		private void loadBuffer8(double t){
			double value;
			for(int k = 0; k < buf.length; k++){				
				value = f.evaluate(t + 1.0*k*samplePeriod);
				// clip sound data
				if(value > 1.0) value = 1.0;
				if(value < -1.0) value = -1.0;
				
				buf[k]=(byte)(value*maxVolume);	
			}
		}


		/**
		 * Fills the internal buffer with sound data generated by time-valued
		 * GeoFunction f(t) starting at time t. 
		 * Uses 16-bit mono, signed, big-endian samples.
		 * 
		 * @param t
		 */
		private void loadBuffer16(double t){
			double value;
			for(int k = 0; k < buf.length/2; k++){				
				value = f.evaluate(t + 1.0*k*samplePeriod);
				// clip sound data
				if(value > 1.0) value = 1.0;
				if(value < -1.0) value = -1.0;
				
				short sample = (short) (value*maxVolume);					 
				buf[2*k] = (byte)(sample & 0xff);
				buf[2*k+1] = (byte)((sample >> 8) & 0xff);
			}
		}

		/**
		 * Shapes ends of waveform to fade sound data
		 * TODO: is this actually working?  
		 * @param peakValue
		 * @param isFadeOut
		 */
		private void doFade(short peakValue, boolean isFadeOut){

			int numSamples = sampleRate/100;	
			byte[] fadeBuf = new byte[bitDepth == 8? numSamples : 2*numSamples];

			double delta = 1.0*peakValue/numSamples;
			if(isFadeOut)
				delta = -delta;

			short value = isFadeOut? peakValue: 0;

		//	System.out.println("peak: " + peakValue);
		//	System.out.println("delta: " + delta);

			for(int k = 0; k < numSamples; k++){
				if(bitDepth == 8){
					fadeBuf[k]=(byte)(value);
				}else{
					fadeBuf[2*k] = (byte)(value & 0xff);
					fadeBuf[2*k+1] = (byte)((value >> 8) & 0xff);
				}

				value += delta;
				//System.out.println(value);
			}

			sdl.write(fadeBuf,0,fadeBuf.length);	
		}


		/**
		 * Stops function sound 
		 */
		public void stopSound() {
			stopped = true;		
		}

	}
	//================================
	// END SoundThread class

	
}


