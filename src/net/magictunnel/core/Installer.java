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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Takes care of copying Iodine files to the system partition.
 * @author Vitaly
 *
 */
public class Installer {
    /** Name of the dynamically-generated installation script. */
    public static final String INSTALL_SCRIPT = "install.sh";

    /** Where do we copy files to. */
    public static final String TARGET_PARTITION = "/system";

    /** The iodine executable file in our local data folder. */
    public static final String DNS_TUNNEL_LOCALFILE = "iodine";

    /** The final location of the iodine file. */
    public static final String DNS_TUNNEL_FILE = "/system/bin/iodine";

    /** The iodine executable file in our assets. */
    public static final String DNS_TUNNEL_ASSET = "iodine";

    /** Size of the buffer for copy operations. */
    private static final int BUFFER_SIZE = 512;

    /** 500 ms for timeout during install operation. */
    private static final int TIMEOUT = 500;

    /** Asset manager. */
    private AssetManager mAssets;

    /** Android context. */
    private Context mContext;

    /**
     * Initializes the installer class.
     * @param context The Android context.
     */
    public Installer(final Context context) {
        mAssets = context.getAssets();
        mContext = context;
    }

    /**
     * Copy the file from the input stream to the output stream.
     * @param out The destination of the copy.
     * @param in The source of the copy.
     * @throws IOException in case of problems.
     */
    private void copyFile(
            final OutputStream out,
            final InputStream in) throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        int count = 0;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }

    /**
     * Copies the specified asset to its final destination.
     * @param sourceAsset the name of the source asset.
     * @param destFile the name of the destination file.
     * @return the success of the copy.
     */
    private boolean installFile(
            final String sourceAsset,
            final String destFile) {

        InputStream inputStream;

        try {
            inputStream = mAssets.open(sourceAsset);
            FileOutputStream outputStream =
                mContext.openFileOutput(destFile, Context.MODE_PRIVATE);
            copyFile(outputStream, inputStream);
            outputStream.close();
            inputStream.close();
            return true;
        } catch (IOException e) {
            Log.e(Installer.class.toString(), e.getMessage());
            return false;
        }
    }

    /**
     * Creates an install script that will run in super user mode.
     * @param partition on which to install the DNS tunnel client.
     * @param source The source file.
     * @param dest The destination path of the iodine client.
     * @return the success status of the operation.
     */
    public final boolean generateInstallScript(
            final String partition,
            final String source,
            final String dest) {
        try {
            FileOutputStream fos =
                mContext.openFileOutput(INSTALL_SCRIPT, Context.MODE_PRIVATE);

            PrintWriter script = new PrintWriter(fos);

            File privateDir = mContext.getFilesDir();
            File absSource = new File(privateDir, source);

            //Remount read-write
            Partition p = new Partition();
            String mountCommand = p.remountPartition(partition, false);
            script.println(mountCommand);

            //Copy the file
            script.println("cp " + absSource.toString() + " " + dest);

            //Add executable permission
            script.println("chmod 700 " + dest);

            //Remount read-only
            mountCommand = p.remountPartition(partition, true);
            script.println(mountCommand);

            script.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * @return Whether the iodine client is installed.
     */
    public static boolean iodineInstalled() {
        File file = new File(DNS_TUNNEL_FILE);
        return file.exists();
    }

    /**
     * Performs the installation.
     * @return the success status of the operation.
     */
    public final boolean installIodine() {
        if (iodineInstalled()) {
            return true;
        }

        //Copy the file to private storage
        if (!installFile(DNS_TUNNEL_ASSET, DNS_TUNNEL_LOCALFILE)) {
            return false;
        }

        //Generate a shell script that will copy the file
        //to the root partition
        if (!generateInstallScript(TARGET_PARTITION,
                DNS_TUNNEL_LOCALFILE, DNS_TUNNEL_FILE)) {
            return false;
        }

        File script = new File(mContext.getFilesDir(), INSTALL_SCRIPT);
        Commands.runScriptAsRoot(script.toString());
        try {
            //Give some time for the script to complete.
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return iodineInstalled();
    }
}
