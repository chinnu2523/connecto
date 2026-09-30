from datetime import datetime, timezone
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user, get_current_user_optional
from app.db.models.user import User
from app.db.models.academy import (
    CourseTrack, Lesson, QuizQuestion, UserProgress, UserAcademyProfile, calculate_rank_title
)
from app.schemas.academy import (
    TrackResponse, LessonDetail, QuizSubmission, QuizResultResponse, AcademyProfileResponse
)

router = APIRouter(prefix="/academy", tags=["Academy & Learning Curriculum"])

async def ensure_default_curriculum_seeded(db: AsyncSession):
    """Seeds real cybersecurity curriculum into database if empty."""
    stmt = select(CourseTrack)
    existing = (await db.execute(stmt)).scalars().first()
    if existing:
        return

    # Seed Track 1: Web Application Security
    t1 = CourseTrack(
        title="Web Application Security & OWASP",
        description="Master web vulnerabilities, SQL injection, XSS, and secure coding practices.",
        icon="Shield"
    )
    db.add(t1)
    await db.commit()
    await db.refresh(t1)

    l1 = Lesson(
        track_id=t1.id,
        title="SQL Injection & Input Sanitization",
        content_markdown="""# SQL Injection (SQLi) & Defense

SQL Injection occurs when untrusted user input is directly concatenated into SQL queries without parameterization.

### Vulnerable Pattern:
```sql
SELECT * FROM users WHERE username = '` + userInput + `' AND password = '` + pwd + `'
```

### Defense Standard:
Always use parameterized prepared statements (e.g. SQLAlchemy ORM or PDO in PHP).
""",
        xp_reward=150,
        order_index=1
    )
    db.add(l1)
    await db.commit()
    await db.refresh(l1)

    q1 = QuizQuestion(
        lesson_id=l1.id,
        question_text="What is the primary defense against SQL Injection vulnerabilities?",
        options=[
            "Client-side JavaScript validation",
            "Parameterized prepared statements / ORM queries",
            "Encoding HTML entities",
            "Using MD5 hashes"
        ],
        correct_option_index=1,
        explanation="Parameterized prepared statements ensure input is treated purely as data, preventing SQL payload execution."
    )
    db.add(q1)

    # Seed Track 2: Network Reconnaissance
    t2 = CourseTrack(
        title="Network Reconnaissance & Analysis",
        description="Learn port scanning, protocol inspection, and packet analysis techniques.",
        icon="Radio"
    )
    db.add(t2)
    await db.commit()
    await db.refresh(t2)

    l2 = Lesson(
        track_id=t2.id,
        title="Nmap Scanning & Packet Analysis",
        content_markdown="""# Nmap & Network Reconnaissance

Nmap (Network Mapper) is an open-source utility for network discovery and vulnerability auditing.

### Common Scan Modes:
- **SYN Stealth Scan**: `nmap -sS <target>`
- **Service Version Detection**: `nmap -sV <target>`
""",
        xp_reward=150,
        order_index=1
    )
    db.add(l2)
    await db.commit()
    await db.refresh(l2)

    q2 = QuizQuestion(
        lesson_id=l2.id,
        question_text="Which Nmap flag performs service version detection on open ports?",
        options=["-sU", "-sV", "-O", "-A"],
        correct_option_index=1,
        explanation="-sV probes open ports to determine service and version information."
    )
    db.add(q2)

    await db.commit()

@router.get("/tracks", response_model=List[TrackResponse])
async def list_course_tracks(
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: AsyncSession = Depends(get_db)
):
    await ensure_default_curriculum_seeded(db)
    stmt = (
        select(CourseTrack)
        .options(selectinload(CourseTrack.lessons))
        .order_by(CourseTrack.created_at.asc())
    )
    res = await db.execute(stmt)
    return res.scalars().all()

@router.get("/lessons/{lesson_id}", response_model=LessonDetail)
async def get_lesson_detail(
    lesson_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = (
        select(Lesson)
        .options(selectinload(Lesson.questions))
        .where(Lesson.id == lesson_id)
    )
    lesson = (await db.execute(stmt)).scalar_one_or_none()
    if not lesson:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Lesson not found.")
    return lesson

@router.post("/lessons/{lesson_id}/quiz", response_model=QuizResultResponse)
async def submit_quiz(
    lesson_id: str,
    submission: QuizSubmission,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Lesson).where(Lesson.id == lesson_id)
    lesson = (await db.execute(stmt)).scalar_one_or_none()
    if not lesson:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Lesson not found.")

    q_stmt = select(QuizQuestion).where(QuizQuestion.lesson_id == lesson_id)
    questions = (await db.execute(q_stmt)).scalars().all()

    if not questions:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="No quiz questions for this lesson.")

    correct_count = 0
    explanations = {}

    for q in questions:
        user_choice = submission.selected_answers.get(q.id)
        explanations[q.id] = q.explanation or "No explanation provided."
        if user_choice is not None and user_choice == q.correct_option_index:
            correct_count += 1

    score_pct = int((correct_count / len(questions)) * 100)
    passed = score_pct >= 70

    xp_earned = 0
    if passed:
        prog_stmt = select(UserProgress).where(
            UserProgress.user_id == current_user.id,
            UserProgress.lesson_id == lesson_id
        )
        existing_prog = (await db.execute(prog_stmt)).scalar_one_or_none()

        if not existing_prog:
            xp_earned = lesson.xp_reward
            prog = UserProgress(
                user_id=current_user.id,
                lesson_id=lesson_id,
                completed=True,
                score_percentage=score_pct,
                xp_earned=xp_earned
            )
            db.add(prog)

            acad_stmt = select(UserAcademyProfile).where(UserAcademyProfile.user_id == current_user.id)
            acad_profile = (await db.execute(acad_stmt)).scalar_one_or_none()

            if not acad_profile:
                acad_profile = UserAcademyProfile(
                    user_id=current_user.id,
                    total_xp=xp_earned,
                    current_rank=calculate_rank_title(xp_earned)
                )
                db.add(acad_profile)
            else:
                acad_profile.total_xp += xp_earned
                acad_profile.current_rank = calculate_rank_title(acad_profile.total_xp)

            await db.commit()

    acad_stmt = select(UserAcademyProfile).where(UserAcademyProfile.user_id == current_user.id)
    acad_profile = (await db.execute(acad_stmt)).scalar_one_or_none()
    total_xp = acad_profile.total_xp if acad_profile else 0
    rank = acad_profile.current_rank if acad_profile else "Security Apprentice"

    return QuizResultResponse(
        lesson_id=lesson_id,
        passed=passed,
        score_percentage=score_pct,
        xp_earned=xp_earned,
        total_xp=total_xp,
        current_rank=rank,
        explanations=explanations
    )

@router.get("/me/progress", response_model=AcademyProfileResponse)
async def get_my_academy_progress(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    acad_stmt = select(UserAcademyProfile).where(UserAcademyProfile.user_id == current_user.id)
    acad_profile = (await db.execute(acad_stmt)).scalar_one_or_none()

    prog_stmt = select(UserProgress.lesson_id).where(
        UserProgress.user_id == current_user.id,
        UserProgress.completed == True
    )
    completed_ids = (await db.execute(prog_stmt)).scalars().all()

    total_xp = acad_profile.total_xp if acad_profile else 0
    current_rank = acad_profile.current_rank if acad_profile else "Security Apprentice"

    return AcademyProfileResponse(
        user_id=current_user.id,
        total_xp=total_xp,
        current_rank=current_rank,
        completed_lesson_ids=list(completed_ids)
    )
