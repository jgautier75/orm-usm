package com.acme.users.mgt.logging.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.logging.utils.LogHttpUtils;
import lombok.RequiredArgsConstructor;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService {
    public static final Logger LOGGER = LoggerFactory.getLogger("app-logger");
    private final MessageSource messageSource;

    /**
     * {@inheritDoc}
     */
    @Override
    public void infoB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.INFO, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void infoS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.INFO, message, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debugB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.DEBUG, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debugS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.DEBUG, message, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void traceB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.TRACE, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void traceS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.TRACE, message, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warnB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.WARN, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warnS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.WARN, message, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String callerName, Exception e) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(callerName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void errorB(String callerName, String bundleMessage, Object[] params) {
        logBundleMessage(Level.ERROR, bundleMessage, params, callerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void errorS(String callerName, String message, Object[] params) {
        logSimpleMessage(Level.ERROR, message, params, callerName);
    }

    /**
     * Log with bundle message.
     * 
     * @param level      Log level
     * @param message    Message key in bundle
     * @param params     Parameters
     * @param callerName Caller
     */
    private void logBundleMessage(Level level, String message, Object[] params, String callerName) {
        if (level == Level.DEBUG && LOGGER.isDebugEnabled() && LogHttpUtils.APP_LOG_CTX.get() != null
                && LogHttpUtils.APP_LOG_CTX.get().booleanValue()) {
            LOGGER.debug(buildMessage(true, message, params, callerName));
        } else if (level == Level.ERROR && LOGGER.isErrorEnabled()) {
            LOGGER.error(buildMessage(true, message, params, callerName));
        } else if (level == Level.INFO && LOGGER.isInfoEnabled()) {
            LOGGER.info(buildMessage(true, message, params, callerName));
        } else if (level == Level.TRACE && LOGGER.isTraceEnabled()) {
            LOGGER.trace(buildMessage(true, message, params, callerName));
        } else if (level == Level.WARN && LOGGER.isWarnEnabled()) {
            LOGGER.warn(buildMessage(true, message, params, callerName));
        }
    }

    /**
     * Log using string format.<br/>
     * Use standard %s for substitution
     * 
     * @param level      Log level
     * @param message    Message
     * @param params     Parameters
     * @param callerName Caller name
     */
    private void logSimpleMessage(Level level, String message, Object[] params, String callerName) {
        if (level == Level.DEBUG && LOGGER.isDebugEnabled() && LogHttpUtils.APP_LOG_CTX.get() != null
                && LogHttpUtils.APP_LOG_CTX.get().booleanValue()) {
            LOGGER.debug(buildMessage(false, message, params, callerName));
        } else if (level == Level.ERROR && LOGGER.isErrorEnabled()) {
            LOGGER.error(buildMessage(false, message, params, callerName));
        } else if (level == Level.INFO && LOGGER.isInfoEnabled()) {
            LOGGER.info(buildMessage(false, message, params, callerName));
        } else if (level == Level.TRACE && LOGGER.isTraceEnabled()) {
            LOGGER.trace(buildMessage(false, message, params, callerName));
        } else if (level == Level.WARN && LOGGER.isWarnEnabled()) {
            LOGGER.warn(buildMessage(false, message, params, callerName));
        }
    }

    /**
     * Build message.
     * 
     * @param bundleMessage Is message a bundle key
     * @param msg           Message to format
     * @param params        Parameters
     * @param callerName    Caller name
     * @return Message
     */
    private String buildMessage(boolean bundleMessage, String msg, Object[] params, String callerName) {
        String targetMsg = msg;

        Object[] filteredParams = filterParams(params);
        if (bundleMessage) {
            targetMsg = messageSource.getMessage(msg, filteredParams, LocaleContextHolder.getLocale());
        } else {
            if (params != null && params.length > 0) {
                targetMsg = String.format(msg, filteredParams);
            }
        }

        return messageSource.getMessage("log_msg_noctx_format", new Object[] { callerName, targetMsg },
                LocaleContextHolder.getLocale());

    }

    /**
     * Filter parameters (URLDecode).
     * 
     * @param params Parameters
     * @return Filtered parameters
     */
    private Object[] filterParams(Object[] params) {
        if (params != null && params.length > 0) {
            Object[] filteredParams = new Object[params.length];
            for (int inc = 0; inc < params.length; inc++) {
                if (params[inc] != null && !"".equals(params[inc])) {
                    if (String.class.isAssignableFrom(params[inc].getClass())) {
                        try {
                            filteredParams[inc] = URLDecoder.decode((String) params[inc], StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // Decoding error, just ignore
                            // Can fail for example if an invalid email id filled in, expected email regex
                            // is returned but this regex contains an ampersand !
                        }
                    } else {
                        filteredParams[inc] = params[inc];
                    }
                } else {
                    filteredParams[inc] = params[inc];
                }
            }
            return filteredParams;
        } else {
            return new Object[0];
        }
    }

    @Override
    public void error(String callerName, Throwable t) {
        LOGGER.error(callerName, t);
    }

}
