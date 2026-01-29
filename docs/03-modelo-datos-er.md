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
bigint id PK
varchar display_name
varchar email
varchar avatar_url
datetime created_at
datetime updated_at
}

AUTH_IDENTITIES {
bigint id PK
bigint user_id FK
enum provider
varchar provider_user_id
varchar email_at_provider
varchar access_token_hash
varchar refresh_token_hash
datetime token_expires_at
datetime created_at
}

GROUPS {
bigint id PK
varchar name
text description
bigint created_by FK
datetime created_at
datetime updated_at
}

GROUP_MEMBERS {
bigint group_id FK
bigint user_id FK
enum role
datetime joined_at
}

CATEGORIES {
bigint id PK
varchar name
varchar icon
}

EXPENSES {
bigint id PK
bigint group_id FK
bigint payer_user_id FK
bigint category_id FK
varchar title
decimal total_amount
char currency
date expense_date
text note
enum split_mode
datetime created_at
datetime updated_at
}

EXPENSE_SHARES {
bigint expense_id FK
bigint user_id FK
enum share_type
decimal amount_assigned
}

ATTACHMENTS {
bigint id PK
bigint expense_id FK
enum attachment_type
varchar file_path
varchar mime_type
int file_size
text ocr_text
datetime created_at
}
