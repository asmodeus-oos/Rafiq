begin;

create extension if not exists pgcrypto;
create extension if not exists "uuid-ossp";

-- ------------------------------------------------------------
-- core schema cleanup
-- ------------------------------------------------------------

do $$
declare
  r record;
begin
  for r in
    select policyname
    from pg_policies
    where schemaname = 'public'
      and tablename = 'comments'
  loop
    execute format('drop policy if exists %I on public.comments', r.policyname);
  end loop;
end $$;

alter table public.comments
  alter column id type uuid using id::uuid,
  alter column post_id type uuid using post_id::uuid,
  alter column user_id type uuid using user_id::uuid,
  alter column parent_id type uuid using parent_id::uuid;

alter table public.notifications
  drop constraint if exists notifications_comment_id_fkey;

alter table public.notifications
  rename column comment_id to comment_id_text;

alter table public.notifications
  add column if not exists comment_id uuid;

update public.notifications
set comment_id = nullif(comment_id_text, '')::uuid;

alter table public.notifications
  drop column if exists comment_id_text;

-- ------------------------------------------------------------
-- foreign keys
-- ------------------------------------------------------------

alter table public.comments
  drop constraint if exists comments_post_id_fkey,
  drop constraint if exists comments_user_id_fkey,
  drop constraint if exists comments_parent_id_fkey;

alter table public.comments
  add constraint comments_post_id_fkey
    foreign key (post_id) references public.posts(id) on delete cascade,
  add constraint comments_user_id_fkey
    foreign key (user_id) references public.users(id) on delete cascade,
  add constraint comments_parent_id_fkey
    foreign key (parent_id) references public.comments(id) on delete cascade;

alter table public.notifications
  add constraint notifications_comment_id_fkey
    foreign key (comment_id) references public.comments(id) on delete cascade;

-- ------------------------------------------------------------
-- uniqueness and indexes
-- ------------------------------------------------------------

create unique index if not exists likes_user_post_uidx
  on public.likes (user_id, post_id);

create unique index if not exists user_swipes_swiper_target_uidx
  on public.user_swipes (swiper_id, target_id);

create unique index if not exists followers_follower_following_uidx
  on public.followers (follower_id, following_id);

create unique index if not exists message_reactions_message_user_uidx
  on public.message_reactions (message_id, user_id);

create index if not exists idx_posts_user_id_created_at
  on public.posts (user_id, created_at desc);

create index if not exists idx_likes_post_id_created_at
  on public.likes (post_id, created_at desc);

create index if not exists idx_comments_post_id_created_at
  on public.comments (post_id, timestamp desc);

create index if not exists idx_comments_parent_id
  on public.comments (parent_id);

create index if not exists idx_notifications_recipient_read_created_at
  on public.notifications (recipient_id, read, created_at desc);

create index if not exists idx_notifications_recipient_created_at
  on public.notifications (recipient_id, created_at desc);

create index if not exists idx_followers_follower_id
  on public.followers (follower_id);

create index if not exists idx_followers_following_id
  on public.followers (following_id);

create index if not exists idx_messages_sender_receiver_created_at
  on public.messages (sender_id, receiver_id, created_at desc);

create index if not exists idx_messages_receiver_read_created_at
  on public.messages (receiver_id, is_read, created_at desc);

create index if not exists idx_voice_rooms_active_created_at
  on public.voice_rooms (is_active, created_at desc);

create index if not exists idx_stories_user_id_expires_at
  on public.stories (user_id, expires_at desc);

create index if not exists idx_call_queue_gender_entered_at
  on public.call_queue (gender, entered_at desc);

-- ------------------------------------------------------------
-- count maintenance
-- ------------------------------------------------------------

create or replace function public.refresh_post_like_count()
returns trigger
language plpgsql
as $$
declare
  v_post_id uuid;
begin
  v_post_id := coalesce(new.post_id, old.post_id);

  update public.posts p
  set likes_count = (
    select count(*)
    from public.likes l
    where l.post_id = v_post_id
  )
  where p.id = v_post_id;

  return null;
end;
$$;

drop trigger if exists trg_refresh_post_like_count on public.likes;
create trigger trg_refresh_post_like_count
after insert or delete or update of post_id
on public.likes
for each row
execute function public.refresh_post_like_count();

create or replace function public.refresh_post_comment_count()
returns trigger
language plpgsql
as $$
declare
  v_post_id uuid;
begin
  v_post_id := coalesce(new.post_id, old.post_id);

  update public.posts p
  set comments_count = (
    select count(*)
    from public.comments c
    where c.post_id = v_post_id
  )
  where p.id = v_post_id;

  return null;
end;
$$;

drop trigger if exists trg_refresh_post_comment_count on public.comments;
create trigger trg_refresh_post_comment_count
after insert or delete or update of post_id
on public.comments
for each row
execute function public.refresh_post_comment_count();

create or replace function public.refresh_follower_counts()
returns trigger
language plpgsql
as $$
declare
  v_follower_id uuid;
  v_following_id uuid;
begin
  v_follower_id := coalesce(new.follower_id, old.follower_id);
  v_following_id := coalesce(new.following_id, old.following_id);

  update public.users u
  set followers_count = (
    select count(*)
    from public.followers f
    where f.following_id = v_following_id
  )
  where u.id = v_following_id;

  update public.users u
  set following_count = (
    select count(*)
    from public.followers f
    where f.follower_id = v_follower_id
  )
  where u.id = v_follower_id;

  return null;
end;
$$;

drop trigger if exists trg_refresh_follower_counts on public.followers;
create trigger trg_refresh_follower_counts
after insert or delete or update of follower_id, following_id
on public.followers
for each row
execute function public.refresh_follower_counts();

-- ------------------------------------------------------------
-- notifications
-- ------------------------------------------------------------

create or replace function public.create_like_notification()
returns trigger
language plpgsql
as $$
declare
  v_post_owner uuid;
begin
  select p.user_id
  into v_post_owner
  from public.posts p
  where p.id = new.post_id;

  if v_post_owner is not null and v_post_owner <> new.user_id then
    insert into public.notifications (
      recipient_id,
      sender_id,
      type,
      post_id,
      read,
      created_at
    )
    values (
      v_post_owner,
      new.user_id,
      'LIKE',
      new.post_id,
      false,
      now()
    );
  end if;

  return null;
end;
$$;

drop trigger if exists trg_create_like_notification on public.likes;
create trigger trg_create_like_notification
after insert
on public.likes
for each row
execute function public.create_like_notification();

create or replace function public.create_comment_notification()
returns trigger
language plpgsql
as $$
declare
  v_post_owner uuid;
begin
  select p.user_id
  into v_post_owner
  from public.posts p
  where p.id = new.post_id;

  if v_post_owner is not null and v_post_owner <> new.user_id then
    insert into public.notifications (
      recipient_id,
      sender_id,
      type,
      post_id,
      comment_id,
      read,
      created_at
    )
    values (
      v_post_owner,
      new.user_id,
      'COMMENT',
      new.post_id,
      new.id,
      false,
      now()
    );
  end if;

  return null;
end;
$$;

drop trigger if exists trg_create_comment_notification on public.comments;
create trigger trg_create_comment_notification
after insert
on public.comments
for each row
execute function public.create_comment_notification();

create or replace function public.create_follow_notification()
returns trigger
language plpgsql
as $$
begin
  if new.following_id is not null and new.following_id <> new.follower_id then
    insert into public.notifications (
      recipient_id,
      sender_id,
      type,
      read,
      created_at
    )
    values (
      new.following_id,
      new.follower_id,
      'FOLLOW',
      false,
      now()
    );
  end if;

  return null;
end;
$$;

drop trigger if exists trg_create_follow_notification on public.followers;
create trigger trg_create_follow_notification
after insert
on public.followers
for each row
execute function public.create_follow_notification();

create or replace function public.create_message_notification()
returns trigger
language plpgsql
as $$
begin
  if new.receiver_id is not null and new.receiver_id <> new.sender_id then
    insert into public.notifications (
      recipient_id,
      sender_id,
      type,
      read,
      created_at
    )
    values (
      new.receiver_id,
      new.sender_id,
      case
        when coalesce(new.is_voice, false) then 'VOICE_MESSAGE'
        else 'MESSAGE'
      end,
      false,
      now()
    );
  end if;

  return null;
end;
$$;

drop trigger if exists trg_create_message_notification on public.messages;
create trigger trg_create_message_notification
after insert
on public.messages
for each row
execute function public.create_message_notification();

-- ------------------------------------------------------------
-- backfill counts
-- ------------------------------------------------------------

update public.posts p
set
  likes_count = (
    select count(*)
    from public.likes l
    where l.post_id = p.id
  ),
  comments_count = (
    select count(*)
    from public.comments c
    where c.post_id = p.id
  );

update public.users u
set
  followers_count = (
    select count(*)
    from public.followers f
    where f.following_id = u.id
  ),
  following_count = (
    select count(*)
    from public.followers f
    where f.follower_id = u.id
  );

-- ------------------------------------------------------------
-- realtime publication
-- ------------------------------------------------------------

do $$
begin
  execute 'alter publication supabase_realtime add table public.notifications';
exception
  when duplicate_object then null;
end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.messages';
exception
  when duplicate_object then null;
end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.likes';
exception
  when duplicate_object then null;
end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.comments';
exception
  when duplicate_object then null;
end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.followers';
exception
  when duplicate_object then null;
end $$;

commit;
