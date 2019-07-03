package org.sinoc.shell.web.controller;

import org.sinoc.shell.config.WebEnabledCondition;
import org.sinoc.shell.model.dto.*;
import org.sinoc.shell.service.BlockchainInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Conditional(WebEnabledCondition.class)
public class WebSocketController {

    @Autowired
    BlockchainInfoService blockchainInfoService;

    @Autowired
    private Environment env;


    /**
     * Websocket handlers for immediate result.
     */

    @MessageMapping("/machineInfo")
    public MachineInfoDTO getMachineInfo() {
        return blockchainInfoService.getMachineInfo();
    }

    @MessageMapping("/initialInfo")
    public InitialInfoDTO getInitialInfo() {
        return blockchainInfoService.getInitialInfo();
    }

    @MessageMapping("/currentBlocks")
    public Queue<BlockInfo> getBlocks() {
        return blockchainInfoService.getBlocks();
    }

    @MessageMapping("/currentSystemLogs")
    public Queue<String> getSystemLogs() {
        return blockchainInfoService.getSystemLogs();
    }

    /**
     * Defines request mapping for all site pages.
     * As we have angular routing - we return index.html here.
     */
    @RequestMapping({"/", "/systemLog", "/peers", "/rpcUsage", "/terminal", "/wallet", "/contracts", "/contractNew"})
    public String index(HttpServletRequest request) {
        final boolean contractsEnabled = env.getProperty("feature.contract.enabled", "false").equalsIgnoreCase("true");
        if (!contractsEnabled && request.getRequestURI().equalsIgnoreCase("/contracts")) {
            return "error.html";
        }
        return "index.html";
    }

    /**
     * @return logs file to be able to download from browser
     */
    @RequestMapping(value = "/logs/{logName:.+}", method = RequestMethod.GET)
    @ResponseBody
    public FileSystemResource logFile(@PathVariable("logName") String logName, HttpServletResponse response) {
        final String fileName = logName;

        if (!fileName.endsWith(".log") && !fileName.endsWith(".zip")) {
            throw new RuntimeException("Forbidden file requested: " + fileName);
        }

        // force file downloading, otherwise line breaks will gone in web view
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        return new FileSystemResource(new File(getLogsDir() + "/" + fileName));
    }

    @RequestMapping(value = "/logs", method = RequestMethod.GET)
    @ResponseBody
    public String listLogFiles() {
        final File logsLocation = new File(getLogsDir());
        final File[] files = logsLocation.listFiles();
        if (files == null) {
            return "No logs found";
        }

        return "<html><body>"
                + Arrays.asList(files).stream()
                    .sorted()
                    .map(f -> "<a href='logs/" + f.getName() + "'>" + f.getName() + "</a> " + readableFileSize(f.length()))
                    .collect(Collectors.joining("<br>"))

                + "</body></html>";
    }

    private String getLogsDir() {
        return env.getProperty("logs.dir", "logs");
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ResponseBody
    public String showConfig() {
        return blockchainInfoService.getConfigDump()
                .replaceAll("\n", "<br>");
    }

    @RequestMapping(value = "/genesis", method = RequestMethod.GET)
    @ResponseBody
    public String showGenesis() {
        return blockchainInfoService.getGenesisDump()
                .replaceAll("\n", "<br>");
    }

    // for human readable size
    private String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
