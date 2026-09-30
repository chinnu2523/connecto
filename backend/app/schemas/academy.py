from datetime import datetime
from typing import Dict, List, Optional
from pydantic import BaseModel, ConfigDict, Field

class QuizQuestionResponse(BaseModel):
    id: str
    question_text: str
    options: List[str]

    model_config = ConfigDict(from_attributes=True)

class LessonSummary(BaseModel):
    id: str
    track_id: str
    title: str
    xp_reward: int
    order_index: int

    model_config = ConfigDict(from_attributes=True)

class LessonDetail(BaseModel):
    id: str
    track_id: str
    title: str
    content_markdown: str
    xp_reward: int
    order_index: int
    questions: List[QuizQuestionResponse]

    model_config = ConfigDict(from_attributes=True)

class TrackResponse(BaseModel):
    id: str
    title: str
    description: str
    icon: str
    lessons: List[LessonSummary]

    model_config = ConfigDict(from_attributes=True)

class QuizSubmission(BaseModel):
    selected_answers: Dict[str, int] # question_id -> selected_option_index

class QuizResultResponse(BaseModel):
    lesson_id: str
    passed: bool
    score_percentage: int
    xp_earned: int
    total_xp: int
    current_rank: str
    explanations: Dict[str, str] # question_id -> explanation text

class AcademyProfileResponse(BaseModel):
    user_id: str
    total_xp: int
    current_rank: str
    completed_lesson_ids: List[str]

    model_config = ConfigDict(from_attributes=True)
