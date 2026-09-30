import React, { useEffect, useState } from 'react';
import { Track, LessonDetail, QuizResult, AcademyProfile } from '../types';
import { getTracks, getLessonDetail, submitQuiz, getAcademyProgress } from '../services/api';
import { Shield, Award, BookOpen, CheckCircle, ArrowLeft, HelpCircle, Check, X, Sparkles } from 'lucide-react';

export const AcademyDashboard: React.FC = () => {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [progress, setProgress] = useState<AcademyProfile | null>(null);
  const [activeLesson, setActiveLesson] = useState<LessonDetail | null>(null);
  const [selectedAnswers, setSelectedAnswers] = useState<Record<string, number>>({});
  const [quizResult, setQuizResult] = useState<QuizResult | null>(null);
  const [submittingQuiz, setSubmittingQuiz] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAcademyData();
  }, []);

  const fetchAcademyData = async () => {
    setLoading(true);
    try {
      const [tList, prog] = await Promise.all([getTracks(), getAcademyProgress()]);
      setTracks(tList);
      setProgress(prog);
    } catch {
      // Ignore
    } finally {
      setLoading(false);
    }
  };

  const handleOpenLesson = async (lessonId: string) => {
    try {
      const detail = await getLessonDetail(lessonId);
      setActiveLesson(detail);
      setSelectedAnswers({});
      setQuizResult(null);
    } catch (err: any) {
      alert(err.message);
    }
  };

  const handleSelectOption = (questionId: string, optionIndex: number) => {
    setSelectedAnswers((prev) => ({
      ...prev,
      [questionId]: optionIndex,
    }));
  };

  const handleSubmitQuizForm = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeLesson) return;

    setSubmittingQuiz(true);
    try {
      const res = await submitQuiz(activeLesson.id, selectedAnswers);
      setQuizResult(res);
      // Refresh user academy profile
      const updatedProg = await getAcademyProgress();
      setProgress(updatedProg);
    } catch (err: any) {
      alert(err.message);
    } finally {
      setSubmittingQuiz(false);
    }
  };

  if (loading) {
    return (
      <div className="p-12 text-center text-cyanglow font-mono animate-pulse">
        Loading Cybersecurity Academy Curriculum...
      </div>
    );
  }

  return (
    <div className="w-full max-w-5xl mx-auto space-y-6">
      {/* Header Banner with Rank & XP Badges */}
      <div className="p-8 rounded-3xl glass-panel relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-electric via-cyanglow to-neonpink"></div>

        <div>
          <div className="flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 rounded-2xl bg-electric/20 border border-electric/40 flex items-center justify-center text-cyanglow">
              <Shield className="w-6 h-6" />
            </div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">Cyber Security Academy</h1>
          </div>
          <p className="text-sm text-slate-400">Master defensive and offensive security skills through hands-on labs and quizzes</p>
        </div>

        {progress && (
          <div className="flex items-center space-x-4 bg-darkcard/80 p-4 rounded-2xl border border-slate-800">
            <div className="text-right">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block">Rank Title</span>
              <span className="text-base font-extrabold text-cyanglow">{progress.current_rank}</span>
            </div>

            <div className="h-10 w-0.5 bg-slate-800"></div>

            <div className="flex items-center space-x-2 bg-gradient-to-r from-electric to-cyanglow px-4 py-2 rounded-xl text-white font-extrabold shadow-lg shadow-electric/20">
              <Award className="w-5 h-5" />
              <span>{progress.total_xp} XP</span>
            </div>
          </div>
        )}
      </div>

      {activeLesson ? (
        /* LESSON DETAIL & QUIZ VIEW */
        <div className="p-8 rounded-3xl glass-panel space-y-6">
          <button
            onClick={() => setActiveLesson(null)}
            className="flex items-center space-x-2 text-sm text-slate-400 hover:text-white font-semibold transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Curriculum Tracks</span>
          </button>

          <div className="border-b border-slate-800 pb-4">
            <div className="flex items-center space-x-2 text-xs font-bold text-cyanglow uppercase tracking-wider mb-1">
              <BookOpen className="w-4 h-4" />
              <span>Lesson Module</span>
            </div>
            <h2 className="text-2xl font-bold text-white">{activeLesson.title}</h2>
          </div>

          {/* Lesson Content Body */}
          <div className="p-6 rounded-2xl bg-darkcard/60 border border-slate-800/80 text-slate-200 text-sm leading-relaxed whitespace-pre-line font-mono">
            {activeLesson.content_markdown}
          </div>

          {/* Quiz Section */}
          <div className="pt-6 border-t border-slate-800 space-y-6">
            <h3 className="text-xl font-bold text-white flex items-center space-x-2">
              <HelpCircle className="w-5 h-5 text-cyanglow" />
              <span>Knowledge Assessment Quiz (+{activeLesson.xp_reward} XP)</span>
            </h3>

            {quizResult ? (
              /* Quiz Result Display */
              <div className="p-6 rounded-2xl glass-card space-y-4">
                <div
                  className={`p-4 rounded-xl border flex items-center space-x-3 ${
                    quizResult.passed
                      ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                      : 'bg-red-500/10 border-red-500/30 text-red-400'
                  }`}
                >
                  {quizResult.passed ? <Check className="w-6 h-6 shrink-0" /> : <X className="w-6 h-6 shrink-0" />}
                  <div>
                    <p className="font-bold text-base">
                      {quizResult.passed ? 'Quiz Passed! XP Awarded!' : 'Quiz Attempt Failed'}
                    </p>
                    <p className="text-xs">
                      Score: {quizResult.score_percentage}% • Total XP: {quizResult.total_xp} • Rank: {quizResult.current_rank}
                    </p>
                  </div>
                </div>

                <div className="space-y-3 pt-2">
                  <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Answer Explanations</h4>
                  {Object.entries(quizResult.explanations).map(([qId, expl]) => (
                    <div key={qId} className="p-3 rounded-xl bg-darkspace text-xs text-slate-300 border border-slate-800">
                      {expl}
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              /* Quiz Questions Form */
              <form onSubmit={handleSubmitQuizForm} className="space-y-6">
                {activeLesson.questions.map((q, idx) => (
                  <div key={q.id} className="p-6 rounded-2xl glass-card space-y-3">
                    <p className="font-bold text-white text-base">
                      {idx + 1}. {q.question_text}
                    </p>

                    <div className="space-y-2 pt-2">
                      {q.options.map((opt, optIdx) => (
                        <label
                          key={optIdx}
                          className={`flex items-center space-x-3 p-3.5 rounded-xl border cursor-pointer transition-all ${
                            selectedAnswers[q.id] === optIdx
                              ? 'bg-electric/20 border-cyanglow text-white font-semibold'
                              : 'bg-darkspace border-slate-800 text-slate-300 hover:bg-slate-800/50'
                          }`}
                        >
                          <input
                            type="radio"
                            name={`question_${q.id}`}
                            checked={selectedAnswers[q.id] === optIdx}
                            onChange={() => handleSelectOption(q.id, optIdx)}
                            className="hidden"
                          />
                          <div
                            className={`w-4 h-4 rounded-full border flex items-center justify-center ${
                              selectedAnswers[q.id] === optIdx
                                ? 'border-cyanglow bg-cyanglow'
                                : 'border-slate-600'
                            }`}
                          >
                            {selectedAnswers[q.id] === optIdx && <div className="w-1.5 h-1.5 rounded-full bg-darkspace" />}
                          </div>
                          <span className="text-sm">{opt}</span>
                        </label>
                      ))}
                    </div>
                  </div>
                ))}

                <button
                  type="submit"
                  disabled={submittingQuiz || Object.keys(selectedAnswers).length < activeLesson.questions.length}
                  className="w-full py-3.5 px-6 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 text-white font-bold rounded-xl transition-all shadow-lg shadow-electric/25 disabled:opacity-40"
                >
                  {submittingQuiz ? 'Evaluating Quiz Answers...' : 'Submit Quiz Answers'}
                </button>
              </form>
            )}
          </div>
        </div>
      ) : (
        /* TRACKS LIST VIEW */
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {tracks.map((track) => (
              <div key={track.id} className="p-6 rounded-3xl glass-panel space-y-4 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <div className="w-10 h-10 rounded-xl bg-electric/20 border border-electric/40 flex items-center justify-center text-cyanglow">
                      <Shield className="w-5 h-5" />
                    </div>
                    <span className="text-xs font-mono text-cyanglow font-semibold bg-cyanglow/10 px-2.5 py-1 rounded-full border border-cyanglow/20">
                      {track.lessons.length} Modules
                    </span>
                  </div>

                  <h3 className="text-xl font-bold text-white mb-1.5">{track.title}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{track.description}</p>
                </div>

                <div className="space-y-2 pt-2 border-t border-slate-800/80">
                  {track.lessons.map((lesson) => {
                    const isCompleted = progress?.completed_lesson_ids.includes(lesson.id);
                    return (
                      <button
                        key={lesson.id}
                        onClick={() => handleOpenLesson(lesson.id)}
                        className="w-full p-3 rounded-xl bg-darkcard/60 hover:bg-slate-800/80 border border-slate-800 flex items-center justify-between text-left transition-all group"
                      >
                        <div className="flex items-center space-x-3">
                          <BookOpen className="w-4 h-4 text-slate-400 group-hover:text-cyanglow" />
                          <span className="text-sm font-semibold text-slate-200 group-hover:text-white">
                            {lesson.title}
                          </span>
                        </div>

                        <div className="flex items-center space-x-2">
                          <span className="text-xs font-bold text-cyanglow">+{lesson.xp_reward} XP</span>
                          {isCompleted ? (
                            <CheckCircle className="w-4 h-4 text-emerald-400" />
                          ) : (
                            <Sparkles className="w-4 h-4 text-slate-600 group-hover:text-neonpink" />
                          )}
                        </div>
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
