-- users + auth
create table users
(
    id                 uuid primary key,
    email              varchar(255) unique not null,
    password_hash      varchar(255)        not null,
    roles              varchar(255)        not null,
    phone_country_code varchar(63)         not null,
    phone_number        varchar(63) unique  not null,
    user_name          varchar(63)         not null,
    is_2fa_enabled     boolean             not null default false,
    created_at         timestamp           not null default now()
);

create table oauth_accounts
(
    id               uuid primary key,
    provider         varchar(50)  not null,
    provider_user_id varchar(255) not null,
    user_id          uuid         not null references users (id) on delete cascade
);

create table refresh_tokens
(
    id               uuid primary key,
    user_id          uuid         not null references users (id) on delete cascade,
    token_hash       varchar(255) not null,
    issued_at        timestamp    not null default now(),
    expires_at       timestamp    not null,
    revoked_at       timestamp,
    rotation_counter int          not null default 0
);

-- catalog
create table brands
(
    id   uuid primary key,
    name varchar(120)        not null,
    slug varchar(140) unique not null
);

create table categories
(
    id        uuid primary key,
    name      varchar(120)        not null,
    slug      varchar(140) unique not null,
    parent_id uuid references categories (id)
);

create table products
(
    id          uuid primary key,
    name        varchar(200)        not null,
    slug        varchar(220) unique not null,
    description text,
    price       numeric(12, 2)      not null,
    stock_qty   int                 not null default 0,
    weight      numeric(10, 3),
    width       numeric(10, 3),
    height      numeric(10, 3),
    length      numeric(10, 3),
    brand_id    uuid references brands (id),
    created_at  timestamp           not null default now()
);

create table product_categories
(
    product_id  uuid not null references products (id) on delete cascade,
    category_id uuid not null references categories (id) on delete cascade,
    primary key (product_id, category_id)
);

create table product_images
(
    id         uuid primary key,
    product_id uuid not null references products (id) on delete cascade,
    url        text not null,
    alt        varchar(255),
    position   int  not null default 0
);

-- Index
create index idx_products_name on products (name);
create index idx_products_price on products (price);
create index idx_brands_slug on brands (slug);
create index idx_categories_slug on categories (slug);
