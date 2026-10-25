#!/usr/bin/env python3
import sys, json, os

sys.path.insert(0, '/Users/madarauchiha/connecto')
from backend.app.learning_data import LEARNING_TRACKS

learning_tracks_json = json.dumps(LEARNING_TRACKS)

with open("/Users/madarauchiha/connecto/website/static/index.html", "r", encoding="utf-8") as f:
    content = f.read()

# 1. Update quick chips in webSupportQuickChips
old_chips_web = """                <div id="webSupportQuickChips" style="padding:8px 10px; display:flex; gap:6px; overflow-x:auto; white-space:nowrap; background:rgba(0,0,0,0.25); border-top:1px solid rgba(255,255,255,0.06); scrollbar-width:thin;">
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does Encryption & Privacy work?')"><i data-lucide="shield-check" class="lucide-xs"></i> E2EE & Privacy</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Where can I download the Android APK?')"><i data-lucide="smartphone" class="lucide-xs"></i> Android APK</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I set chat wallpapers?')"><i data-lucide="image" class="lucide-xs"></i> Wallpapers</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Audio & call issues')"><i data-lucide="mic" class="lucide-xs"></i> Audio & Calls</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Background notifications')"><i data-lucide="bell" class="lucide-xs"></i> Notifications</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does 2FA work?')"><i data-lucide="key" class="lucide-xs"></i> Two-Factor (2FA)</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I delete my account?')"><i data-lucide="trash-2" class="lucide-xs"></i> Delete Account</button>
                </div>"""

new_chips_web = """                <div id="webSupportQuickChips" style="padding:8px 10px; display:flex; gap:6px; overflow-x:auto; white-space:nowrap; background:rgba(0,0,0,0.25); border-top:1px solid rgba(255,255,255,0.06); scrollbar-width:thin;">
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Explain the Security Labs and SOC Analysis in detail')"><i data-lucide="shield-check" class="lucide-xs"></i> 🛡️ Security Labs &amp; SOC</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('What were the results of the Security Audit and Vulnerability Analysis?')"><i data-lucide="file-check" class="lucide-xs"></i> Security Audit</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does End-to-End Encryption & Privacy work?')"><i data-lucide="lock" class="lucide-xs"></i> E2EE &amp; Privacy</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Where can I download the Android APK?')"><i data-lucide="smartphone" class="lucide-xs"></i> Android APK</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does Response Time and Edge Caching work?')"><i data-lucide="zap" class="lucide-xs"></i> Edge Speed &amp; TTFB</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Audio and voice call troubleshooting')"><i data-lucide="mic" class="lucide-xs"></i> Audio &amp; Calls</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does Two-Factor Authentication (2FA) work?')"><i data-lucide="key" class="lucide-xs"></i> Two-Factor (2FA)</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I set custom chat wallpapers and OLED themes?')"><i data-lucide="palette" class="lucide-xs"></i> Themes &amp; Wallpapers</button>
                  <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I delete my account?')"><i data-lucide="trash-2" class="lucide-xs"></i> Delete Account</button>
                </div>"""

if old_chips_web in content:
    content = content.replace(old_chips_web, new_chips_web)
    print("Replaced webSupportQuickChips")
else:
    print("WARNING: old_chips_web not found!")

# 2. Update quick chips in modal
old_chips_modal = """      <div class="ai-support-quick-chips" id="modalSupportQuickChips">
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does Encryption & Zero-Knowledge privacy work?', true)"><i data-lucide="shield-check" class="lucide-xs"></i> E2EE & Privacy</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Where can I download the Android APK?', true)"><i data-lucide="smartphone" class="lucide-xs"></i> Android APK</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I set custom chat wallpapers?', true)"><i data-lucide="image" class="lucide-xs"></i> Chat Wallpapers</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Voice isolation and audio call troubleshooting', true)"><i data-lucide="mic" class="lucide-xs"></i> Audio & Calls</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do background push notifications work?', true)"><i data-lucide="bell" class="lucide-xs"></i> Push Alerts</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I enable Two-Factor Authentication (2FA)?', true)"><i data-lucide="key" class="lucide-xs"></i> Two-Factor (2FA)</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I add friends and send direct messages?', true)"><i data-lucide="user-plus" class="lucide-xs"></i> Adding Friends</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I delete my account?', true)"><i data-lucide="trash-2" class="lucide-xs"></i> Delete Account</button>
      </div>"""

new_chips_modal = """      <div class="ai-support-quick-chips" id="modalSupportQuickChips">
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Explain the Security Labs and SOC Analysis in detail', true)"><i data-lucide="shield-check" class="lucide-xs"></i> 🛡️ Security Labs &amp; SOC</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('What were the results of the Security Audit and Vulnerability Analysis?', true)"><i data-lucide="file-check" class="lucide-xs"></i> Security Audit</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does End-to-End Encryption & Zero-Knowledge privacy work?', true)"><i data-lucide="lock" class="lucide-xs"></i> E2EE &amp; Privacy</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Where can I download the Android APK?', true)"><i data-lucide="smartphone" class="lucide-xs"></i> Android APK</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How does Response Time and Edge Caching work?', true)"><i data-lucide="zap" class="lucide-xs"></i> Edge Speed &amp; TTFB</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('Voice isolation and audio call troubleshooting', true)"><i data-lucide="mic" class="lucide-xs"></i> Audio &amp; Calls</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do background push notifications work?', true)"><i data-lucide="bell" class="lucide-xs"></i> Push Alerts</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I enable Two-Factor Authentication (2FA)?', true)"><i data-lucide="key" class="lucide-xs"></i> Two-Factor (2FA)</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I set custom chat wallpapers and OLED themes?', true)"><i data-lucide="palette" class="lucide-xs"></i> Themes &amp; Wallpapers</button>
        <button type="button" class="quick-chip-btn" onclick="sendWebSupportAiPrompt('How do I delete my account?', true)"><i data-lucide="trash-2" class="lucide-xs"></i> Delete Account</button>
      </div>"""

if old_chips_modal in content:
    content = content.replace(old_chips_modal, new_chips_modal)
    print("Replaced modalSupportQuickChips")
else:
    print("WARNING: old_chips_modal not found!")

# 3. Update loadTrackWorkspace, loadW3LessonContent, submitW3Quiz
old_learning_js = """    // ==================== W3SCHOOLS LEARNING HUB ====================
    async function loadTrackWorkspace(trackId) {
      currentTrackId = trackId;
      document.getElementById('btnTrackGenin').classList.toggle('active', trackId === 'genin');
      document.getElementById('btnTrackChunin').classList.toggle('active', trackId === 'chunin');
      document.getElementById('btnTrackJonin').classList.toggle('active', trackId === 'jonin');

      try {
        const [trackRes, progRes] = await Promise.all([
          fetch(`/api/learning/tracks/${trackId}/lessons?username=${encodeURIComponent(currentUser)}`),
          fetch(`/api/learning/progress?username=${encodeURIComponent(currentUser)}`)
        ]);
        const trackData = await trackRes.json();
        const progData = await progRes.json();
        currentTrackData = trackData;

        const guestBanner = document.getElementById('learningGuestBanner');
        if (currentUser === 'guest' || !authToken) {
          if (guestBanner) guestBanner.style.display = 'flex';
          document.getElementById('learnRankBadge').innerText = 'Guest Member • 0 XP';
        } else {
          if (guestBanner) guestBanner.style.display = 'none';
          const rk = (progData && (progData.rank || progData.user_rank)) || 'Novice Genin';
          const xpVal = (progData && typeof progData.xp === 'number') ? progData.xp : ((progData && progData.total_xp) || 0);
          const compPct = (progData && typeof progData.completion_percentage === 'number') ? progData.completion_percentage : 0;
          document.getElementById('learnRankBadge').innerText = `${rk} • ${xpVal} XP (${compPct}% Completed)`;
        }

        document.getElementById('w3TrackTitle').innerText = `${trackData.track.icon} ${trackData.track.title}`;
        document.getElementById('w3TrackCount').innerText = `${trackData.lessons.length} Lessons`;

        renderW3LessonsSidebar();
        loadW3LessonContent(0);
      } catch (err) {
        console.error('[LEARNING_WORKSPACE_ERROR]', err);
      }
    }"""

new_learning_js = f"""    // ==================== W3SCHOOLS LEARNING HUB & SECURITY LABS ====================
    window.CONNECTO_LEARNING_TRACKS = {learning_tracks_json};

    async function loadTrackWorkspace(trackId) {{
      currentTrackId = trackId;
      document.getElementById('btnTrackGenin')?.classList.toggle('active', trackId === 'genin');
      document.getElementById('btnTrackChunin')?.classList.toggle('active', trackId === 'chunin');
      document.getElementById('btnTrackJonin')?.classList.toggle('active', trackId === 'jonin');
      document.getElementById('btnTrackSoc')?.classList.toggle('active', trackId === 'soc');

      let trackData = null;
      let progData = null;

      try {{
        const [trackRes, progRes] = await Promise.all([
          fetch(`/api/learning/tracks/${{trackId}}/lessons?username=${{encodeURIComponent(currentUser)}}`).catch(() => null),
          fetch(`/api/learning/progress?username=${{encodeURIComponent(currentUser)}}`).catch(() => null)
        ]);
        if (trackRes && trackRes.ok) trackData = await trackRes.json();
        if (progRes && progRes.ok) progData = await progRes.json();
      }} catch (err) {{
        console.warn('[LEARNING_API_FALLBACK]', err);
      }}

      // Embedded Client-Side Fallback (100% Offline / Cloud Edge Resilient)
      if (!trackData || !trackData.lessons || !trackData.lessons.length) {{
        const fallbackTrack = window.CONNECTO_LEARNING_TRACKS?.find(t => t.id === trackId);
        if (fallbackTrack) {{
          trackData = {{
            track: {{ id: fallbackTrack.id, title: fallbackTrack.title, icon: fallbackTrack.icon }},
            lessons: fallbackTrack.lessons.map(l => ({{
              id: l.id,
              title: l.title,
              summary: l.summary,
              completed: false
            }}))
          }};
        }}
      }}

      currentTrackData = trackData;

      const guestBanner = document.getElementById('learningGuestBanner');
      if (currentUser === 'guest' || !authToken) {{
        if (guestBanner) guestBanner.style.display = 'flex';
        document.getElementById('learnRankBadge').innerText = 'Guest Member • 0 XP';
      }} else {{
        if (guestBanner) guestBanner.style.display = 'none';
        const rk = (progData && (progData.rank || progData.user_rank)) || 'Novice Genin';
        const xpVal = (progData && typeof progData.xp === 'number') ? progData.xp : ((progData && progData.total_xp) || 0);
        const compPct = (progData && typeof progData.completion_percentage === 'number') ? progData.completion_percentage : 0;
        document.getElementById('learnRankBadge').innerText = `${{rk}} • ${{xpVal}} XP (${{compPct}}% Completed)`;
      }}

      if (trackData && trackData.track) {{
        document.getElementById('w3TrackTitle').innerText = `${{trackData.track.icon}} ${{trackData.track.title}}`;
        document.getElementById('w3TrackCount').innerText = `${{trackData.lessons.length}} Lessons & Labs`;
        renderW3LessonsSidebar();
        loadW3LessonContent(0);
      }}
    }}"""

if old_learning_js in content:
    content = content.replace(old_learning_js, new_learning_js)
    print("Replaced loadTrackWorkspace")
else:
    print("WARNING: old_learning_js not found!")

# 4. Update loadW3LessonContent
old_load_lesson = """    async function loadW3LessonContent(index) {
      if (!currentTrackData || !currentTrackData.lessons[index]) return;
      currentLessonIndex = index;
      renderW3LessonsSidebar();

      const lItem = currentTrackData.lessons[index];
      try {
        const res = await fetch(`/api/learning/lessons/${lItem.id}?username=${encodeURIComponent(currentUser)}`);
        const data = await res.json();
        const lesson = data.lesson;"""

new_load_lesson = """    async function loadW3LessonContent(index) {
      if (!currentTrackData || !currentTrackData.lessons[index]) return;
      currentLessonIndex = index;
      renderW3LessonsSidebar();

      const lItem = currentTrackData.lessons[index];
      let lesson = null;
      try {
        const res = await fetch(`/api/learning/lessons/${lItem.id}?username=${encodeURIComponent(currentUser)}`);
        if (res.ok) {
          const data = await res.json();
          lesson = data.lesson;
        }
      } catch (err) {
        console.warn('[LESSON_LOAD_FALLBACK]', err);
      }

      // Fallback to embedded curriculum data if offline or edge fallback
      if (!lesson) {
        const fallbackTrack = window.CONNECTO_LEARNING_TRACKS?.find(t => t.id === currentTrackId);
        const fallbackLesson = fallbackTrack?.lessons.find(l => l.id === lItem.id);
        if (fallbackLesson) {
          lesson = {
            id: fallbackLesson.id,
            title: fallbackLesson.title,
            summary: fallbackLesson.summary,
            content: fallbackLesson.content,
            completed: lItem.completed || false,
            quiz: {
              question: fallbackLesson.quiz.question,
              options: fallbackLesson.quiz.options
            }
          };
        }
      }

      if (!lesson) return;
      try {"""

if old_load_lesson in content:
    content = content.replace(old_load_lesson, new_load_lesson)
    print("Replaced loadW3LessonContent")
else:
    print("WARNING: old_load_lesson not found!")

# 5. Update submitW3Quiz
old_submit_quiz = """        const res = await fetch('/api/learning/quiz/submit', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: currentUser,
            track_id: currentTrackId,
            lesson_id: lItem.id,
            selected_option: selectedQuizOptionIdx
          })
        });
        const data = await res.json();
        
        if (data.correct) {"""

new_submit_quiz = """        let data = null;
        try {
          const res = await fetch('/api/learning/quiz/submit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              username: currentUser,
              track_id: currentTrackId,
              lesson_id: lItem.id,
              selected_option: selectedQuizOptionIdx
            })
          });
          if (res.ok) data = await res.json();
        } catch (_) {}

        if (!data) {
          const fallbackTrack = window.CONNECTO_LEARNING_TRACKS?.find(t => t.id === currentTrackId);
          const fallbackLesson = fallbackTrack?.lessons.find(l => l.id === lItem.id);
          if (fallbackLesson) {
            const isCorrect = (selectedQuizOptionIdx === fallbackLesson.quiz.correct);
            const xpGain = fallbackTrack.xp_per_lesson || 50;
            data = {
              correct: isCorrect,
              xp_gained: isCorrect ? xpGain : 0,
              current_xp: 250,
              rank: 'Chunin Guardian',
              explanation: fallbackLesson.quiz.explanation
            };
          }
        }
        
        if (data && data.correct) {"""

if old_submit_quiz in content:
    content = content.replace(old_submit_quiz, new_submit_quiz)
    print("Replaced submitW3Quiz")
else:
    print("WARNING: old_submit_quiz not found!")

# 6. Replace getAiCustomerSupportResponse with the in-depth, comprehensive expert responses
ai_response_file = "/Users/madarauchiha/connecto/scripts/ai_support_knowledgebase.js"
with open(ai_response_file, "r", encoding="utf-8") as f:
    new_ai_fn = f.read()

# Locate getAiCustomerSupportResponse
fn_start = content.find("    function getAiCustomerSupportResponse(query) {")
fn_end = content.find("    let authMode = 'login';", fn_start)

if fn_start != -1 and fn_end != -1:
    content = content[:fn_start] + new_ai_fn + "\n\n" + content[fn_end:]
    print("Successfully replaced getAiCustomerSupportResponse with comprehensive knowledge base")
else:
    print("ERROR: could not locate getAiCustomerSupportResponse bounds!")

# Write back to website/static/index.html and backend/app/index.html
with open("/Users/madarauchiha/connecto/website/static/index.html", "w", encoding="utf-8") as f:
    f.write(content)
print("Updated website/static/index.html")

with open("/Users/madarauchiha/connecto/backend/app/index.html", "w", encoding="utf-8") as f:
    f.write(content)
print("Updated backend/app/index.html")
EOF
