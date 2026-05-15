package com.splitup.desktop.controller;

import com.splitup.desktop.SessionContext;
import com.splitup.model.ExpenseGroup;
import com.splitup.model.GroupMember;
import com.splitup.model.User;
import com.splitup.model.enums.SplitMode;
import com.splitup.service.BusinessException;
import com.splitup.service.ExpenseService;
import com.splitup.service.GroupService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CreateExpenseController {

    private static final Logger log = LoggerFactory.getLogger(CreateExpenseController.class);

    @FXML private TextField titleField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> payerCombo;
    @FXML private RadioButton equalRadio;
    @FXML private RadioButton customRadio;

    // Tabla de participantes (EQUAL: checkbox; CUSTOM: checkbox + monto)
    @FXML private TableView<ParticipantRow>             participantsTable;
    @FXML private TableColumn<ParticipantRow, Boolean>  colCheck;
    @FXML private TableColumn<ParticipantRow, String>   colParticipantName;
    @FXML private TableColumn<ParticipantRow, String>   colCustomAmount;

    private final GroupService   groupService   = new GroupService();
    private final ExpenseService expenseService = new ExpenseService();

    /** Fila del modelo para la tabla de participantes. */
    public static class ParticipantRow {
        final User user;
        final SimpleBooleanProperty selected = new SimpleBooleanProperty(true);
        final SimpleStringProperty  customAmount = new SimpleStringProperty("0.00");

        ParticipantRow(User user) { this.user = user; }
    }

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());

        ExpenseGroup group = SessionContext.getSelectedGroup();
        List<GroupMember> members = groupService.getMembersOfGroup(group);

        // Poblar ComboBox de pagadores con los miembros del grupo
        List<String> memberNames = members.stream()
                .map(m -> m.getUser().getName())
                .collect(Collectors.toList());
        payerCombo.setItems(FXCollections.observableArrayList(memberNames));

        // Preseleccionar el usuario actual como pagador si es miembro
        members.stream()
                .filter(m -> m.getUser().getId().equals(SessionContext.getCurrentUser().getId()))
                .findFirst()
                .ifPresent(m -> payerCombo.setValue(m.getUser().getName()));

        // Configurar tabla de participantes
        List<ParticipantRow> rows = members.stream()
                .map(m -> new ParticipantRow(m.getUser()))
                .collect(Collectors.toList());
        participantsTable.setItems(FXCollections.observableArrayList(rows));
        participantsTable.setEditable(true);

        colCheck.setCellValueFactory(c -> c.getValue().selected);
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));

        colParticipantName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().user.getName()));

        colCustomAmount.setCellValueFactory(c -> c.getValue().customAmount);
        colCustomAmount.setCellFactory(TextFieldTableCell.forTableColumn());
        colCustomAmount.setOnEditCommit(e -> e.getRowValue().customAmount.set(e.getNewValue()));

        // La columna de monto custom solo se activa en modo CUSTOM
        colCustomAmount.setVisible(false);

        ToggleGroup tg = new ToggleGroup();
        equalRadio.setToggleGroup(tg);
        customRadio.setToggleGroup(tg);
        equalRadio.setSelected(true);

        tg.selectedToggleProperty().addListener((obs, old, newVal) -> {
            boolean isCustom = newVal == customRadio;
            colCustomAmount.setVisible(isCustom);
        });
    }

    @FXML
    private void onSave() {
        try {
            String title = titleField.getText().trim();
            if (title.isBlank()) { showError("Introduce el título del gasto."); return; }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                showError("El importe no es un número válido."); return;
            }

            if (payerCombo.getValue() == null) { showError("Selecciona el pagador."); return; }

            ExpenseGroup group = SessionContext.getSelectedGroup();
            List<GroupMember> members = groupService.getMembersOfGroup(group);

            // Resolver pagador
            String payerName = payerCombo.getValue();
            User payer = members.stream()
                    .map(GroupMember::getUser)
                    .filter(u -> u.getName().equals(payerName))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Pagador no encontrado."));

            // Resolver participantes seleccionados
            List<ParticipantRow> rows = participantsTable.getItems().stream()
                    .filter(r -> r.selected.get())
                    .collect(Collectors.toList());

            if (rows.isEmpty()) { showError("Selecciona al menos un participante."); return; }

            List<User> participants = rows.stream().map(r -> r.user).collect(Collectors.toList());

            SplitMode mode = customRadio.isSelected() ? SplitMode.CUSTOM : SplitMode.EQUAL;
            Map<User, BigDecimal> customAmounts = null;

            if (mode == SplitMode.CUSTOM) {
                customAmounts = new LinkedHashMap<>();
                for (ParticipantRow row : rows) {
                    try {
                        BigDecimal a = new BigDecimal(row.customAmount.get().trim().replace(",", "."));
                        customAmounts.put(row.user, a);
                    } catch (NumberFormatException e) {
                        showError("Importe inválido para " + row.user.getName()); return;
                    }
                }
            }

            expenseService.createExpense(
                    group, payer, title, amount, datePicker.getValue(),
                    null, null, null, mode, participants, customAmounts);

            close();

        } catch (BusinessException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            log.error("Error creando gasto", ex);
            showError("Error inesperado al guardar el gasto.");
        }
    }

    @FXML
    private void onCancel() { close(); }

    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
