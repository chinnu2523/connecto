"""
app/core/tasks.py — Safe background task helpers.

Wraps asyncio.create_task() with exception logging so fire-and-forget
tasks no longer silently swallow errors.

Usage:
    from app.core.tasks import safe_task
    safe_task(ws_manager.broadcast_global({...}))
"""
import asyncio
import logging

_logger = logging.getLogger("connecto.tasks")


async def _guarded(coro, task_name: str):
    try:
        await coro
    except asyncio.CancelledError:
        raise
    except Exception as exc:
        _logger.error(f"[BACKGROUND_TASK_ERROR] {task_name}: {exc}", exc_info=True)


def safe_task(coro, *, name: str = "") -> asyncio.Task:
    """
    Schedule a coroutine as an asyncio background task with full exception logging.
    Returns the Task object (can be ignored).
    """
    task_name = name or getattr(coro, "__name__", repr(coro))
    return asyncio.create_task(_guarded(coro, task_name), name=task_name)
