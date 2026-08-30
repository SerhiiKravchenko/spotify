package org.epam.learn.uiservice.web;

import org.epam.learn.uiservice.client.StorageClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StoragePageController {

    private static final String CLIENT_REGISTRATION_ID = "spotify-ui";

    private final StorageClient storageClient;

    public StoragePageController(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    @GetMapping("/")
    public String index(Model model,
                        Authentication authentication,
                        @RegisteredOAuth2AuthorizedClient(CLIENT_REGISTRATION_ID) OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        model.addAttribute("storages", storageClient.findAll(accessToken));
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", hasRole(authentication, "ROLE_ADMIN"));
        model.addAttribute("storageForm", new StorageForm());
        return "index";
    }

    @PostMapping("/storages")
    public String create(@ModelAttribute StorageForm storageForm,
                         @RegisteredOAuth2AuthorizedClient(CLIENT_REGISTRATION_ID) OAuth2AuthorizedClient authorizedClient,
                         RedirectAttributes redirectAttributes) {
        try {
            storageClient.create(authorizedClient.getAccessToken().getTokenValue(),
                    storageForm.getStorageType(), storageForm.getBucket(), storageForm.getPath());
            redirectAttributes.addFlashAttribute("message", "Storage entry created.");
        } catch (RestClientResponseException ex) {
            redirectAttributes.addFlashAttribute("error", "Could not create storage entry: " + ex.getStatusCode());
        }
        return "redirect:/";
    }

    @PostMapping("/storages/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RegisteredOAuth2AuthorizedClient(CLIENT_REGISTRATION_ID) OAuth2AuthorizedClient authorizedClient,
                         RedirectAttributes redirectAttributes) {
        try {
            storageClient.delete(authorizedClient.getAccessToken().getTokenValue(), id);
            redirectAttributes.addFlashAttribute("message", "Storage entry " + id + " deleted.");
        } catch (RestClientResponseException ex) {
            redirectAttributes.addFlashAttribute("error", "Could not delete storage entry: " + ex.getStatusCode());
        }
        return "redirect:/";
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> role.equals(granted.getAuthority()));
    }
}
