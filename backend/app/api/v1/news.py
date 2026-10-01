import os
import json
import asyncio
import logging
from typing import Optional, List, Dict, Any
from fastapi import APIRouter, Header, Query, Response, HTTPException
from pydantic import BaseModel
from app.news_data import fetch_all_live_news, translate_text, translate_article_dict

logger = logging.getLogger("news.api")
from app.core.security import verify_access_jwt
router = APIRouter(tags=["News Intelligence"])

DATA_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "data", "app_database.json")

NEWS_CACHE: List[Dict[str, Any]] = []

def _load_stored_articles() -> List[Dict[str, Any]]:
    global NEWS_CACHE
    if NEWS_CACHE:
        return NEWS_CACHE
    if os.path.exists(DATA_FILE):
        try:
            with open(DATA_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                articles = data.get("news_articles", [])
                if articles:
                    NEWS_CACHE = articles
                    return NEWS_CACHE
        except Exception as e:
            logger.warning(f"Failed to load news from DB: {e}")
    return []

def _save_stored_articles(articles: List[Dict[str, Any]]):
    global NEWS_CACHE
    NEWS_CACHE = articles
    if os.path.exists(DATA_FILE):
        try:
            with open(DATA_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
            data["news_articles"] = articles
            with open(DATA_FILE, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning(f"Failed to save news to DB: {e}")

class NewsTranslateRequest(BaseModel):
    text: Optional[str] = None
    article: Optional[Dict[str, Any]] = None
    article_id: Optional[str] = None
    source: Optional[str] = "auto"
    target: Optional[str] = "en"

@router.get("/api/news")
@router.get("/api/news/feed")
async def get_news_feed(
    response: Response,
    category: Optional[str] = Query("all"),
    search: Optional[str] = Query(None),
    lang_mode: Optional[str] = Query("all"),
    limit: int = Query(100, ge=1, le=200)
):
    response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate, max-age=0"
    response.headers["Pragma"] = "no-cache"
    response.headers["Expires"] = "0"
    response.headers["Access-Control-Allow-Origin"] = "*"

    articles = _load_stored_articles()
    if not articles:
        articles = await fetch_all_live_news()
        _save_stored_articles(articles)

    filtered = articles
    if category and category.lower() != "all":
        cat_lower = category.lower()
        filtered = [a for a in filtered if a.get("category", "").lower() == cat_lower]

    LANG_CODE_MAP = {
        "english_only": "en",
        "hindi_only": "hi",
        "tamil_only": "ta",
        "kannada_only": "kn",
        "malayalam_only": "ml",
        "marathi_only": "mr",
        "bengali_only": "bn",
        "gujarati_only": "gu",
        "punjabi_only": "pa",
        "odia_only": "or"
    }

    if lang_mode == "telugu_only":
        filtered = [a for a in filtered if a.get("lang") == "te"]
    elif lang_mode in LANG_CODE_MAP:
        t_target = LANG_CODE_MAP[lang_mode]
        top_slice = filtered[:min(limit, 30)]
        tasks = []
        for a in top_slice:
            if a.get("lang") == "te" or a.get("lang") != t_target:
                tasks.append(translate_article_dict(a, target_lang=t_target))
            else:
                tasks.append(asyncio.sleep(0, result=a))
        translated_top = await asyncio.gather(*tasks)
        filtered = list(translated_top) + filtered[len(top_slice):]

    if search:
        s_lower = search.lower().strip()
        filtered = [
            a for a in filtered
            if s_lower in a.get("title", "").lower() or s_lower in a.get("summary", "").lower() or s_lower in a.get("author", "").lower()
        ]

    return {
        "status": "ok",
        "total": len(filtered),
        "articles": filtered[:limit],
        "lang_mode": lang_mode
    }

@router.post("/api/news/translate")
async def translate_news_endpoint(req: NewsTranslateRequest):
    target_lang = req.target or "en"
    if req.article_id:
        articles = _load_stored_articles()
        if not articles:
            articles = await fetch_all_live_news()
            _save_stored_articles(articles)

        target_art = next((a for a in articles if a.get("id") == req.article_id), None)
        if not target_art:
            raise HTTPException(status_code=404, detail="Article not found")

        translated_art = await translate_article_dict(target_art, target_lang=target_lang)
        return {"status": "ok", "article": translated_art}

    if req.article:
        translated_art = await translate_article_dict(req.article, target_lang=target_lang)
        return {"status": "ok", "article": translated_art}

    if req.text:
        translated_str = await translate_text(req.text, sl=req.source or "te", tl=target_lang)
        return {"status": "ok", "translated_text": translated_str, "source": req.source, "target": target_lang}

    raise HTTPException(status_code=400, detail="Provide either 'text', 'article', or 'article_id'")

@router.get("/api/news/breaking")
async def get_breaking_news():
    articles = _load_stored_articles()
    if not articles:
        articles = await fetch_all_live_news()
        _save_stored_articles(articles)

    breaking = [
        {"id": a.get("id"), "title": a.get("title"), "category": a.get("category", "tech")}
        for a in articles[:10]
    ]
    return {"status": "ok", "breaking": breaking}

@router.get("/api/news/article/{article_id}")
async def get_news_article(article_id: str):
    articles = _load_stored_articles()
    if not articles:
        articles = await fetch_all_live_news()
        _save_stored_articles(articles)

    for a in articles:
        if a.get("id") == article_id:
            return {"status": "ok", "article": a}

    raise HTTPException(status_code=404, detail="Article not found")

@router.post("/api/news/refresh")
async def refresh_live_news(authorization: Optional[str] = Header(None)):
    auth_jwt = authorization[7:] if authorization and authorization.startswith("Bearer ") else ""
    user_payload = verify_access_jwt(auth_jwt) if auth_jwt else None
    if not (user_payload and user_payload.get("role") == "admin"):
        raise HTTPException(status_code=403, detail="Forbidden: Admin authorization required to trigger news refresh.")
    try:
        live = await fetch_all_live_news()
        if live:
            _save_stored_articles(live)
        return {"status": "ok", "total_articles": len(live), "live_fetched": len(live)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
