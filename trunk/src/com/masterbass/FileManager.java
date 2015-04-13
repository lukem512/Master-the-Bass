package com.masterbass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.util.Log;

public class FileManager {
		private final static String logTag = "FileManager";
		private final static String defaultName = "unknown";
		
		private File file;
	
		/* Constructor */
	
		public FileManager() {
			file = null;
		}
		
		/* I/O file functions */
		
		public boolean openFile(String name) {
			file = new File(name);
			createDirectoryStructure(file);
			return true;
		}
		
		public boolean openFile(String path, String name) {
			return openFile(path+"/"+name);
		}
		
		public void closeFile() {
			file = null;
		}
		
		public void deleteFile() {
			if (file != null) {
				file.delete();
			}
		}
		
		public String getName() {
			if (file != null) {
				return file.getName();
			} else {
				return defaultName;
			}
		}
		
		public String getPath() {
			if (file != null) {
				return file.getPath();
			} else {
				return FileManager.getSDPath() + "/MasterTheBass";
			}
		}
		
		public short[] readBinaryFile() throws IOException {
			if (file != null) {
				byte[] data = readBinaryFileFromHandle(file);
				return byteToShortArray(data);
			} else {
				return null;
			}
		}
		
		public boolean writeBinaryFile(short[] data) {
			if (file != null) {
				return writeBinaryFileToHandle(file, shortToByteArray(data), 0);
			} else {
				return false;
			}
		}
		
		public boolean writeBinaryFile(short[] data, int offsetInBytes) {
			if (file != null) {
				return writeBinaryFileToHandle(file, shortToByteArray(data), offsetInBytes);
			} else {
				return false;
			}
		}
		
		public boolean appendBinaryFile(short[] data) {
			if (file != null) {
				return appendBinaryFileToHandle(file, shortToByteArray(data));
			} else {
				return false;
			}
		}
		
		/* Static methods */
		
		/* Read binary file functions */
	
		public static byte[] readBinaryFile(String filename) throws IOException {
			File fp = new File(filename);
			return readBinaryFileFromHandle(fp);
		}
		
		private static byte[] readBinaryFileFromHandle(File handle) throws IOException {
			int len = (int) handle.length();
			FileInputStream fis = new FileInputStream(handle);
			return pumpBinaryFile(fis, len);
		}
		
		private static void createDirectoryStructure (File handle) {
			File parent = handle.getParentFile();
			
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		
		private static byte[] pumpBinaryFile(InputStream in, int size) throws IOException {
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
		
		private static byte[] shortToByteArray(short[] data) {
			byte[] byteData = new byte[2*data.length];
			int i = 0;
			
			for (short s : data) {
				byteData[i++] = (byte) (s & 0x00ff);
				byteData[i++] = (byte) ((s & 0xff00) >>> 8);
			}
			
			return byteData;
		}
		
		private static short[] byteToShortArray(byte[] data) {
			if (data.length % 2 != 0) {
				throw new IllegalArgumentException ("Byte array must be even in length");
			}
			
			short[] shortData = new short[data.length/2];
			int j = 0;
			
			for (int i = 0; i < shortData.length; i++) {
				shortData[i] = (short)((data[j] & 0xFF) | data[j+1]<<8);
				j = j+2;
			}
			
			return shortData;
		}
		 
		public static boolean writeBinaryFile(String path, String filename, short[] data) {
			return writeBinaryFile(path, filename, shortToByteArray(data), 0);
		}
		
		public static boolean writeBinaryFile(String path, String filename, byte[] data) {
			return writeBinaryFile(path, filename, data, 0);
		}
		
		public static boolean writeBinaryFile(String path, String filename, short[] data, int offsetInBytes) {
			return writeBinaryFile(path, filename, shortToByteArray(data), offsetInBytes);
		}
		
		public static boolean writeBinaryFile(String path, String filename, byte[] data, int offset) {
			File fp;
			
			try {
				fp = new File(path, filename);
			} catch (NullPointerException e) {
				Log.w(logTag+".writeBinaryFile", "Null filename supplied.");
				e.printStackTrace();
				return false;
			} 
			
			return writeBinaryFileToHandle(fp, data, offset);
		}
		
		private static boolean writeBinaryFileToHandle(File handle, byte[] data, int offset) {
			FileOutputStream fs;
			
			try {
				fs = new FileOutputStream(handle);
			} catch (FileNotFoundException e) {
				Log.w(logTag+".writeBinaryFile", "Could not open file "+handle.getAbsolutePath());
				e.printStackTrace();
				return false;
			}
			
			try {
				fs.write(data, offset, data.length);
				fs.close();
			} catch (IOException e) {
				Log.w(logTag+".writeBinaryFile", "Could not write to file " + handle.getAbsolutePath());
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		public static boolean appendBinaryFile(String path, String filename, short[] data) {
			return appendBinaryFile(path, filename, shortToByteArray(data));
		}
		
		public static boolean appendBinaryFile(String path, String filename, byte[] data) {			
			File fp;
			
			try {
				fp = new File(path, filename);
			} catch (NullPointerException e) {
				Log.w(logTag+".appendBinaryFile", "Null filename supplied.");
				e.printStackTrace();
				return false;
			} 
			
			return appendBinaryFileToHandle(fp, data);
		}
		
		private static boolean appendBinaryFileToHandle(File handle, byte[] data) {			
			FileOutputStream fs;
			
			try {
				fs = new FileOutputStream(handle, true);
			} catch (FileNotFoundException e) {
				Log.w(logTag+".appendBinaryFile", "Could not open file "+handle.getAbsolutePath());
				e.printStackTrace();
				return false;
			}
			
			try {
				Log.d(logTag+".appendBinaryFile", "Attempting to append data of length " + data.length);
				fs.write(data, 0, data.length);
				fs.close();
			} catch (IOException e) {
				Log.w(logTag+".appendBinaryFile", "Could not write to file " + handle.getAbsolutePath());
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		public static String getSDPath() {
			return Environment.getExternalStorageDirectory().getPath();
		}
}