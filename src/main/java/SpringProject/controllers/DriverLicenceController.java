package SpringProject.controllers;

import SpringProject.services.DriverLicenceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequestMapping("/drivers")
public class DriverLicenceController {
    private final DriverLicenceService driverLicenceService;

    public DriverLicenceController(DriverLicenceService driverLicenceService) {
        this.driverLicenceService = driverLicenceService;
    }

    private boolean notLoggedIn(HttpSession session) {
        return session == null || session.getAttribute("loggedInUser") == null;
    }

    private void addHeaderData(HttpSession session, Model model) {
        if (session != null) {
            model.addAttribute("username", session.getAttribute("loggedInUser"));
        }
    }

    @GetMapping("/licence-proof")
    public String licenceProofForm(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        addFormData(model);
        addHeaderData(session, model);
        return "driverLicenceProof";
    }

    @PostMapping("/licence-proof")
    public String saveLicenceProof(@RequestParam int driverId,
                                   @RequestParam String licenseNumber,
                                   @RequestParam String permitType,
                                   @RequestParam MultipartFile licenceProof,
                                   HttpSession session,
                                   RedirectAttributes ra,
                                   Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        try {
            driverLicenceService.saveLicenceProof(driverId, licenseNumber, permitType, licenceProof);
            ra.addFlashAttribute("success", "Licence proof uploaded. It is now pending admin verification.");
            return "redirect:/drivers/licence-proof";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("driverId", driverId);
            model.addAttribute("licenseNumber", licenseNumber);
            model.addAttribute("permitType", permitType);
            addFormData(model);
            addHeaderData(session, model);
            return "driverLicenceProof";
        }
    }

    @GetMapping("/licence-proof-file/{fileName:.+}")
    public ResponseEntity<Resource> viewLicenceProof(@PathVariable String fileName, HttpSession session) {
        if (notLoggedIn(session)) {
            return ResponseEntity.status(302).header("Location", "/login").build();
        }

        try {
            Path proofFile = driverLicenceService.getLicenceProofFile(fileName);
            String contentType = Files.probeContentType(proofFile);
            MediaType mediaType = contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(contentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new FileSystemResource(proofFile));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private void addFormData(Model model) {
        try {
            model.addAttribute("drivers", driverLicenceService.getDriversForLicenceUpload());
        } catch (Exception e) {
            model.addAttribute("drivers", java.util.List.of());
        }
    }
}
