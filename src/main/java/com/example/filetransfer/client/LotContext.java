package com.example.filetransfer.client;

import java.nio.file.Path;
import java.util.*;

public class LotContext {

    private final String lotId;
    private String systemId;
    private final Set<Integer> expectedWafers = new LinkedHashSet<>();
    private final Map<Integer, WaferContext> waferMap = new HashMap<>();
    private Path mainExcelPath;
    private boolean sent;

    public LotContext(String lotId) {
        this.lotId = lotId;
    }

    public String getLotId() {
        return lotId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public Set<Integer> getExpectedWafers() {
        return expectedWafers;
    }

    public void setExpectedWafers(Set<Integer> wafers) {
        expectedWafers.clear();
        if (wafers != null) {
            expectedWafers.addAll(wafers);
        }
    }

    public WaferContext getOrCreateWafer(int waferNo) {
        return waferMap.computeIfAbsent(waferNo, WaferContext::new);
    }

    public Collection<WaferContext> getWaferContexts() {
        return waferMap.values();
    }

    public Path getMainExcelPath() {
        return mainExcelPath;
    }

    public void setMainExcelPath(Path mainExcelPath) {
        this.mainExcelPath = mainExcelPath;
    }

    public boolean isSent() {
        return sent;
    }

    public void markSent() {
        this.sent = true;
    }
}
