package com.example.soundtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {
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
}
