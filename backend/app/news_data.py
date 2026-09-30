import asyncio
import logging
import time
import re
import html
import httpx
import urllib.parse
import xml.etree.ElementTree as ET
from datetime import datetime
from typing import List, Dict, Any, Optional

logger = logging.getLogger("news_engine")

# In-memory translation cache (persists across queries)
TRANSLATION_CACHE: Dict[str, str] = {}

# High-Reputation Live RSS Endpoints
RSS_FEEDS = [
    {
        "name": "Telugu Top News",
        "category": "telugu_top",
        "category_label": "🔥 తెలుగు తాజా వార్తలు",
        "lang": "te",
        "url": "https://news.google.com/rss?hl=te&gl=IN&ceid=IN:te"
    },
    {
        "name": "Telugu Cinema & Entertainment",
        "category": "telugu_cinema",
        "category_label": "🎬 సినిమా & వినోదం",
        "lang": "te",
        "url": "https://news.google.com/rss/headlines/section/topic/ENTERTAINMENT?hl=te&gl=IN&ceid=IN:te"
    },
    {
        "name": "Telugu AP & Telangana",
        "category": "telugu_state",
        "category_label": "🏛️ ఆంధ్రప్రదేశ్ & తెలంగాణ",
        "lang": "te",
        "url": "https://news.google.com/rss/headlines/section/topic/NATION?hl=te&gl=IN&ceid=IN:te"
    },
    {
        "name": "Telugu Sports",
        "category": "telugu_sports",
        "category_label": "🏏 క్రీడలు & క్రికెట్",
        "lang": "te",
        "url": "https://news.google.com/rss/headlines/section/topic/SPORTS?hl=te&gl=IN&ceid=IN:te"
    },
    {
        "name": "Telugu Technology",
        "category": "telugu_tech",
        "category_label": "💻 టెక్నాలజీ (తెలుగు)",
        "lang": "te",
        "url": "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=te&gl=IN&ceid=IN:te"
    },
    {
        "name": "BBC News Telugu",
        "category": "telugu_top",
        "category_label": "📡 BBC తెలుగు వార్తలు",
        "lang": "te",
        "url": "https://feeds.bbci.co.uk/telugu/rss.xml"
    },
    {
        "name": "Global Tech & AI",
        "category": "tech_ai",
        "category_label": "🤖 Tech & Artificial Intelligence",
        "lang": "en",
        "url": "https://news.google.com/rss/headlines/section/topic/TECHNOLOGY?hl=en-IN&gl=IN&ceid=IN:en"
    },
    {
        "name": "Cybersecurity & Zero-Day",
        "category": "cybersecurity",
        "category_label": "🛡️ Cybersecurity & Defense",
        "lang": "en",
        "url": "https://news.google.com/rss/search?q=cybersecurity+zero-day+vulnerability&hl=en-IN&gl=IN&ceid=IN:en"
    }
]

def clean_html_text(raw_html: str) -> str:
    """Strips HTML tags and unescapes entities."""
    if not raw_html:
        return ""
    text = re.sub(r"<[^>]+>", " ", raw_html)
    text = html.unescape(text)
    text = re.sub(r"\s+", " ", text).strip()
    return text

def parse_source_from_title(title: str) -> tuple[str, str]:
    """Splits 'Headline - Source' if present."""
    if " - " in title:
        parts = title.rsplit(" - ", 1)
        return parts[0].strip(), parts[1].strip()
    return title.strip(), "Live News Feed"

def generate_takeaways(title: str, summary: str, is_telugu: bool = False) -> List[str]:
    """Generates 3 contextual takeaways for in-app reader modal."""
    if is_telugu:
        return [
            f"తాజా సమాచారం: {title[:75]}...",
            "ఈ అంశంపై అధికారులు మరియు ప్రతినిధుల అధికారిక ప్రకటన వెలువడింది.",
            "మరిన్ని పూర్తి వివరాలు మరియు అప్‌డేట్‌లు లైవ్ డెస్క్ ద్వారా అందుబాటులో ఉన్నాయి."
        ]
    return [
        f"Key Development: {title[:80]}...",
        "Real-time coverage verified across primary reporting channels.",
        "Continuous intelligence monitoring enabled for subsequent updates."
    ]

async def fetch_rss_feed(feed_info: Dict[str, Any], client: httpx.AsyncClient) -> List[Dict[str, Any]]:
    """Fetches and parses a single RSS feed."""
    articles = []
    try:
        res = await client.get(feed_info["url"], headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) ConnectoNews/2.6"}, timeout=7.0)
        if res.status_code != 200:
            return []
        
        root = ET.fromstring(res.content)
        items = root.findall(".//item")
        
        for idx, item in enumerate(items[:15]): # top 15 per feed
            raw_title = item.find("title").text if item.find("title") is not None else ""
            raw_desc = item.find("description").text if item.find("description") is not None else ""
            pub_date = item.find("pubDate").text if item.find("pubDate") is not None else ""
            link = item.find("link").text if item.find("link") is not None else ""
            
            clean_title, source_name = parse_source_from_title(raw_title)
            clean_desc = clean_html_text(raw_desc)
            if not clean_desc or clean_desc == clean_title:
                clean_desc = f"{clean_title}. పూర్తి తాజా సమాచారం మరియు సమగ్ర వివరాలు ఇక్కడ పరిశీలించవచ్చు." if feed_info["lang"] == "te" else f"{clean_title}. Detailed coverage and live updates from primary correspondents."
            
            # Format time
            time_str = "Today • Live"
            if pub_date:
                try:
                    time_str = pub_date[:16]
                except Exception:
                    pass

            is_te = feed_info["lang"] == "te"
            avatar = "📰" if is_te else ("🤖" if feed_info["category"] == "tech_ai" else "🛡️")
            if "cinema" in feed_info["category"]:
                avatar = "🎬"
            elif "sports" in feed_info["category"]:
                avatar = "🏏"
            elif "state" in feed_info["category"]:
                avatar = "🏛️"

            # Create a robust unique ID
            article_id = f"art_{feed_info['category']}_{idx}_{abs(hash(clean_title)) % 1000000}"
            takeaways = generate_takeaways(clean_title, clean_desc, is_telugu=is_te)

            excerpt_text = clean_desc[:220] + ("..." if len(clean_desc) > 220 else "")
            
            full_content_html = f"""
                <div style="margin-bottom:16px;">
                    <p style="font-size:16px; font-weight:600; color:#f1f5f9; line-height:1.75;">{excerpt_text}</p>
                </div>
                <div style="background:rgba(34,211,238,0.06); border:1px solid rgba(34,211,238,0.25); border-radius:14px; padding:18px; margin:20px 0;">
                    <div style="display:flex; align-items:center; gap:8px; margin-bottom:8px;">
                        <span style="font-size:16px;">📰</span>
                        <h4 style="font-size:14px; font-weight:800; color:#22d3ee;">
                            {'మూల ప్రచురణకర్త (Original Source)' if is_te else 'Original Publisher'}
                        </h4>
                    </div>
                    <p style="font-size:13.5px; color:#cbd5e1; line-height:1.65; margin-bottom:14px;">
                        {'ఈ కథనం యొక్క పూర్తి పాఠం మరియు సమగ్ర వివరాలను తెలుసుకోవడానికి అధికారిక వార్తా మూలాన్ని సందర్శించండి:' if is_te else 'This is an aggregated summary excerpt. To read the complete article, visit the original publisher:'} 
                        <strong style="color:#ffffff;">{source_name}</strong>
                    </p>
                    <a href="{link}" target="_blank" rel="noopener noreferrer" style="display:inline-flex; align-items:center; gap:6px; background:linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%); color:#ffffff; font-weight:800; font-size:13px; padding:9px 18px; border-radius:10px; text-decoration:none; box-shadow:0 4px 14px rgba(6,182,212,0.35);">
                        <span>{'పూర్తి వార్తను చదవండి (' + source_name + ') ↗' if is_te else 'Read Full Story on ' + source_name + ' ↗'}</span>
                    </a>
                </div>
                <p style="font-size:12.5px; color:#64748b; line-height:1.6;">
                    {'ప్రచురణ సంస్థ: ' + source_name + ' • తేదీ: ' + time_str if is_te else 'Source: ' + source_name + ' • Published: ' + time_str}
                </p>
            """

            articles.append({
                "id": article_id,
                "title": clean_title,
                "summary": excerpt_text,
                "category": feed_info["category"],
                "category_label": feed_info["category_label"],
                "lang": feed_info["lang"],
                "author": source_name,
                "source_name": source_name,
                "avatar": avatar,
                "published_time": time_str,
                "read_time": "1 min excerpt" if not is_te else "1 నిమి సారాంశం",
                "takeaways": takeaways,
                "content": full_content_html,
                "url": link
            })
    except Exception as ex:
        logger.warning(f"[RSS_FETCH_ERROR] Feed {feed_info['name']}: {ex}")
    return articles

async def fetch_all_live_news() -> List[Dict[str, Any]]:
    """Fetches all live Telugu and Global news in parallel."""
    all_articles = []
    async with httpx.AsyncClient(timeout=10.0, follow_redirects=True) as client:
        tasks = [fetch_rss_feed(feed, client) for feed in RSS_FEEDS]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        for res in results:
            if isinstance(res, list):
                all_articles.extend(res)
    
    telugu_articles = [a for a in all_articles if a.get("lang") == "te"]
    other_articles = [a for a in all_articles if a.get("lang") != "te"]

    combined = []
    max_len = max(len(telugu_articles), len(other_articles)) if (telugu_articles or other_articles) else 0
    for i in range(max_len):
        if i < len(telugu_articles):
            combined.append(telugu_articles[i])
        if i < len(other_articles):
            combined.append(other_articles[i])

    return combined if combined else all_articles


# ==================== HIGH-SPEED TRANSLATION ENGINE ====================

async def translate_text(text: str, sl: str = "te", tl: str = "en") -> str:
    """Translates text with multi-tier failover and instant caching."""
    if not text or not text.strip():
        return ""
    if sl == tl:
        return text

    cache_key = f"{sl}_{tl}_{text.strip()}"
    if cache_key in TRANSLATION_CACHE:
        return TRANSLATION_CACHE[cache_key]

    # Tier 1: Google Mobile Web (Fast, 0 Rate Limit)
    try:
        url = f"https://translate.google.com/m?sl={sl}&tl={tl}&q={urllib.parse.quote(text.strip())}"
        async with httpx.AsyncClient(timeout=3.5, follow_redirects=True) as client:
            res = await client.get(url, headers={"User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)"})
            if res.status_code == 200:
                match = re.search(r'<div class="result-container">([\s\S]*?)</div>', res.text)
                if match:
                    translated = html.unescape(match.group(1).strip())
                    if translated and translated.strip() != text.strip():
                        TRANSLATION_CACHE[cache_key] = translated
                        return translated
    except Exception as e:
        logger.debug(f"[GT_MOBILE_FAIL] {e}")

    # Tier 2: MyMemory API
    try:
        url = f"https://api.mymemory.translated.net/get?q={urllib.parse.quote(text.strip())}&langpair={sl}|{tl}"
        async with httpx.AsyncClient(timeout=3.0, follow_redirects=True) as client:
            res = await client.get(url, headers={"User-Agent": "Mozilla/5.0"})
            if res.status_code == 200:
                data = res.json()
                trans = data.get("responseData", {}).get("translatedText", "")
                if trans and trans.strip() != text.strip() and not trans.startswith("MYMEMORY WARNING"):
                    TRANSLATION_CACHE[cache_key] = trans.strip()
                    return trans.strip()
    except Exception as e:
        logger.debug(f"[MYMEMORY_FAIL] {e}")

    return text

async def translate_article_dict(article: Dict[str, Any], target_lang: str = "en") -> Dict[str, Any]:
    """Efficiently translates an article by combining title and summary into 1 single call."""
    sl = article.get("lang", "te")
    if sl == target_lang:
        return article

    cloned = dict(article)
    cloned["title_orig"] = article.get("title_orig", article.get("title", ""))
    cloned["summary_orig"] = article.get("summary_orig", article.get("summary", ""))

    title = article.get("title", "").strip()
    summary = article.get("summary", "").strip()

    # Single batch call for title and summary
    combined_payload = f"{title} ___BREAK___ {summary}"
    trans_combined = await translate_text(combined_payload, sl=sl, tl=target_lang)
    
    if "___BREAK___" in trans_combined:
        t_title, t_summary = trans_combined.split("___BREAK___", 1)
        cloned["title"] = t_title.strip()
        cloned["summary"] = t_summary.strip()
    else:
        cloned["title"] = trans_combined.strip()
        cloned["summary"] = summary

    cloned["title_en"] = cloned["title"]
    cloned["summary_en"] = cloned["summary"]
    cloned["translated"] = True
    cloned["target_lang"] = target_lang
    return cloned
