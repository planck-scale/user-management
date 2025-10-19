package com.tericcabrel.authorization.controllers;

import com.tericcabrel.authorization.events.OnResetPasswordEvent;
import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.dtos.ForgotPasswordDto;
import com.tericcabrel.authorization.models.dtos.ResetPasswordDto;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.entities.UserAccount;
import com.tericcabrel.authorization.services.interfaces.UserAccountService;
import com.tericcabrel.authorization.services.interfaces.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static com.tericcabrel.authorization.utils.Constants.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class ResetPasswordController {

    private final UserService userService;

    private final ApplicationEventPublisher eventPublisher;

    private final UserAccountService userAccountService;

    public ResetPasswordController(
        UserService userService,
        ApplicationEventPublisher eventPublisher,
        UserAccountService userAccountService
    ) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.userAccountService = userAccountService;
    }

    @PostMapping(value = "/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto)
        throws ResourceNotFoundException {
        User user = userService.findByEmail(forgotPasswordDto.getEmail());
        Map<String, String> result = new HashMap<>();

        if (user == null) {
            result.put(MESSAGE_KEY, NO_USER_FOUND_WITH_EMAIL_MESSAGE);

            return ResponseEntity.badRequest().body(result);
        }

        eventPublisher.publishEvent(new OnResetPasswordEvent(user));

        result.put(MESSAGE_KEY, PASSWORD_LINK_SENT_MESSAGE);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordDto passwordResetDto)
        throws ResourceNotFoundException {
        UserAccount userAccount = userAccountService.findByToken(passwordResetDto.getToken());
        Map<String, String> result = new HashMap<>();

        if (userAccount.isExpired()) {
            result.put(MESSAGE_KEY, TOKEN_EXPIRED_MESSAGE);

            userAccountService.delete(userAccount.getId());

            return ResponseEntity.badRequest().body(result);
        }

        userService.updatePassword(userAccount.getUser().getId(), passwordResetDto.getPassword());

        result.put(MESSAGE_KEY, RESET_PASSWORD_SUCCESS_MESSAGE);

        userAccountService.delete(userAccount.getId());

        return ResponseEntity.badRequest().body(result);
    }
}
