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

/**
 * Class that represents an Iodine exception.
 * @author Vitaly
 *
 */
public class IodineException extends Exception {

    /** Serial number. */
    private static final long serialVersionUID = -4144280992818031303L;

    /** The resource id of the message. */
    private int mMsgResId;


    /**
     * Creates a new Iodine exception with the specified message id.
     * @param msgResId The message id.
     */
    public IodineException(final int msgResId) {
        mMsgResId = msgResId;
    }

    /**
     * Get the message id.
     * @return The message id of the exception.
     */
    public final int getMessageId() {
        return mMsgResId;
    }
}
