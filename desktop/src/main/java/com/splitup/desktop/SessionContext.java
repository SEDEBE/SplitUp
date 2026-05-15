package com.splitup.desktop;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.User;

/** Almacén estático de estado de sesión: usuario logueado y grupo seleccionado. */
public final class SessionContext {

    private static User currentUser;
    private static ExpenseGroup selectedGroup;

    private SessionContext() {}

    public static User getCurrentUser()           { return currentUser; }
    public static void setCurrentUser(User u)     { currentUser = u; }

    public static ExpenseGroup getSelectedGroup()        { return selectedGroup; }
    public static void setSelectedGroup(ExpenseGroup g)  { selectedGroup = g; }

    public static void clear() {
        currentUser   = null;
        selectedGroup = null;
    }
}
