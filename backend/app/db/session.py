import sqlite3
from sqlalchemy import event
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import declarative_base
from app.core.config import settings

Base = declarative_base()

engine = create_async_engine(
    settings.DATABASE_URL,
    echo=False,
    future=True
)

@event.listens_for(engine.sync_engine, "connect")
def set_sqlite_pragma(dbapi_connection, connection_record):
    if isinstance(dbapi_connection, sqlite3.Connection):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA journal_mode=WAL")
        cursor.execute("PRAGMA synchronous=NORMAL")
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.execute("PRAGMA mmap_size=268435456")       # 256MB zero-copy memory mapping
        cursor.execute("PRAGMA cache_size=-64000")         # 64MB dedicated RAM page cache
        cursor.execute("PRAGMA busy_timeout=10000")        # 10s wait for locks under burst writes
        cursor.execute("PRAGMA temp_store=MEMORY")         # RAM temp store for sorts & joins
        cursor.execute("PRAGMA wal_autocheckpoint=2000")  # Checkpoint interval
        cursor.execute("PRAGMA threads=4")                 # Multi-threaded query execution
        cursor.close()

AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False
)

async def init_db():
    """Initializes database tables and migrates schema non-destructively."""
    import app.db.models.user  # noqa
    import app.db.models.chat  # noqa
    import app.db.models.academy  # noqa
    import app.db.models.careers  # noqa
    import app.db.models.voice  # noqa
    import app.db.models.notification  # noqa
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        
        # SQLite dynamic column additions for users table if needed
        def migrate_sqlite_columns(connection):
            try:
                res = connection.exec_driver_sql("PRAGMA table_info(users)").fetchall()
                existing_cols = {row[1] for row in res}
                if "phone_number" not in existing_cols:
                    connection.exec_driver_sql("ALTER TABLE users ADD COLUMN phone_number VARCHAR(32)")
                if "two_factor_enabled" not in existing_cols:
                    connection.exec_driver_sql("ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT 0 NOT NULL")
                if "two_factor_method" not in existing_cols:
                    connection.exec_driver_sql("ALTER TABLE users ADD COLUMN two_factor_method VARCHAR(16) DEFAULT 'sms' NOT NULL")
                if "banner_url" not in existing_cols:
                    connection.exec_driver_sql("ALTER TABLE users ADD COLUMN banner_url VARCHAR(512)")
            except Exception as e:
                print(f"User schema migration check: {e}")

        await conn.run_sync(migrate_sqlite_columns)

