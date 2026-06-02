package com.par.jbfh.storage;

import com.par.jbfh.storage.enums.FileType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorage {

    /**
     * Сохранить файл и вернуть путь к нему.
     *
     * @param file     загружаемый файл
     * @param entityId идентификатор сущности, к которой относится файл
     * @param fileType тип файла (определяет подпапку, макс. размер, разрешённые типы)
     * @return относительный путь к сохранённому файлу
     */
    String save(MultipartFile file, UUID entityId, FileType fileType);

    /**
     * Удалить файл по пути.
     *
     * @param filePath путь к файлу
     */
    void delete(String filePath);

    /**
     * Получить ресурс файла для скачивания/отображения.
     *
     * @param filePath путь к файлу
     * @return Resource для чтения файла
     */
    Resource getResource(String filePath);

    /**
     * Проверить файл на соответствие ограничениям типа (размер, content-type).
     *
     * @param file     файл для проверки
     * @param fileType тип файла с ограничениями
     * @throws IllegalArgumentException если файл не проходит валидацию
     */
    void validate(MultipartFile file, FileType fileType);
}