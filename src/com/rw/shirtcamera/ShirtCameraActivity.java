package com.rw.shirtcamera;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.FrameLayout;

public class ShirtCameraActivity extends Activity {
	public String most_recent_video_filename = "";
	boolean isRecording = false;
	static String TAG = "tag";

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		captureButton.setText();
		if(keyCode == 79) {
			handleCameraState();
		}
		return true;
	}
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.d("LOG! ", e.getMessage());
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	Button captureButton;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Create an instance of Camera
		mCamera = getCameraInstance();
//		CameraInfo info;
//		info = new android.hardware.Camera.CameraInfo();
//
//		//int degrees = (info.orientation + 180 ) % 360;
////	      android.hardware.Camera.getCameraInfo(mCamera.id, info);
////	      orientation = (orientation + 45) / 90 * 90;
////	      int rotation = 0;
////	      if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
////	          rotation = (info.orientation - orientation + 360) % 360;
////	      } else {  // back-facing camera
////	          rotation = (info.orientation + orientation) % 360;
////	      }
		Camera.Parameters mParameters = mCamera.getParameters();
//		mParameters.setRotation((info.orientation + 90) % 360);
//		mParameters.set("orientation", "portrait");
//		mCamera.setParameters(mParameters);
//		mParameters.set("rotation", 90);
		mParameters.setZoom(0);
		mCamera.setParameters(mParameters);
		
		mCamera.setDisplayOrientation(90);
//		mCamera.Parameters.setRotation(90);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		// Add a listener to the Capture button
		captureButton = (Button) findViewById(R.id.button_capture);
//		captureButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {handleCameraState();}
//		});
	}
		public void handleCameraState() {
			if (isRecording) {
				// stop recording and release camera
				mMediaRecorder.stop(); // stop the recording
				releaseMediaRecorder(); // release the MediaRecorder object
				mCamera.lock(); // take camera access back from
				// MediaRecorder

				// inform the user that recording has stopped
				// setCaptureButtonText("Capture");
				isRecording = false;
				captureButton.setText("Uploading");
				// byte[] data =

				final FileInputStream fIn;
				String send_result = "no result yet";
				try {
					fIn = new FileInputStream(most_recent_video_filename);
					
					// spawn bg thread
					  new Thread(new Runnable() {
							public void run() {
								sendFile(fIn);
							}
					  }).start();


				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					send_result = e.getMessage();
				}

				captureButton.setText("Uploaded [" + send_result
						+ "]. Not recording");
			} else {
				// initialize video camera
				if (prepareVideoRecorder()) {
					// Camera is available and unlocked, MediaRecorder is
					// prepared,
					// now you can start recording
					mMediaRecorder.start();

					// inform the user that recording has started
					// setCaptureButtonText("Stop");
					isRecording = true;
					captureButton.setText("Recording");
					CameraInfo info;
//					info = new android.hardware.Camera.CameraInfo();
//					captureButton.setText(Integer.toString(info.orientation));

				} else {
					// prepare didn't work, release the camera
					releaseCamera();
					// inform user
					captureButton.setText("Prepare failed");
				}
			}

		}
	


	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	// private static Uri getOutputMediaFileUri(int type){
	// return Uri.fromFile(getOutputMediaFile(type));
	// }

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		Log.d("dir: ", mediaStorageDir.toString());
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		Log.d(TAG, Integer.toString(type));

		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	private boolean prepareVideoRecorder() {

		// mCamera = getCameraInstance();

		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_LOW));
		mMediaRecorder.setOrientationHint(90);
		// Step 4: Set output file
		most_recent_video_filename = getOutputMediaFile(MEDIA_TYPE_VIDEO)
				.toString();
		mMediaRecorder.setOutputFile(most_recent_video_filename);

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
								// first
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	public String sendFile(FileInputStream fIn) {
		// InetSocketAddress dst_addr = new InetSocketAddress("192.168.2.4",
		// 65001);
		try {
			Socket out_sock = new Socket("192.168.201.53", 65001);
			//InputStreamReader isr = new InputStreamReader(file_source);
			BufferedInputStream br = new BufferedInputStream(fIn);
			GZIPOutputStream gz_out = new GZIPOutputStream(out_sock.getOutputStream());
			DataOutputStream os = new DataOutputStream(gz_out);
		//	BufferedWriter out_stream = new BufferedWriter(out_sock.getOutputStream(), 8192);
			// int i = 0;
			// for(i = 0; i < 10; i++) {
			int sum = 0;
			int readFile;
//			char[] char_buffer = new char[8192];
			byte[] byte_buffer = new byte[8192];

			while ((readFile = br.read(byte_buffer, 0, 8192)) != -1) {
				//for(int i = 0; i < readFile; i++) byte_buffer[i] = (byte) char_buffer[i];
				os.write(byte_buffer, 0, readFile);
				os.flush();
				sum += readFile;
			}
			System.out.println(sum);
			br.close();
			// s.shutdownOutput()
			os.flush();
			os.close();
			// br.readLine();
			// fileTransferTime = System.currentTimeMillis() - fileTransferTime;
			// System.out.println("File transfer time: " + fileTransferTime +
			// " ms");
			// s.close();
			os.flush();
			out_sock.close();
			return "success";

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.d("CONN", e.getMessage());
			return e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("CONN", e.getMessage());
			return e.getMessage();
		}

		// dst_addr.getOutputStream();
	}

}