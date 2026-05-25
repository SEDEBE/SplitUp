package com.splitup.desktop.controller;

import com.splitup.desktop.MainApp;
import com.splitup.desktop.SessionContext;
import com.splitup.model.*;
import com.splitup.model.enums.GroupRole;
import com.splitup.service.BusinessException;
import com.splitup.service.BalanceService;
import com.splitup.service.GroupService;
import com.splitup.service.ExpenseService;
import com.splitup.service.dto.Settlement;
import com.splitup.service.dto.UserBalance;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Toolbar ──────────────────────────────────────────────────────────────
    @FXML private Label userLabel;

    // ── Sidebar de grupos ────────────────────────────────────────────────────
    @FXML private ListView<ExpenseGroup> groupsList;

    // ── Panel central ────────────────────────────────────────────────────────
    @FXML private StackPane centerPane;
    @FXML private VBox welcomePane;
    @FXML private VBox groupDetailPane;
    @FXML private Label groupNameLabel;
    @FXML private TabPane tabPane;

    // Tab Gastos
    @FXML private TableView<Expense>          expensesTable;
    @FXML private TableColumn<Expense,String> colExpDate;
    @FXML private TableColumn<Expense,String> colExpTitle;
    @FXML private TableColumn<Expense,String> colExpPayer;
    @FXML private TableColumn<Expense,String> colExpAmount;

    // Tab Miembros
    @FXML private TableView<GroupMember>          membersTable;
    @FXML private TableColumn<GroupMember,String> colMemName;
    @FXML private TableColumn<GroupMember,String> colMemEmail;
    @FXML private TableColumn<GroupMember,String> colMemRole;

    // Tab Balances
    @FXML private TableView<UserBalance>          balancesTable;
    @FXML private TableColumn<UserBalance,String> colBalUser;
    @FXML private TableColumn<UserBalance,String> colBalAmount;
    @FXML private TableColumn<UserBalance,String> colBalStatus;

    // Código de invitación (tab Miembros)
    @FXML private TextField inviteCodeField;

    // Tab Liquidar — Pendientes
    @FXML private TableView<Settlement>          pendingSettlementsTable;
    @FXML private TableColumn<Settlement,String> colPendFrom;
    @FXML private TableColumn<Settlement,String> colPendTo;
    @FXML private TableColumn<Settlement,String> colPendAmount;

    // Tab Liquidar — Historial
    @FXML private TableView<SettlementRecord>          confirmedSettlementsTable;
    @FXML private TableColumn<SettlementRecord,String> colConfDate;
    @FXML private TableColumn<SettlementRecord,String> colConfFrom;
    @FXML private TableColumn<SettlementRecord,String> colConfTo;
    @FXML private TableColumn<SettlementRecord,String> colConfAmount;

    private final GroupService   groupService   = new GroupService();
    private final ExpenseService expenseService = new ExpenseService();
    private final BalanceService balanceService = new BalanceService();

    @FXML
    private void initialize() {
        userLabel.setText("👤 " + SessionContext.getCurrentUser().getName());

        groupsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ExpenseGroup g, boolean empty) {
                super.updateItem(g, empty);
                setText(empty || g == null ? null : g.getName());
            }
        });

        groupsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, group) -> {
                    if (group != null) loadGroupDetail(group);
                });

        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (obs, old, idx) -> refreshCurrentTab(idx.intValue()));

        configureTableColumns();
        loadGroups();
        showWelcome();
    }

    // ── Grupos ───────────────────────────────────────────────────────────────

    private void loadGroups() {
        try {
            List<ExpenseGroup> groups = groupService.getGroupsByMember(SessionContext.getCurrentUser());
            groupsList.setItems(FXCollections.observableArrayList(groups));
        } catch (Exception e) {
            log.error("Error cargando grupos", e);
        }
    }

    @FXML
    private void onNewGroup() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Nuevo grupo");
        dlg.setHeaderText(null);
        dlg.setContentText("Nombre del grupo:");
        dlg.showAndWait().ifPresent(name -> {
            try {
                groupService.createGroup(name, null, SessionContext.getCurrentUser());
                loadGroups();
            } catch (BusinessException ex) {
                showError(ex.getMessage());
            }
        });
    }

    @FXML
    private void onJoinGroup() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Unirse a grupo");
        dlg.setHeaderText(null);
        dlg.setContentText("Código de invitación:");
        dlg.showAndWait().ifPresent(code -> {
            try {
                groupService.joinByInviteCode(code.trim(), SessionContext.getCurrentUser());
                loadGroups();
            } catch (BusinessException ex) {
                showError(ex.getMessage());
            }
        });
    }

    // ── Detalle de grupo ─────────────────────────────────────────────────────

    private void loadGroupDetail(ExpenseGroup group) {
        SessionContext.setSelectedGroup(group);
        groupNameLabel.setText(group.getName());
        welcomePane.setVisible(false);
        welcomePane.setManaged(false);
        groupDetailPane.setVisible(true);
        groupDetailPane.setManaged(true);

        int selectedTab = tabPane.getSelectionModel().getSelectedIndex();
        refreshCurrentTab(selectedTab >= 0 ? selectedTab : 0);
    }

    private void refreshCurrentTab(int tabIndex) {
        ExpenseGroup group = SessionContext.getSelectedGroup();
        if (group == null) return;
        switch (tabIndex) {
            case 0 -> loadExpenses(group);
            case 1 -> loadMembers(group);
            case 2 -> loadBalances(group);
            case 3 -> { loadPendingSettlements(group); loadConfirmedSettlements(group); }
        }
    }

    private void showWelcome() {
        welcomePane.setVisible(true);
        welcomePane.setManaged(true);
        groupDetailPane.setVisible(false);
        groupDetailPane.setManaged(false);
    }

    // ── Tab: Gastos ──────────────────────────────────────────────────────────

    private void loadExpenses(ExpenseGroup group) {
        try {
            List<Expense> expenses = expenseService.getExpensesByGroup(group);
            expensesTable.setItems(FXCollections.observableArrayList(expenses));
        } catch (Exception e) {
            log.error("Error cargando gastos", e);
        }
    }

    @FXML
    private void onNewExpense() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/splitup/desktop/fxml/create-expense.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(MainApp.getPrimaryStage());
            dialog.setTitle("Nuevo gasto");
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(
                            "/com/splitup/desktop/styles/app.css")).toExternalForm());
            dialog.setScene(scene);
            dialog.showAndWait();
            ExpenseGroup group = SessionContext.getSelectedGroup();
            loadExpenses(group);
            loadBalances(group);
            loadPendingSettlements(group);
            loadConfirmedSettlements(group);
        } catch (Exception e) {
            log.error("Error abriendo diálogo de gasto", e);
            showError("No se pudo abrir el formulario de gasto.");
        }
    }

    @FXML
    private void onDeleteExpense() {
        Expense selected = expensesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selecciona un gasto para eliminar."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el gasto \"" + selected.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                expenseService.deleteExpense(selected);
                loadExpenses(SessionContext.getSelectedGroup());
            }
        });
    }

    // ── Eliminar grupo ───────────────────────────────────────────────────────

    @FXML
    private void onDeleteGroup() {
        ExpenseGroup group = SessionContext.getSelectedGroup();
        if (group == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el grupo \"" + group.getName() + "\" y todos sus datos?\n" +
                "Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    groupService.deleteGroup(group, SessionContext.getCurrentUser());
                    SessionContext.setSelectedGroup(null);
                    showWelcome();
                    loadGroups();
                } catch (BusinessException ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    // ── Tab: Miembros ────────────────────────────────────────────────────────

    private void loadMembers(ExpenseGroup group) {
        try {
            List<GroupMember> members = groupService.getMembersOfGroup(group);
            membersTable.setItems(FXCollections.observableArrayList(members));
            // Mostrar el código de invitación (puede ser null en grupos creados antes de V3)
            String code = group.getInviteCode();
            inviteCodeField.setText(code != null ? code : "");
        } catch (Exception e) {
            log.error("Error cargando miembros", e);
        }
    }

    @FXML
    private void onCopyInviteCode() {
        String code = inviteCodeField.getText();
        if (code == null || code.isBlank()) {
            showError("Este grupo no tiene código de invitación todavía.");
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(code);
        Clipboard.getSystemClipboard().setContent(content);
        // Confirmación visual breve: cambiar temporalmente el texto del campo
        Alert info = new Alert(Alert.AlertType.INFORMATION,
                "Código copiado al portapapeles:\n" + code, ButtonType.OK);
        info.setHeaderText(null);
        info.showAndWait();
    }

    @FXML
    private void onAddMember() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Añadir miembro");
        dlg.setHeaderText(null);
        dlg.setContentText("Email del usuario a añadir:");
        dlg.showAndWait().ifPresent(email -> {
            try {
                com.splitup.service.UserService us = new com.splitup.service.UserService();
                User target = us.findByEmail(email)
                        .orElseThrow(() -> new BusinessException("No existe usuario con ese email."));
                groupService.addMember(SessionContext.getSelectedGroup(), target,
                        GroupRole.MEMBER, SessionContext.getCurrentUser());
                loadMembers(SessionContext.getSelectedGroup());
            } catch (BusinessException ex) {
                showError(ex.getMessage());
            }
        });
    }

    // ── Tab: Balances ────────────────────────────────────────────────────────

    private void loadBalances(ExpenseGroup group) {
        try {
            List<UserBalance> balances = balanceService.getGroupBalances(group);
            balancesTable.setItems(FXCollections.observableArrayList(balances));
        } catch (Exception e) {
            log.error("Error cargando balances", e);
        }
    }

    // ── Tab: Liquidaciones — Pendientes ──────────────────────────────────────

    private void loadPendingSettlements(ExpenseGroup group) {
        try {
            List<Settlement> settlements = balanceService.getSettlements(group);
            pendingSettlementsTable.setItems(FXCollections.observableArrayList(settlements));
        } catch (Exception e) {
            log.error("Error calculando liquidaciones pendientes", e);
        }
    }

    @FXML
    private void onConfirmSettlement() {
        Settlement selected = pendingSettlementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una transferencia pendiente para confirmar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Confirmas que %s ha pagado %.2f € a %s?",
                        selected.from().getName(), selected.amount(), selected.to().getName()),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    ExpenseGroup group = SessionContext.getSelectedGroup();
                    balanceService.confirmSettlement(
                            group,
                            selected.from(), selected.to(),
                            selected.amount(),
                            SessionContext.getCurrentUser());
                    loadPendingSettlements(group);
                    loadConfirmedSettlements(group);
                    loadBalances(group);
                } catch (BusinessException ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    // ── Tab: Liquidaciones — Historial ────────────────────────────────────────

    private void loadConfirmedSettlements(ExpenseGroup group) {
        try {
            List<SettlementRecord> records = balanceService.getConfirmedSettlements(group);
            confirmedSettlementsTable.setItems(FXCollections.observableArrayList(records));
        } catch (Exception e) {
            log.error("Error cargando liquidaciones confirmadas", e);
        }
    }

    @FXML
    private void onUnconfirmSettlement() {
        SettlementRecord selected = confirmedSettlementsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona un pago confirmado para deshacer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("¿Deshacer el pago de %s → %s por %.2f €?",
                        selected.getFromUser().getName(),
                        selected.getToUser().getName(),
                        selected.getAmount()),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    ExpenseGroup group = SessionContext.getSelectedGroup();
                    balanceService.unconfirmSettlement(
                            selected.getId(), SessionContext.getCurrentUser());
                    loadPendingSettlements(group);
                    loadConfirmedSettlements(group);
                    loadBalances(group);
                } catch (BusinessException ex) {
                    showError(ex.getMessage());
                }
            }
        });
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @FXML
    private void onLogout() {
        SessionContext.clear();
        try {
            MainApp.showLogin();
        } catch (Exception e) {
            log.error("Error volviendo al login", e);
        }
    }

    // ── Configuración de columnas ────────────────────────────────────────────

    private void configureTableColumns() {
        // Gastos
        colExpDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getExpenseDate().toString()));
        colExpTitle.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTitle()));
        colExpPayer.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPayer().getName()));
        colExpAmount.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("€ %.2f", c.getValue().getTotalAmount())));

        // Miembros
        colMemName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getName()));
        colMemEmail.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUser().getEmail()));
        colMemRole.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRole().name()));

        // Balances
        colBalUser.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().user().getName()));
        colBalAmount.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("€ %.2f", c.getValue().balance())));
        colBalStatus.setCellValueFactory(c -> {
            BigDecimal bal = c.getValue().balance();
            int sign = bal.compareTo(BigDecimal.ZERO);
            String status = sign > 0 ? "Acreedor ↑" : sign < 0 ? "Deudor ↓" : "Saldado ✓";
            return new SimpleStringProperty(status);
        });

        // Liquidar — Pendientes
        colPendFrom.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().from().getName()));
        colPendTo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().to().getName()));
        colPendAmount.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("€ %.2f", c.getValue().amount())));

        // Liquidar — Historial
        colConfDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSettledAt() != null
                        ? DT_FMT.format(c.getValue().getSettledAt()) : ""));
        colConfFrom.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFromUser().getName()));
        colConfTo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getToUser().getName()));
        colConfAmount.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("€ %.2f", c.getValue().getAmount())));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
