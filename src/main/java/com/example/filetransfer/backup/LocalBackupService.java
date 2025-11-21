package com.example.filetransfer.backup;

import com.example.filetransfer.client.ClientConfig;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalBackupService implements BackupService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public Path backupLotFile(String lotId, Path sourceFile) throws IOException {
        String today = LocalDate.now().format(DATE_FMT);
        Path root = ClientConfig.BACKUP_ROOT;
        Path targetDir = root.resolve(today).resolve(lotId);
        Files.createDirectories(targetDir);

        Path target = targetDir.resolve(sourceFile.getFileName());
        return Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
