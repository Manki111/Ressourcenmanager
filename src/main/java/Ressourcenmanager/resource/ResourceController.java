package Ressourcenmanager.resource;

import Ressourcenmanager.dto.ResourceDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class ResourceController implements Serializable {

    @Inject
    private ResourceService resourceService;

    private List<Resource> resources;
    private ResourceDTO newResource = new ResourceDTO();

    private Resource selectedResource;
    private ResourceDTO editResource = new ResourceDTO();

    @PostConstruct
    public void init() {
        loadResources();
    }

    private void loadResources() {
        resources = resourceService.getAllIncludingInactive();
    }

    public void saveNewResource() {
        if (newResource.getName() == null || newResource.getName().isBlank()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Pflichtfeld", "Bitte einen Namen eingeben.");
            return;
        }
        if (newResource.getType() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Pflichtfeld", "Bitte einen Typ auswählen.");
            return;
        }
        if (newResource.getCapacity() <= 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Ungültige Kapazität",
                    "Die Kapazität muss mindestens 1 betragen.");
            return;
        }

        resourceService.createResource(newResource);
        newResource = new ResourceDTO();
        loadResources();
        addMessage(FacesMessage.SEVERITY_INFO, "Gespeichert",
                "Die Ressource wurde erfolgreich angelegt.");
    }

    public void prepareEdit(Resource resource) {
        selectedResource = resource;
        editResource = new ResourceDTO();
        editResource.setName(resource.getName());
        editResource.setDescription(resource.getDescription());
        editResource.setType(resource.getType());
        editResource.setCapacity(resource.getCapacity());
        editResource.setLocation(resource.getLocation());
    }

    public void saveEdit() {
        if (selectedResource == null) return;

        if (editResource.getName() == null || editResource.getName().isBlank()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Pflichtfeld", "Bitte einen Namen eingeben.");
            return;
        }
        if (editResource.getCapacity() <= 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Ungültige Kapazität",
                    "Die Kapazität muss mindestens 1 betragen.");
            return;
        }

        resourceService.updateResource(selectedResource.getId(), editResource);
        selectedResource = null;
        loadResources();
        addMessage(FacesMessage.SEVERITY_INFO, "Aktualisiert",
                "Die Ressource wurde erfolgreich aktualisiert.");
    }

    public void deleteResource(Long id) {
        resourceService.deleteResource(id);
        loadResources();
        addMessage(FacesMessage.SEVERITY_INFO, "Deaktiviert",
                "Die Ressource wurde deaktiviert und ist nicht mehr buchbar.");
    }

    public void activateResource(Long id) {
        resourceService.activateResource(id);
        loadResources();
        addMessage(FacesMessage.SEVERITY_INFO, "Aktiviert",
                "Die Ressource ist wieder buchbar.");
    }

    public void hardDeleteResource(Long id) {
        resourceService.hardDeleteResource(id);
        loadResources();
        addMessage(FacesMessage.SEVERITY_INFO, "Gelöscht",
                "Die Ressource und alle zugehörigen Buchungen wurden dauerhaft entfernt.");
    }

    public List<ResourceType> getAvailableTypes() {
        return resourceService.getAvailableTypes();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<Resource> getResources() { return resources; }

    public List<Resource> getRooms() {
        if (resources == null) return List.of();
        return resources.stream()
                .filter(r -> r.getType() == ResourceType.RAUM && r.isActive())
                .toList();
    }

    public List<Resource> getSeats() {
        if (resources == null) return List.of();
        return resources.stream()
                .filter(r -> r.getType() == ResourceType.SITZPLATZ && r.isActive())
                .toList();
    }
    public ResourceDTO getNewResource() { return newResource; }
    public void setNewResource(ResourceDTO newResource) { this.newResource = newResource; }
    public Resource getSelectedResource() { return selectedResource; }
    public ResourceDTO getEditResource() { return editResource; }
    public void setEditResource(ResourceDTO editResource) { this.editResource = editResource; }
}
