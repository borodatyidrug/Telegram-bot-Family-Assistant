package telegrambot.istorage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileConfigStorage implements IConfigStorage {

	protected Map<String, String> configMap;
	protected ObjectMapper mapper;
	
	public FileConfigStorage() {
		mapper = new ObjectMapper();
	}
	
	@Override
	public Map<String, String> readConfig(String path) throws StreamReadException, DatabindException, IOException {
		configMap = mapper.readValue(new File(path), new TypeReference<Map<String, String>> () {});
		return configMap;
	}

	@Override
	public void writeConfig(String path) throws StreamWriteException, DatabindException, IOException {
		mapper.writeValue(new File(path), configMap);
	}

	@Override
	public String getValue(String key) {
		if (configMap != null && !configMap.isEmpty() && configMap.get(key) != null) {
			return configMap.get(key);
		} else {
			throw new IllegalArgumentException("В файле конфигурации отсутствует ключ \"" + key + "\", файл конфигурации - пуст, или"
					+ " - не существует");
		}
	}

}
