package telegrambot.istorage;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
/**
 * Интерфейс определяет простейший набор методов для чтения и записи конфигурации приложения.
 * @author borodatyidrug
 *
 */
public interface IConfigStorage {
	/**
	 * Возвращает мапу с прочитанным конфигом
	 * @param path Путь к файлу конфигурации
	 * @return Мапа с конфигурацией
	 * @throws StreamReadException
	 * @throws DatabindException
	 * @throws IOException
	 */
    Map<String, String> readConfig(String path) throws StreamReadException, DatabindException, IOException;
    /**
     * Записывает текущую конфигурацию приложения в файл
     * @param path Путь к файлу конфигурации
     * @throws StreamWriteException
     * @throws DatabindException
     * @throws IOException
     */
    void writeConfig(String path) throws StreamWriteException, DatabindException, IOException;
    /**
     * Возвращает значение параметра конфигурации по его имени
     * @param key Имя-ключ параметра конфигурации
     * @return Значение параметра конфигурации
     * @throws IllegalArgumentException
     */
    String getValue(String key) throws IllegalArgumentException;
}
