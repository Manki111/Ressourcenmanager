package Ressourcenmanager.resource;

import Ressourcenmanager.dto.ResourceDTO;
import Ressourcenmanager.repository.ResourceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ResourceService {

    @Inject
    private ResourceRepository resourceRepository;

    public List<Resource> getAllActive() {
        return resourceRepository.findAll();
    }

    public List<Resource> getAllIncludingInactive() {
        return resourceRepository.findAllIncludingInactive();
    }

    public void createResource(ResourceDTO dto) {
        Resource resource = new Resource(
                dto.getName(),
                dto.getDescription(),
                dto.getType(),
                dto.getCapacity(),
                dto.getLocation()
        );
        resourceRepository.save(resource);
    }

    public void updateResource(Long id, ResourceDTO dto) {
        Resource resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new IllegalArgumentException("Ressource nicht gefunden: " + id);
        }
        resource.setName(dto.getName());
        resource.setDescription(dto.getDescription());
        resource.setType(dto.getType());
        resource.setCapacity(dto.getCapacity());
        resource.setLocation(dto.getLocation());
        resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        resourceRepository.delete(id);
    }

    public void activateResource(Long id) {
        resourceRepository.activate(id);
    }

    public void hardDeleteResource(Long id) {
        resourceRepository.hardDelete(id);
    }

    public List<ResourceType> getAvailableTypes() {
        return List.of(ResourceType.values());
    }
}
