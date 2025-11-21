package com.example.filetransfer.backup;

import java.io.IOException;
import java.nio.file.Path;

public interface BackupService {
    Path backupLotFile(String lotId, Path sourceFile) throws IOException;
}
