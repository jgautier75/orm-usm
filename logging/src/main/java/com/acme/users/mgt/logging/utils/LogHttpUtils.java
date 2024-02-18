package com.acme.users.mgt.logging.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.acme.jga.users.mgt.exceptions.TechnicalException;
import com.acme.users.mgt.logging.services.api.ILogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogHttpUtils {
    public static final String CR_SEP = "\n     ";
    public static final String CR_SIMPLE = "\n";
    public static final ThreadLocal<Boolean> APP_LOG_CTX = new ThreadLocal<>();

    /**
     * Get error for application and id.
     * 
     * @param path    Path
     * @param appName Application
     * @param id      Id
     * @return File content
     */
    public static final String getError(String path, String appName, String id) {
        String targetFile = generateErrorFileName(appName, id);
        try {
            return Files.readString(Paths.get(targetFile));
        } catch (Exception e) {
            throw new TechnicalException("Unabel to access " + targetFile);
        }
    }

    /**
     * List files for path.
     * 
     * @param path Path
     * @return Files list
     */
    public static final String listFiles(String path) {
        final StringBuilder buf = new StringBuilder();
        buf.append("List ").append(path).append("\n");
        File f = new File(path);
        String[] pathnames = f.list();
        if (pathnames != null) {
            for (String pathname : pathnames) {
                // Print the names of files and directories
                buf.append(pathname).append("\n");
            }
        }
        return buf.toString();
    }

    public static String generateErrorFileName(String moduleName, String errorUuid) {
        return "Err_" + moduleName + "_" + errorUuid + ".log";
    }

    /**
     * Dump http request to string
     * 
     * @param httpServletRequest Http request
     * @return Dumped request
     */
    public static String dumpHttpRequest(HttpServletRequest httpServletRequest) {
        if (CachedHttpServletRequest.class.isAssignableFrom(httpServletRequest.getClass())) {
            return dumpCachedHttpRequest((CachedHttpServletRequest) httpServletRequest);
        } else {
            return dumpHttpReqRaw(httpServletRequest);
        }
    }

    /**
     * Dump cached http request.
     * 
     * @param httpServletRequest Http request
     * @return Dumped http request
     */
    public static String dumpCachedHttpRequest(CachedHttpServletRequest httpServletRequest) {
        StringBuilder dump = new StringBuilder();
        dump.append(dumpHttpReqRaw(httpServletRequest));
        dump.append(CR_SIMPLE).append("Request Body    :");
        dump.append(CR_SIMPLE).append(new String(httpServletRequest.getCachedBody()));
        return dump.toString();
    }

    /**
     * Dump raw http request.
     * 
     * @param httpServletRequest Http request
     * @return Dumped http request
     */
    public static String dumpHttpReqRaw(HttpServletRequest httpServletRequest) {
        StringBuilder dump = new StringBuilder();
        dump.append("Session ID      : ").append(httpServletRequest.getSession().getId());
        dump.append(CR_SIMPLE).append("Remote Addr     : ").append(httpServletRequest.getRemoteAddr());
        dump.append(CR_SIMPLE).append("Request Url     : ").append(httpServletRequest.getRequestURL());
        dump.append(CR_SIMPLE).append("Request Method  : ").append(httpServletRequest.getMethod());
        dump.append(CR_SIMPLE).append("Request Header  : ");
        Collections.list(httpServletRequest.getHeaderNames()).forEach(header -> dump.append(CR_SEP).append(header)
                .append(" : ").append(httpServletRequest.getHeader(header)));
        dump.append(CR_SIMPLE).append("Request Params  : ");
        Collections.list(httpServletRequest.getParameterNames()).forEach(param -> dump.append(CR_SEP).append(param)
                .append(" : ").append(httpServletRequest.getParameter(param)));
        return dump.toString();
    }

    /**
     * Dump http response.
     * 
     * @param responseWrapper Response wrapper
     * @return Dumped response
     */
    public static String dumpHttpResponse(ContentCachingResponseWrapper responseWrapper) {
        StringBuilder dump = new StringBuilder();
        dump.append("Response Status      : ").append(responseWrapper.getStatus());
        dump.append(CR_SIMPLE).append("Response Header  : ");
        responseWrapper.getHeaderNames().forEach(
                header -> dump.append(CR_SEP).append(header).append(" : ").append(responseWrapper.getHeader(header)));
        String responseBody = readStreamFully(responseWrapper.getContentInputStream());
        dump.append(CR_SIMPLE).append("Response Body    :");
        dump.append(responseBody);
        return dump.toString();
    }

    /**
     * Dump text to file.
     * 
     * @param logService Log service
     * @param path       Store path
     * @param moduleName Module name
     * @param errorUUID  Error uid
     * @param exContent  Exception content
     */
    public static void dumpToFile(ILogService logService, String path, String moduleName, String errorUUID,
            String exContent) {
        try (FileWriter fw = new FileWriter(path + "/" + LogHttpUtils.generateErrorFileName(moduleName, errorUUID))) {
            fw.write("Error:" + errorUUID + " ****************************************************\n");
            fw.write(exContent);
        } catch (IOException ioe) {
            logService.error("dumpToFile", ioe);
        }
    }

    public static void dumpToFile(ILogService logService, String path, String moduleName, String errorUUID,
            String exContent, HttpServletRequest httpServletRequest) {
        String payload = dumpHttpRequest(httpServletRequest);
        try (FileWriter fw = new FileWriter(path + "/" + LogHttpUtils.generateErrorFileName(moduleName, errorUUID))) {
            fw.write("Error:" + errorUUID + " ****************************************************");
            fw.write(CR_SEP);
            fw.write("Payload:  ****************************************************");
            fw.write(CR_SEP);
            fw.write(payload);
            fw.write(CR_SEP);
            fw.write("Stack:  ****************************************************");
            fw.write(CR_SEP);
            fw.write(exContent);
        } catch (IOException ioe) {
            logService.error("dumpToFile", ioe);
        }
    }

    /**
     * Read stream fully.
     * 
     * @param is Input stream.
     * @return Stream content as string
     */
    private static String readStreamFully(InputStream is) {
        String textContent = null;
        try (StringWriter writer = new StringWriter();
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader);) {
            char[] chars = new char[1024];

            while (true) {
                int readChars;
                if ((readChars = bufferedReader.read(chars)) == -1) {
                    textContent = writer.toString();
                    break;
                }
                writer.write(chars, 0, readChars);
            }
        } catch (IOException ioException) {
            // Silent catch
        }
        return textContent;
    }
}
