package com.acme.users.mgt.logging.services.api;

/**
 * Interface service de logging centralis&eacute;.
 */
public interface ILogService {

    /**
     * Trace en mode info.
     *
     * @param callerName    Appelant
     * @param bundleMessage Message
     * @param params        Param&egrave;tres
     */
    void infoB(String callerName, String bundleMessage, Object[] params);

    /**
     * Trace en mode info avec formatage du message (expressions de type
     * String.format(%s,xxx)).
     *
     * @param callerName Appelant
     * @param message    Message
     * @param params     Param&egrave;tres
     */
    void infoS(String callerName, String message, Object[] params);

    /**
     * Trace en mode debug.
     *
     * @param callerName    Appelant
     * @param bundleMessage Message
     * @param params        Param&egrave;tres
     */
    void debugB(String callerName, String bundleMessage, Object[] params);

    /**
     * Trace en mode debug avec formatage du message (expressions de type
     * String.format(%s,xxx)).
     *
     * @param callerName Appelant
     * @param message    Message
     * @param params     Param&egrave;tres
     */
    void debugS(String callerName, String message, Object[] params);

    /**
     * Trace en mode trace.
     *
     * @param callerName    Appelant
     * @param bundleMessage Message
     * @param params        Param&egrave;tres
     */
    void traceB(String callerName, String bundleMessage, Object[] params);

    /**
     * Trace en mode trace avec formatage du message (expressions de type
     * String.format(%s,xxx)).
     *
     * @param callerName Appelant
     * @param message    Message
     * @param params     Param&egrave;tres
     */
    void traceS(String callerName, String message, Object[] params);

    /**
     * Trace en mode warning.
     *
     * @param callerName    Appelant
     * @param bundleMessage Message
     * @param params        Param&egrave;tres
     */
    void warnB(String callerName, String bundleMessage, Object[] params);

    /**
     * Trace en mode warning avec formatage du message (expressions de type
     * String.format(%s,xxx)).
     *
     * @param callerName Appelant
     * @param message    Message
     * @param params     Param&egrave;tres
     */
    void warnS(String callerName, String message, Object[] params);

    /**
     * Trace en mode erreur.
     *
     * @param callerName Appelant
     * @param e          Exception
     */
    void error(String callerName, Exception e);

    /**
     * Trace en mode erreur.
     *
     * @param callerName    Appelant
     * @param bundleMessage Message
     * @param params        Param&egrave;tres
     */
    void errorB(String callerName, String bundleMessage, Object[] params);

    /**
     * Trace en mode erreur (expressions de type String.format(%s,xxx)).
     *
     * @param callerName Appelant
     * @param message    Message
     * @param params     Param&egrave;tres
     */
    void errorS(String callerName, String message, Object[] params);

}
