package ru.rsreu.videohosting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rsreu.videohosting.dto.LoginDTO;
import ru.rsreu.videohosting.dto.RegistrationDTO;
import ru.rsreu.videohosting.service.UserService;

import javax.validation.Valid;

@Controller
public class AuthorizationController {

    private final UserService userService;

    @Autowired
    public AuthorizationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationDTO());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") @Valid RegistrationDTO registrationDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "registration";
        }

        try {
            if (userService.isUserExist(registrationDto.getLogin())) {
                result.rejectValue("login", "login.exists");
            }
            if (userService.isUserWithCurrentEmailExist(registrationDto.getEmail())) {
                result.rejectValue("email", "email.exists");
            }

            if (result.hasErrors()) {
                return "registration";
            }
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно!");
            return "redirect:/login";
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("login", "error.user", "Логин уже занят");
            return "registration";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка регистрации: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new LoginDTO());
        return "login";
    }

}
