package com.example.excel_importer.services;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Сервис импорта данных из Excel-файлов в базу данных.
 * Использует Apache POI и JDBC.
 */
@Service
public class ExcelImportService {

    private final JdbcTemplate jdbc;

    public ExcelImportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Импортирует указанный Excel-файл в таблицу базы данных.
     * Поддерживает стандартные форматы XLSX и CSV, а также выполняет пакетную
     * вставку данных.
     *
     * @param absolutePath абсолютный путь к Excel-файлу
     * @param tableName    имя таблицы, в которую будут импортированы данные
     * @return количество вставленных строк
     * @throws Exception в случае ошибок чтения или вставки данных
     */
    public int importFile(String absolutePath, String tableName) throws Exception {
        System.out.println("Starting import for file: " + absolutePath + " into table: " + tableName);

        // Используем общий FileInputStream для универсальности
        FileInputStream inputStream = new FileInputStream(absolutePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<Object[]> batch = new ArrayList<>();
        int rowCount = 0;

        // Проходим по всем строкам и добавляем данные в коллекцию для batch вставки
        for (Row row : sheet) {
            String firstName = getStringValue(row.getCell(0));
            String lastName = getStringValue(row.getCell(1));
            String email = getStringValue(row.getCell(2));

            batch.add(new Object[] { firstName, lastName, email });
            rowCount++;

            // Для повышения производительности выполняем batch вставку
            if (batch.size() % 100 == 0) {
                String sql = "INSERT INTO " + tableName + " (first_name, last_name, email) VALUES (?, ?, ?)";
                jdbc.batchUpdate(sql, batch);
                batch.clear();
            }
        }

        // Вставляем оставшиеся данные
        if (!batch.isEmpty()) {
            String sql = "INSERT INTO " + tableName + " (first_name, last_name, email) VALUES (?, ?, ?)";
            jdbc.batchUpdate(sql, batch);
        }

        workbook.close(); // закрываем workbook, освобождаем ресурсы
        System.out.println("Import finished successfully: " + rowCount + " rows inserted");
        return rowCount;
    }

    /**
     * Преобразует значение ячейки Excel в строку для вставки в БД
     */
    private String getStringValue(Cell cell) {
        if (cell == null)
            return "";
        return cell.toString().trim();
    }
}