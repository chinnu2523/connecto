export interface User {
  id: string;
  username: string;
  display_name: string;
  email: string;
  full_name?: string | null;
  location?: string | null;
  gender?: string | null;
  date_of_birth?: string | null;
  phone_number?: string | null;
  bio?: string | null;
  avatar_url: string | null;
  username_changed: boolean;
  two_factor_enabled?: boolean;
  two_factor_method?: string;
  requires_2fa?: boolean;
  masked_destination?: string;
  dev_otp?: string;
  is_online?: boolean;
  is_stealth?: boolean;
  is_recruiter: boolean;
  is_admin?: boolean;
  created_at: string;
}

export interface UserSignup {
  email: string;
  username: string;
  display_name: string;
  password: string;
}

export interface UserLogin {
  login: string;
  password: string;
}

export interface ProfileUpdate {
  display_name?: string;
  full_name?: string;
  location?: string;
  gender?: string;
  date_of_birth?: string;
  username?: string;
  bio?: string;
  phone_number?: string;
  email?: string;
  is_stealth?: boolean;
  is_online?: boolean;
}

export interface Server {
  id: string;
  name: string;
  icon_url: string | null;
  owner_id: string;
  created_at: string;
}

export interface Channel {
  id: string;
  server_id: string | null;
  name: string;
  type: 'text' | 'voice';
  created_at: string;
}

export interface Message {
  id: string;
  channel_id: string;
  sender_id: string;
  sender_username: string;
  sender_display_name: string;
  sender_avatar_url: string | null;
  content: string;
  attachments?: string[];
  nonce?: string;
  created_at: string;
  pending?: boolean;
}

export interface Friendship {
  id: string;
  user_id: string;
  friend_id: string;
  friend_username: string;
  friend_display_name: string;
  friend_avatar_url: string | null;
  status: 'pending' | 'accepted' | 'blocked';
  created_at: string;
}

export interface FriendRequestItem {
  id: string;
  sender_id: string;
  sender_username: string;
  sender_display_name: string;
  sender_avatar_url: string | null;
  created_at: string;
}

export interface QuizQuestion {
  id: string;
  question_text: string;
  options: string[];
}

export interface LessonSummary {
  id: string;
  track_id: string;
  title: string;
  xp_reward: number;
  order_index: number;
}

export interface LessonDetail {
  id: string;
  track_id: string;
  title: string;
  content_markdown: string;
  xp_reward: number;
  order_index: number;
  questions: QuizQuestion[];
}

export interface Track {
  id: string;
  title: string;
  description: string;
  icon: string;
  lessons: LessonSummary[];
}

export interface QuizResult {
  lesson_id: string;
  passed: boolean;
  score_percentage: number;
  xp_earned: number;
  total_xp: number;
  current_rank: string;
  explanations: Record<string, string>;
}

export interface AcademyProfile {
  user_id: string;
  total_xp: number;
  current_rank: string;
  completed_lesson_ids: string[];
}

// CAREERS TYPES
export interface JobOpening {
  id: string;
  title: string;
  department: string;
  location: string;
  description: string;
  required_keywords: string[];
  created_at: string;
}

export interface JobApplication {
  id: string;
  job_id: string;
  job_title: string;
  applicant_id: string;
  full_name: string;
  email: string;
  cv_file_url: string;
  ai_score: number;
  detected_skills: string[];
  summary: string;
  created_at: string;
}

export interface AppNotification {
  id: string;
  user_id: string;
  type: string;
  title: string;
  content: string;
  sender_username?: string | null;
  sender_avatar?: string | null;
  reference_id?: string | null;
  is_read: boolean;
  created_at: string;
}
