package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.util.Log;

public class Commands {
	public static final String SU = "su";
	public static final String PATH = "PATH";

	Runtime m_runtime = Runtime.getRuntime();
	StringBuilder m_stdOut = new StringBuilder();
	StringBuilder m_stdErr = new StringBuilder();
	OutputStreamWriter m_osw = null;
	Process m_proc = null;

	public StringBuilder getStdOut() {
		return m_stdOut;
	}

	public StringBuilder getStdErr() {
		return m_stdErr;
	}

	public Commands() {

	}

	public static boolean checkRoot() {
		String paths = System.getenv(PATH);
		String[] pathComponents = paths.split(":");

		for (String path : pathComponents) {
			File f = new File(path, SU);
			if (f.exists()) {
				return true;
			}
		}
		return false;
	}

	public static void runScriptAsRoot(String scriptFile) {
		Commands cmds = new Commands();
		cmds.runCommandAsRoot("sh " + scriptFile);
	}

	public void runCommand(String command) {
		try {
			m_proc = m_runtime.exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void runCommandAsRoot(String command) {

		try {
			m_proc = m_runtime.exec("su");
			m_osw = new OutputStreamWriter(m_proc.getOutputStream());

			m_osw.write(command);
			m_osw.flush();
			m_osw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (m_osw != null) {
				try {
					m_osw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Process getProcess() {
		return m_proc;
	}

	public static boolean isProgramRunning(String name) {
		Commands cmds = new Commands();
		cmds.runCommandAsRoot("ps");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(cmds.getProcess().getInputStream()));
		try {
			String l;
			while ((l = in.readLine()) != null) {
				if (l.contains(name)) {
					return true;
				}
			}
		}catch(Exception e) {
			return false;
		}
		
		return false;
	}
}
