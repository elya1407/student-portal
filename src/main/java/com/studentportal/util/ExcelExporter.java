package com.studentportal.util;

import com.studentportal.model.Attendance;
import com.studentportal.model.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelExporter {
    public byte[] exportStudents(List<Student> students) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Студенты");
            Row header = sheet.createRow(0);
            String[] columns = {"ФИО", "Подгруппа", "Зачётка", "Телефон", "Email", "Дата рождения", "Активен"};
            createHeader(header, columns);

            int rowIndex = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(student.getFullName());
                row.createCell(1).setCellValue(student.getSubgroup());
                row.createCell(2).setCellValue(student.getRecordBookNumber());
                row.createCell(3).setCellValue(nullToEmpty(student.getPhone()));
                row.createCell(4).setCellValue(nullToEmpty(student.getEmail()));
                row.createCell(5).setCellValue(student.getBirthDate() == null ? "" : student.getBirthDate().toString());
                row.createCell(6).setCellValue(Boolean.TRUE.equals(student.getActive()) ? "Да" : "Нет");
            }
            autoSize(sheet, columns.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сформировать Excel", e);
        }
    }

    public byte[] exportAttendance(List<Attendance> attendanceList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Посещаемость");
            Row header = sheet.createRow(0);
            String[] columns = {"Дата", "Время отметки", "ФИО", "Зачётка", "Подгруппа", "Неделя"};
            createHeader(header, columns);

            int rowIndex = 1;
            for (Attendance attendance : attendanceList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(attendance.getDate().toString());
                row.createCell(1).setCellValue(attendance.getTime().toString());
                row.createCell(2).setCellValue(attendance.getStudent().getFullName());
                row.createCell(3).setCellValue(attendance.getStudent().getRecordBookNumber());
                row.createCell(4).setCellValue(attendance.getSubgroup());
                row.createCell(5).setCellValue(attendance.getWeekType());
            }
            autoSize(sheet, columns.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сформировать Excel", e);
        }
    }

    private void createHeader(Row header, String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
