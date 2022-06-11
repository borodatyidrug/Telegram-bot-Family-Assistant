package telegrambot.todos;

import java.util.Set;
import org.telegram.telegrambots.meta.api.objects.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * Интерфейс определяет обязательные методы для объектов, представляющих простые задачи менеджера задач.
 * @author borodatyidrug
 *
 */
@JsonDeserialize(as = Task.class) // Сериализовать объекты данного типа как указанный класс
@JsonSerialize(as = Task.class)
public interface ITask {
	/**
	 * Задает идентификатор чата, в котором был вызвана команда менеджера задач
	 * @param chatId
	 */
    @JsonProperty("chatId")
    void setChatId(String chatId);
    /**
     * Задает "владельца" задачи, т.е. - пользователя, который вызвал команду менеджера задач
     * @param user Пользователь-владелец задачи
     */
    @JsonProperty("owner")
    void setOwner(User user);
    /**
     * Задает имя создаваемой задачи
     * @param name Имя
     */
    @JsonProperty("taskName")
    void setName(String name);
    /**
     * Задает описание создаваемой задаче
     * @param description Описание
     */
    @JsonProperty("description")
    void setDescription(String description);
    /**
     * Задает набор тегов для создаваемой задачи
     * @param tags Множество, содержащее заданные теги
     */
    @JsonProperty("tags")
    void setTags(Set<String> tags);
    
    @JsonProperty("chatId")
    String getChatId();
    @JsonProperty("owner")
    User getOwner();
    @JsonProperty("taskName")
    String getName();
    @JsonProperty("description")
    String getDescription();
    @JsonProperty("tags")
    Set<String> getTags();
}
