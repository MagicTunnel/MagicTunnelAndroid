package net.magictunnel.core;

public class IodineException extends Exception {
	private static final long serialVersionUID = -4144280992818031303L;
	private int m_msgResId;
	
	public IodineException(int msgResId) {
		m_msgResId = msgResId;
	}
	
	public int getMessageId() {
		return m_msgResId;
	}
}
