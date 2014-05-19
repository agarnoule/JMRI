// MrcInterface.java

package jmri.jmrix.mrc;

/**
 * Layout interface, similar to command station
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public interface MrcInterface {

    public void sendMrcMessage(MrcMessage msg);
        
	public void addMrcListener(int Mask, MrcListener l);
	public void removeMrcListener(int Mask, MrcListener l);

	boolean status();   // true if the implementation is operational

    	/**
	 * Mask value to request notification of all incoming messages
	 */
	public static final int ALL				=  ~0;

	/**
	 * Mask value to request notification of messages effecting slot status, including the programming slot
	 */
	public static final int THROTTLEINFO		=   1;

	/**
	 * Mask value to request notification of messages associated with programming
	 */
	public static final int PROGRAMMING		=   2;

	/**
	 * Mask value to request notification of messages indicating changes in turnout status
	 */
	public static final int TURNOUTS		=   4;

	/**
	 * Mask value to request notification of messages indicating changes in sensor status
	 */
	public static final int SENSORS			=   8;

	/**
	 * Mask value to request notification of messages associated with layout power
	 */
	public static final int POWER			=  16;
    
    public static final int CLOCK           = 32;
}


/* @(#)MrcInterface.java */
