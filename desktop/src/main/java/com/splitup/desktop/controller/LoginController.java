package com.splitup.desktop.controller;

import com.splitup.desktop.MainApp;
import com.splitup.desktop.SessionContext;
import com.splitup.model.User;
import com.splitup.service.BusinessException;
import com.splitup.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     nameField;
    @FXML private VBox          registerBox;
    @FXML private Button        actionButton;
    @FXML private Button        googleButton;
    @FXML private Label         modeLabel;
    @FXML private Hyperlink     toggleLink;
    @FXML private Label         errorLabel;

    private boolean registerMode = false;
    private UserService userService;

    @FXML
    private void initialize() {
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private UserService getUserService() {
        if (!com.splitup.utils.HibernateUtil.isAvailable()) {
            throw new RuntimeException("Base de datos no disponible");
        }
        if (userService == null) {
            userService = new UserService();
        }
        return userService;
    }

    @FXML
    private void onAction() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        clearError();

        if (email.isBlank()) { showError("Introduce tu email."); return; }
        if (password.isBlank()) { showError("Introduce tu contraseña."); return; }

        UserService svc;
        try {
            svc = getUserService();
        } catch (RuntimeException ex) {
            showError("Sin conexión a la base de datos.\nComprueba que MySQL está activo en localhost:3306.");
            return;
        }

        if (registerMode) {
            String name = nameField.getText().trim();
            if (name.isBlank()) { showError("Introduce tu nombre."); return; }
            try {
                User user = svc.registerWithPassword(name, email, password);
                navigateToMain(user);
            } catch (BusinessException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                log.error("Error al registrar usuario", ex);
                showError("Error de conexión. Comprueba que MySQL está activo.");
            }
        } else {
            try {
                Optional<User> found = svc.loginWithPassword(email, password);
                if (found.isPresent()) {
                    navigateToMain(found.get());
                } else {
                    showError("Email o contraseña incorrectos.");
                }
            } catch (Exception ex) {
                log.error("Error al iniciar sesión", ex);
                showError("Error de conexión. Comprueba que MySQL está activo.");
            }
        }
    }

    @FXML
    private void onGoogleLogin() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Google Sign-In");
        info.setHeaderText("Autenticación con Google");
        info.setContentText(
                "La autenticación con Google está disponible en la app Android.\n\n" +
                "El backend soporta el proveedor GOOGLE mediante la tabla auth_identities." +
                " En la versión Android se usa el SDK nativo de Google Sign-In.");
        info.showAndWait();
    }

    @FXML
    private void onToggleMode() {
        registerMode = !registerMode;
        clearError();
        if (registerMode) {
            modeLabel.setText("Crear cuenta");
            actionButton.setText("Registrarse");
            toggleLink.setText("¿Ya tienes cuenta? Inicia sesión");
            registerBox.setVisible(true);
            registerBox.setManaged(true);
        } else {
            modeLabel.setText("Iniciar sesión");
            actionButton.setText("Entrar");
            toggleLink.setText("¿No tienes cuenta? Regístrate");
            registerBox.setVisible(false);
            registerBox.setManaged(false);
        }
    }

    private void navigateToMain(User user) {
        SessionContext.setCurrentUser(user);
        try {
            MainApp.showMain();
        } catch (Exception ex) {
            log.error("Error al abrir pantalla principal", ex);
            showError("Error al cargar la aplicación.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
