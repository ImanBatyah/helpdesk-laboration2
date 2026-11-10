
package com.helpdesk.controller;

import com.helpdesk.model.User;
import com.helpdesk.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listUsers(Model model) {

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", User.Role.values());

        return "admin/users";
    }

    @PostMapping("/create")
    public String createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam User.Role role,
            RedirectAttributes redirectAttributes) {

        name = name.trim();
        email = email.trim();

        if (name.isBlank() || name.length() > 100
                || email.isBlank() || email.length() > 254
                || password.isBlank() || password.length() < 8) {

            redirectAttributes.addFlashAttribute(
                    "error", "Kontrollera namn, e-post och lösenord (minst 8 tecken).");

            return "redirect:/admin/users";
        }

        if (userRepository.existsByEmail(email)) {

            redirectAttributes.addFlashAttribute(
                    "error", "E-postadressen används redan.");

            return "redirect:/admin/users";
        }

        User user = new User(
                name,
                email,
                passwordEncoder.encode(password),
                role
        );

        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "success", "Användaren har skapats.");

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String updateRole(
            @PathVariable Long id,
            @RequestParam User.Role role,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = findUser(id);

        if (user.getEmail().equalsIgnoreCase(authentication.getName())
                && role != User.Role.ADMIN) {

            redirectAttributes.addFlashAttribute(
                    "error", "Du kan inte ta bort din egen administratörsroll.");

            return "redirect:/admin/users";
        }

        user.setRole(role);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "success", "Användarens roll har uppdaterats.");

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggleUser(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = findUser(id);

        if (user.getEmail().equalsIgnoreCase(authentication.getName())) {

            redirectAttributes.addFlashAttribute(
                    "error", "Du kan inte inaktivera ditt eget konto.");

            return "redirect:/admin/users";
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "success", "Kontots status har uppdaterats.");

        return "redirect:/admin/users";
    }

    private User findUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Användaren hittades inte"));
    }
}
