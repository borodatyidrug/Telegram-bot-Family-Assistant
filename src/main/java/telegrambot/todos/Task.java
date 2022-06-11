package telegrambot.todos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.telegram.telegrambots.meta.api.objects.User;
/**
 * Класс для хранения данных простой задачи
 * @author borodatyidrug
 *
 */
public class Task implements ITask {
    
	protected String chatId;
    protected User owner;
    protected String taskName;
    protected String description;
    protected Set<String> tags;

    public Task() {
    	this.tags = new HashSet<>();
    	this.description = "";
    };
    
    public Task(
    		@JsonProperty("chatId") String chatId,
            @JsonProperty("owner") User owner, 
            @JsonProperty("taskName") String taskName, 
            @JsonProperty("description") String description, 
            @JsonProperty("tags") Set<String> tags) {
    	this.chatId = chatId;
        this.owner = owner;
        this.taskName = taskName;
        this.description = description;
        this.tags = tags;
    }
    @JsonProperty("chatId")
    @Override
    public void setChatId(String chatId) {
    	this.chatId = chatId;
    }
    @JsonProperty("owner")
    @Override
    public void setOwner(User user) {
        this.owner = user;
    }

    @JsonProperty("taskName")
    @Override
    public void setName(String name) {
        this.taskName = name;
    }

    @JsonProperty("description")
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("tags")
    @Override
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("chatId")
    @Override
    public String getChatId() {
    	return chatId;
    }
    @JsonProperty("owner")
    @Override
    public User getOwner() {
        return owner;
    }

    @JsonProperty("taskName")
    @Override
    public String getName() {
        return taskName;
    }

    @JsonProperty("description")
    @Override
    public String getDescription() {
        return description;
    }

    @JsonProperty("tags")
    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
    	return this.getClass().getName() + "{owner=" + owner.getUserName()
    	+ ", taskName=" + taskName
    	+ ", description=" + description
    	+ ", tags=" + tags.toString()
    	+ "}";
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.owner.getId());
        hash = 59 * hash + Objects.hashCode(this.taskName);
        hash = 59 * hash + Objects.hashCode(this.description);
        hash = 59 * hash + Objects.hashCode(this.tags);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Task other = (Task) obj;
        if (!Objects.equals(this.taskName, other.taskName)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.owner, other.owner)) {
            return false;
        }
        return Objects.equals(this.tags, other.tags);
    }
}
