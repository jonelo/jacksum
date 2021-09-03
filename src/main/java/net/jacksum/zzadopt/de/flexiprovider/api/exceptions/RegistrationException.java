package net.jacksum.zzadopt.de.flexiprovider.api.exceptions;

/**
 * Exception used to indicate registration errors (used by the
 * {@link net.jacksum.zzadopt.de.flexiprovider.api.Registry Registry} class). Since this exception
 * is thrown during static initialization, it extends {@link RuntimeException}.
 * 
 * @author Martin D�ring
 */
public class RegistrationException extends RuntimeException {

    /**
     * Default constructor.
     */
    public RegistrationException() {
	super();
    }

    /**
     * Constructor.
     * 
     * @param s
     *                the error message
     */
    public RegistrationException(String s) {
	super(s);
    }

}
