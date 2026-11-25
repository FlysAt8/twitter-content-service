-- Сообщения
CREATE TABLE IF NOT EXISTS messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    text        VARCHAR(280) NOT NULL,
    author_id   UUID         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- Сообщения автора (профиль) и общая сортировка
CREATE INDEX IF NOT EXISTS idx_messages_author_created ON messages (author_id, created_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_messages_created        ON messages (created_at DESC, id DESC);

-- Подписки: follower_id подписан на following_id
CREATE TABLE IF NOT EXISTS subscriptions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id   UUID      NOT NULL,
    following_id  UUID      NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_subscriptions_pair   UNIQUE (follower_id, following_id),
    CONSTRAINT chk_subscriptions_self  CHECK (follower_id <> following_id)
);

-- uq_subscriptions_pair уже покрывает поиск по follower_id; нужен ещё по following_id
CREATE INDEX IF NOT EXISTS idx_subscriptions_following ON subscriptions (following_id, created_at DESC);
