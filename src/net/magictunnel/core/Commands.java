package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.util.Log;

public class Commands {
	public static final String SU = "su";
	public static final String PATH = "PATH";
	
	public static boolean checkRoot() {
		String paths = System.getenv(PATH);
		String [] pathComponents = paths.split(":");
		
		for (String path:pathComponents) {
			File f = new File(path, SU);
			if (f.exists()) {
				return true;
			}
		}
		return false;
	}

	
	public static boolean runCommand(String command) {
		try {
		    Process p = Runtime.getRuntime().exec(command);
		    return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean runCommand(String command, String [] env) {
		try {
		    Process p = Runtime.getRuntime().exec(command, env);
		    return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean runScriptAsRoot(String scriptFile) {
		OutputStreamWriter osw = null;
		StringBuilder sbstdOut = new StringBuilder();
	    StringBuilder sbstdErr = new StringBuilder();
	    Process proc=null;
	    Runtime runtime = Runtime.getRuntime();
	    
	    try {
	        proc = runtime.exec("su");
	        osw = new OutputStreamWriter(proc.getOutputStream());
	        osw.write("sh " + scriptFile);
	        osw.flush();
	        osw.close();
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } finally {
	        if (osw != null) {
	            try {
	                osw.close();
	            } catch (IOException e) {
	                e.printStackTrace();                    
	            }
	        }
	    }
	    try {
	        if (proc != null)
	            proc.waitFor();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	    sbstdOut.append(new BufferedReader(new InputStreamReader(proc.getInputStream())));
	    sbstdErr.append(new BufferedReader(new InputStreamReader(proc.getErrorStream())));
	
	    Log.i("Commands", sbstdOut.toString());
	    Log.i("Commands", sbstdErr.toString());
	    
	    return true;
	//	return runCommand(SU + " -c \"sh " + scriptFile + "\"" );
	}
	
	public static boolean requestRoot() {
		int uid = android.os.Process.myUid(); 
		if (uid == 0) {
			return true;
		}
		
		if (runCommand(SU) == false) {
			return false;
		}
		
		uid = android.os.Process.myUid();
		return uid == 0;
	}
}
