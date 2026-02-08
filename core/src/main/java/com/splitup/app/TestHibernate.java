package com.splitup.app;

import com.splitup.model.*;
import com.splitup.model.enums.GroupRole;
import com.splitup.model.enums.ShareType;
import com.splitup.model.enums.SplitMode;
import com.splitup.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestHibernate {

    // Para que sea reproducible
    private static final String EMAIL_OWNER = "mj@splitup.dev";
    private static final String EMAIL_MEMBER_2 = "alex@splitup.dev";
    private static final String EMAIL_MEMBER_3 = "miguel@splitup.dev";

    private static final String GROUP_NAME = "Viaje Lisboa (TEST)";
    private static final String CATEGORY_NAME = "Supermercado";

    // Fecha fija para que el test sea 100% reproducible
    private static final LocalDate TEST_EXPENSE_DATE = LocalDate.of(2026, 2, 8);

    public static void main(String[] args) {
        Transaction tx = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // 1) Users
            User owner = findOrCreateUser(session, "Mj", EMAIL_OWNER);
            User m2 = findOrCreateUser(session, "Alex", EMAIL_MEMBER_2);
            User m3 = findOrCreateUser(session, "Miguel", EMAIL_MEMBER_3);

            // 2) Group
            ExpenseGroup group = findOrCreateGroup(session, GROUP_NAME, "Grupo de test para validar entidades", owner);

            // 3) Members (PK compuesta group_id + user_id)
            findOrCreateMember(session, group, owner, GroupRole.OWNER);
            findOrCreateMember(session, group, m2, GroupRole.MEMBER);
            findOrCreateMember(session, group, m3, GroupRole.MEMBER);

            // 4) Category
            Category category = findOrCreateCategory(session, CATEGORY_NAME, "cart");

            // 5) Expense (idempotente: si ya existe, se reutiliza)
            Expense expense = findOrCreateExpense(
                    session,
                    group,
                    owner,
                    category,
                    "Mercadona (TEST)",
                    new BigDecimal("30.00"),
                    TEST_EXPENSE_DATE);

            session.flush(); // asegura ID si se acaba de crear

            // 6) ExpenseShares (EQUAL => amount_assigned NULL)
            // Clave compuesta (expense_id, user_id): evitamos duplicados si ya existen
            findOrCreateShare(session, expense, owner, ShareType.EQUAL, null);
            findOrCreateShare(session, expense, m2, ShareType.EQUAL, null);
            findOrCreateShare(session, expense, m3, ShareType.EQUAL, null);

            // 7) Attachment (opcionalmente idempotente)
            // Si quieres que NO cree adjuntos infinitos, lo hacemos idempotente por
            // file_path.
            Attachment att = findOrCreateAttachment(session, expense, "storage/tickets/test/mercadona_test.jpg");

            tx.commit();

            System.out.println("✅ TestHibernate OK");
            System.out.println("   ownerId=" + owner.getId());
            System.out.println("   groupId=" + group.getId());
            System.out.println("   categoryId=" + category.getId());
            System.out.println("   expenseId=" + expense.getId());
            System.out.println("   attachmentId=" + att.getId());

        } catch (Exception e) {
            safeRollback(tx);
            System.err.println("❌ TestHibernate FAIL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static User findOrCreateUser(Session session, String displayName, String email) {
        User user = session.createQuery(
                "from User u where u.email = :email",
                User.class)
                .setParameter("email", email)
                .uniqueResult();

        if (user == null) {
            user = new User(displayName, email);
            session.persist(user);
            session.flush(); // para obtener id (útil si luego se usa en IDs compuestos)
            System.out.println("Usuario creado: " + email + " (id=" + user.getId() + ")");
        } else {
            System.out.println("Usuario reutilizado: " + email + " (id=" + user.getId() + ")");
        }
        return user;
    }

    private static ExpenseGroup findOrCreateGroup(Session session, String name, String description, User createdBy) {
        ExpenseGroup group = session.createQuery(
                "from ExpenseGroup g where g.name = :name and g.createdBy.id = :ownerId",
                ExpenseGroup.class)
                .setParameter("name", name)
                .setParameter("ownerId", createdBy.getId())
                .uniqueResult();

        if (group == null) {
            group = new ExpenseGroup(name, description, createdBy);
            session.persist(group);
            session.flush();
            System.out.println("Grupo creado: " + name + " (id=" + group.getId() + ")");
        } else {
            System.out.println("Grupo reutilizado: " + name + " (id=" + group.getId() + ")");
        }
        return group;
    }

    private static void findOrCreateMember(Session session, ExpenseGroup group, User user, GroupRole role) {
        // PK compuesta: (group_id, user_id) existe en tabla group_members
        GroupMember existing = session.createQuery(
                "from GroupMember gm where gm.group.id = :gid and gm.user.id = :uid",
                GroupMember.class)
                .setParameter("gid", group.getId())
                .setParameter("uid", user.getId())
                .uniqueResult();

        if (existing == null) {
            GroupMember gm = new GroupMember(user, group, role);
            session.persist(gm);
            System.out.println("Miembro añadido: userId=" + user.getId()
                    + " -> groupId=" + group.getId() + " (" + role + ")");
        } else {
            System.out.println("Miembro ya existe: userId=" + user.getId()
                    + " -> groupId=" + group.getId());
        }
    }

    private static Category findOrCreateCategory(Session session, String name, String icon) {
        Category category = session.createQuery(
                "from Category c where c.name = :name",
                Category.class)
                .setParameter("name", name)
                .uniqueResult();

        if (category == null) {
            category = new Category(name, icon);
            session.persist(category);
            session.flush();
            System.out.println("Categoría creada: " + name + " (id=" + category.getId() + ")");
        } else {
            System.out.println("Categoría reutilizada: " + name + " (id=" + category.getId() + ")");
        }
        return category;
    }

    private static Expense findOrCreateExpense(
            Session session,
            ExpenseGroup group,
            User payer,
            Category category,
            String title,
            BigDecimal total,
            LocalDate date) {
        Expense expense = session.createQuery(
                "from Expense e " +
                        "where e.group.id = :gid and e.payer.id = :pid " +
                        "and e.title = :title and e.expenseDate = :date " +
                        "order by e.id desc",
                Expense.class)
                .setParameter("gid", group.getId())
                .setParameter("pid", payer.getId())
                .setParameter("title", title)
                .setParameter("date", date)
                .setMaxResults(1)
                .uniqueResult();

        if (expense == null) {
            expense = new Expense(group, payer, title, total, date);
            expense.setCategory(category);
            expense.setCurrency("EUR");
            expense.setSplitMode(SplitMode.EQUAL);
            expense.setNote("Compra para el piso (test)");
            session.persist(expense);
            session.flush();
            System.out.println("Gasto creado: id=" + expense.getId());
        } else {
            System.out.println("Gasto reutilizado: id=" + expense.getId());
        }

        return expense;
    }

    private static void findOrCreateShare(
            Session session,
            Expense expense,
            User user,
            ShareType type,
            BigDecimal amountAssigned) {
        ExpenseShare existing = session.createQuery(
                "from ExpenseShare s where s.expense.id = :eid and s.user.id = :uid",
                ExpenseShare.class)
                .setParameter("eid", expense.getId())
                .setParameter("uid", user.getId())
                .uniqueResult();

        if (existing == null) {
            ExpenseShare share = new ExpenseShare(expense, user, type, amountAssigned);
            session.persist(share);
            System.out.println("Share creado: expenseId=" + expense.getId()
                    + ", userId=" + user.getId() + ", type=" + type);
        } else {
            System.out.println("Share ya existe: expenseId=" + expense.getId()
                    + ", userId=" + user.getId());
        }
    }

    private static Attachment findOrCreateAttachment(Session session, Expense expense, String filePath) {
        Attachment existing = session.createQuery(
                "from Attachment a where a.expense.id = :eid and a.filePath = :path",
                Attachment.class)
                .setParameter("eid", expense.getId())
                .setParameter("path", filePath)
                .uniqueResult();

        if (existing == null) {
            Attachment att = new Attachment(expense, filePath);
            att.setMimeType("image/jpeg");
            att.setFileSize(245000); // Integer (coherente)
            session.persist(att);
            System.out.println("Adjunto creado: expenseId=" + expense.getId() + ", path=" + filePath);
            return att;
        } else {
            System.out.println("Adjunto reutilizado: expenseId=" + expense.getId() + ", path=" + filePath);
            return existing;
        }
    }

    private static void safeRollback(Transaction tx) {
        if (tx == null) {
            return;
        }
        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } catch (Exception ex) {
            System.err.println("Rollback fallido (conexión ya cerrada): " + ex.getMessage());
        }
    }
}
