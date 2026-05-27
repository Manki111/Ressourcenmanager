package Ressourcenmanager.dto;

import Ressourcenmanager.resource.ResourceType;
import java.io.Serializable;

public class ResourceDTO implements Serializable {

    private String name;
    private String description;
    private ResourceType type;
    private int capacity;
    private String location;

    public ResourceDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
