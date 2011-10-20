/**
 * MagicTunnel DNS tunnel GUI for Android.
 * Copyright (C) 2011 Vitaly Chipounov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.magictunnel.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Utility class for issuing system commands.
 * @author Vitaly
 *
 */
public class Commands {
    /** The command to get root access. */
    public static final String SU = "su";

    /** The PATH environment variable. */
    public static final String PATH = "PATH";

    /** Runtime. */
    private Runtime mRuntime = Runtime.getRuntime();

    /** Standard output. */
    private StringBuilder mStdOut = new StringBuilder();

    /** Standard error output. */
    private StringBuilder mStdErr = new StringBuilder();

    /** Currently running process. */
    private Process mProc = null;

    /**
     *
     * @return The standard output.
     */
    public final StringBuilder getStdOut() {
        return mStdOut;
    }

    /**
     *
     * @return The standard error output.
     */
    public final StringBuilder getStdErr() {
        return mStdErr;
    }

    /**
     * @return Whether or not the command to have
     * root access is available.
     */
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

    /**
     * Run the given script file as root.
     * @param scriptFile The script to run.
     */
    public static void runScriptAsRoot(final String scriptFile) {
        Commands cmds = new Commands();
        cmds.runCommandAsRoot("sh " + scriptFile);
    }

    /**
     * Execute the specified command.
     * @param command The command to run.
     */
    public final void runCommand(final String command) {
        try {
            mProc = mRuntime.exec(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Execute the specified command as root.
     * @param command The command to run.
     */
    public final void runCommandAsRoot(final String command) {
        OutputStreamWriter osw = null;

        try {
            mProc = mRuntime.exec("su");
            osw = new OutputStreamWriter(mProc.getOutputStream());

            osw.write(command);
            osw.flush();
            osw.close();

        } catch (Exception ex) {
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
    }

    /**
     * Run the command as root and wait for its termination.
     * @param command The command to run.
     * @return The status of the command.
     */
    public final int runCommandAsRootAndWait(final String command) {
        runCommandAsRoot(command);
        try {
            mProc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mProc.exitValue();
    }

    /**
     * @return The currently running process.
     */
    public final Process getProcess() {
        return mProc;
    }

    /**
     * Checks whether the specified process is currently
     * running on the system.
     * @param name The name of the process.
     * @return Whether the process is running or not.
     */
    public static boolean isProgramRunning(final String name) {
        Commands cmds = new Commands();
        cmds.runCommandAsRoot("ps");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(cmds.getProcess().getInputStream()));
        try {
            String l;
            while ((l = in.readLine()) != null) {
                if (l.contains(name)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
