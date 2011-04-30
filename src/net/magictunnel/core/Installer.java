package net.magictunnel.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Installer {
	public static final String INSTALL_SCRIPT = "install.sh";
	public static final String TARGET_PARTITION = "/system";
	public static final String DNS_TUNNEL_LOCALFILE = "iodine";
	public static final String DNS_TUNNEL_FILE = "/system/bin/iodine";
	public static final String DNS_TUNNEL_ASSET = "iodine";
	
	private AssetManager m_assets;
	private Context m_context;
	
	public Installer(Context context) {
		m_assets = context.getAssets();
		m_context = context;
	}
	
	private void copyFile(OutputStream out, InputStream in) throws IOException {
		byte buffer[] = new byte[512];
		int count = 0;
		while((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
	}
	
	private boolean installFile(String sourceAsset, String destFile) {
		InputStream inputStream;
		
		try {
			inputStream = m_assets.open(sourceAsset);
			FileOutputStream outputStream = m_context.openFileOutput(destFile, Context.MODE_PRIVATE);
			copyFile(outputStream, inputStream);
			outputStream.close();
			inputStream.close();
			return true;
		} catch (IOException e) {
			Log.e(Installer.class.toString(), e.getMessage());
			return false;
		}
	}
	
	public boolean generateInstallScript(String partition, String source, String dest) {
		try {
			FileOutputStream fos = m_context.openFileOutput(INSTALL_SCRIPT, Context.MODE_PRIVATE);
			PrintWriter script = new PrintWriter(fos);
			
			File privateDir = m_context.getFilesDir();
			File absSource = new File(privateDir, source);
			 
			//Remount read-write
			Partition p = new Partition();
			String mountCommand = p.remountPartition(partition, false);			
			script.println(mountCommand);
			
			//Copy the file
			script.println("cp " + absSource.toString() + " " + dest);
			
			//Add executable permission
			script.println("chmod +x " + dest);
			
			//Remount read-only
			mountCommand = p.remountPartition(partition, true);			
			script.println(mountCommand);			
			
			script.close();
			return true;
		}catch(Exception e) {
			return false;
		}		
	}
	
	static public boolean iodineInstalled() {
		File file = new File(DNS_TUNNEL_FILE);
		return file.exists();	
	}
	
	public boolean installIodine() {
		if (iodineInstalled()) {
			return true;
		}

		//Copy the file to private storage
		if (!installFile(DNS_TUNNEL_ASSET, DNS_TUNNEL_LOCALFILE)) {
			return false;
		}
		
		//Generate a shell script that will copy the file
		//to the root partition
		if (!generateInstallScript(TARGET_PARTITION, DNS_TUNNEL_LOCALFILE, DNS_TUNNEL_FILE)) {
			return false;
		}
		
		File script = new File(m_context.getFilesDir(), INSTALL_SCRIPT);
		return Commands.runScriptAsRoot(script.toString());		
	}	
}
