## 📌 Diagrama ER (Mermaid)

```mermaid
erDiagram
  USERS ||--o{ AUTH_IDENTITIES : has
  USERS ||--o{ GROUP_MEMBERS : joins
  GROUPS ||--o{ GROUP_MEMBERS : contains
  GROUPS ||--o{ EXPENSES : has
  USERS ||--o{ EXPENSES : pays
  EXPENSES ||--o{ EXPENSE_SHARES : splits
  USERS ||--o{ EXPENSE_SHARES : participates
  EXPENSES ||--o{ ATTACHMENTS : has
  CATEGORIES ||--o{ EXPENSES : classifies

  USERS {
    BIGINT id PK
    VARCHAR display_name
    VARCHAR email
    VARCHAR avatar_url
    DATETIME created_at
    DATETIME updated_at
  }

  AUTH_IDENTITIES {
    BIGINT id PK
    BIGINT user_id FK
    ENUM provider
    VARCHAR provider_user_id
    VARCHAR email_at_provider
    VARCHAR access_token_hash
    VARCHAR refresh_token_hash
    DATETIME token_expires_at
    DATETIME created_at
  }

  GROUPS {
    BIGINT id PK
    VARCHAR name
    TEXT description
    BIGINT created_by FK
    DATETIME created_at
    DATETIME updated_at
  }

  GROUP_MEMBERS {
    BIGINT group_id FK
    BIGINT user_id FK
    ENUM role
    DATETIME joined_at
  }

  CATEGORIES {
    BIGINT id PK
    VARCHAR name
    VARCHAR icon
  }

  EXPENSES {
    BIGINT id PK
    BIGINT group_id FK
    BIGINT payer_user_id FK
    BIGINT category_id FK
    VARCHAR title
    DECIMAL total_amount
    CHAR currency
    DATE expense_date
    TEXT note
    ENUM split_mode
    DATETIME created_at
    DATETIME updated_at
  }

  EXPENSE_SHARES {
```
