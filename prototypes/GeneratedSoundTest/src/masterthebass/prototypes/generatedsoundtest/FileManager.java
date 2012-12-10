package masterthebass.prototypes.generatedsoundtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.util.Log;

public class FileManager {
		private String logTag = "FileManager";
	
		/* Constructor */
	
		public FileManager() {
			// Do nothing
		}
	
		/* Read binary file functions */
	
		public byte[] readBinaryFile(String filename) throws IOException {
			File fp = new File(filename);
			int fpLen = (int) fp.length();
			FileInputStream fis = new FileInputStream(fp);
			return pumpBinaryFile(fis, fpLen);
		}
		
		private byte[] pumpBinaryFile(InputStream in, int size) throws IOException {
			int bufferSize = 1024, done = 0;
		    byte[] buffer = new byte[bufferSize];
		    byte[] result = new byte[size];
		    
		    while (done < size) {
		        int read = in.read(buffer);
		        if (read == -1) {
		            throw new IOException("Something went horribly wrong");
		        }
		        System.arraycopy(buffer, 0, result, done, read);
		        done += read;
		    }
		    
		    in.close();
		    
		    return result;
		}
		
		/* Write binary file function */
		
		public boolean writeBinaryFile(String path, String filename, byte[] data) {
			return writeBinaryFile(path, filename, data, 0);
		}
		
		public boolean writeBinaryFile(String path, String filename, byte[] data, int offset) {
			File fp;
			FileOutputStream fs;
			
			try {
				fp = new File(path, filename);
			} catch (NullPointerException e) {
				Log.w(logTag+".writeBinaryFile", "Null filename supplied.");
				e.printStackTrace();
				return false;
			} 
			
			try {
				fs = new FileOutputStream(new File(path, filename));
			} catch (FileNotFoundException e) {
				Log.w(logTag+".writeBinaryFile", "Could not open file "+path+"/"+filename);
				e.printStackTrace();
				return false;
			}
			
			try {
				Log.d(logTag+".writeBinaryFile", "Attempting to write data of length " + data.length + " bytes to file at offset " + offset);
				fs.write(data, offset, data.length);
				fs.close();
			} catch (IOException e) {
				Log.w(logTag+".writeBinaryFile", "Could not write to file " + fp.getAbsolutePath());
				e.printStackTrace();
				return false;
			}
			
			Log.d(logTag+".writeBinaryFile", "Wrote " + data.length + " bytes to " + fp.getAbsolutePath());
			
			return true;
		}
		
		public boolean appendBinaryFile(String path, String filename, byte[] data) {			
			File fp;
			FileOutputStream fs;
			
			try {
				fp = new File(path, filename);
			} catch (NullPointerException e) {
				Log.w(logTag+".appendBinaryFile", "Null filename supplied.");
				e.printStackTrace();
				return false;
			} 
			
			try {
				fs = new FileOutputStream(new File(path, filename), true);
			} catch (FileNotFoundException e) {
				Log.w(logTag+".appendBinaryFile", "Could not open file "+path+"/"+filename);
				e.printStackTrace();
				return false;
			}
			
			try {
				Log.d(logTag+".appendBinaryFile", "Attempting to append data of length " + data.length);
				fs.write(data, 0, data.length);
				fs.close();
			} catch (IOException e) {
				Log.w(logTag+".appendBinaryFile", "Could not write to file " + fp.getAbsolutePath());
				e.printStackTrace();
				return false;
			}
			
			Log.d(logTag+".appendBinaryFile", "Wrote " + data.length + " bytes to " + fp.getAbsolutePath());
			
			return true;
		}
		
		/* Static functions */
		
		public static String getSDPath() {
			return Environment.getExternalStorageDirectory().getPath();
		}
}
