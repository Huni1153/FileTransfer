package com.example.filetransfer.client;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WaferContext {

    private final int waferNo;
    private final List<Path> subCsvFiles = new ArrayList<>();
    private final List<Path> mapFiles = new ArrayList<>();
    private final List<Path> otherFiles = new ArrayList<>();

    public WaferContext(int waferNo) {
        this.waferNo = waferNo;
    }

    public int getWaferNo() {
        return waferNo;
    }

    public List<Path> getSubCsvFiles() {
        return subCsvFiles;
    }

    public List<Path> getMapFiles() {
        return mapFiles;
    }

    public List<Path> getOtherFiles() {
        return otherFiles;
    }

    public void addSubCsv(Path path) {
        subCsvFiles.add(path);
    }

    public void addMap(Path path) {
        mapFiles.add(path);
    }

    public void addOther(Path path) {
        otherFiles.add(path);
    }
}
