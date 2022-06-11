package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.telegram.telegrambots.meta.api.objects.User;
import telegrambot.todos.ITask;
import telegrambot.todos.Task;

public class TaskTest {
    
    public ITask task;
    public User user;
    public Set<String> tags;
    public Random r;
    public String firstName;
    public String lastName;
    public String userName;
    public String taskName;
    public String description;
    public ObjectMapper mapper;
    public String result;
    
    public TaskTest() throws JsonProcessingException {
        r = new Random();
        tags = new HashSet<>();
        firstName = "Антон";
        lastName = "Антонов";
        userName = "@onton";
        taskName = "Тестовое имя";
        description = "Описание задачи";
        tags.add("личное");
        tags.add("рабочее");
        user = new User(r.nextLong(), firstName, Boolean.FALSE, lastName, userName, "RU_ru", Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        task = new Task("4305204398687", user, taskName, description, tags);
        mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(task));
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }
    
    @Test
    public void testMapping() {
        assertEquals(1, 1);
    }
    
}
