package com.example.excel_importer.controllers;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.excel_importer.services.ExcelImportService;

/**
 * Контроллер для массового импорта Excel-файлов в базу данных.
 * Поддерживает импорт нескольких файлов одновременно.
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private ExcelImportService excelImportService;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    public UploadController() {
        scheduler.setPoolSize(8);
        scheduler.initialize();
    }

    /**
     * Выполняет массовый импорт нескольких Excel-файлов в таблицу базы данных.
     * Путь к каждому файлу передаётся через символ ';'
     *
     * @param paths строка со списком абсолютных путей к файлам Excel, разделённых ';'
     * @param table имя таблицы, в которую будут импортированы данные
     * @return результат планирования задач
     */
    @GetMapping("/import")
    public ResponseEntity<String> importMultipleFiles(
            @RequestParam("paths") String paths,
            @RequestParam(value = "table", defaultValue = "employees") String table) {

        // Обработать входные данные через символ ';'
        String[] filePaths = paths.split(";");

        // Сборка планировщика задач
        List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        // Обработка файлов
        for (String p : filePaths) {
            File file = new File(p.trim());
            if (!file.exists()) {
                result.append("File not found: ").append(p).append("\n");
                continue;
            }

            // Создание задачи импорта с задержкой между тасками
            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    int inserted = excelImportService.importFile(p.trim(), table);
                    System.out.println("Imported " + inserted + " rows from " + p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, Duration.ofSeconds(new Random().nextInt(5) + 1));
            scheduledTasks.add(future);
            
            // Добавление задачи в результат
            result.append("Scheduled import for: ").append(p).append("\n");
        }

        result.append("Total scheduled: ").append(scheduledTasks.size()).append(" tasks");

        // Результат обработки всех файлов
        return ResponseEntity.ok(result.toString());
    }
}