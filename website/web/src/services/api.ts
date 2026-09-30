import {
  User, UserSignup, UserLogin, ProfileUpdate,
  Server, Channel, Message, Friendship, FriendRequestItem,
  Track, LessonDetail, QuizResult, AcademyProfile,
  JobOpening, JobApplication, AppNotification
} from '../types';

const API_BASE = '/api/v1';

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let errorDetail = 'An error occurred';
    try {
      const data = await res.json();
      errorDetail = data.detail || JSON.stringify(data);
    } catch {
      errorDetail = res.statusText;
    }
    throw new Error(errorDetail);
  }
  return res.json();
}

// AUTH & USERS
export async function checkUsernameAvailability(username: string): Promise<{ available: boolean; valid: boolean; message: string }> {
  const res = await fetch(`/api/auth/check-username?username=${encodeURIComponent(username)}`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<{ available: boolean; valid: boolean; message: string }>(res);
}

export async function checkEmailAvailability(email: string): Promise<{ available: boolean; valid: boolean; message: string }> {
  const res = await fetch(`/api/auth/check-email?email=${encodeURIComponent(email)}`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<{ available: boolean; valid: boolean; message: string }>(res);
}

export async function signup(data: UserSignup): Promise<User> {
  const res = await fetch(`${API_BASE}/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data),
  });
  return handleResponse<User>(res);
}

export async function login(data: UserLogin): Promise<User> {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data),
  });
  return handleResponse<User>(res);
}

export async function verifyLogin2Fa(login: string, otp_code: string): Promise<User> {
  const res = await fetch(`${API_BASE}/auth/login/2fa-verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ login, otp_code }),
  });
  return handleResponse<User>(res);
}

export async function logout(): Promise<{ message: string }> {
  const res = await fetch(`${API_BASE}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
  return handleResponse<{ message: string }>(res);
}

export async function getProfile(): Promise<User> {
  const res = await fetch(`${API_BASE}/users/me`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<User>(res);
}

export async function updateProfile(data: ProfileUpdate): Promise<User> {
  const res = await fetch(`${API_BASE}/users/me`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data),
  });
  return handleResponse<User>(res);
}

export async function uploadAvatar(file: File): Promise<User> {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${API_BASE}/users/me/avatar`, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  });
  return handleResponse<User>(res);
}

export async function requestContactChangeOtp(new_contact: string, method: 'email' | 'sms'): Promise<{ status: string; message: string; masked_destination?: string; dev_otp?: string }> {
  const res = await fetch(`${API_BASE}/users/me/change-contact/request-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ new_contact, method }),
  });
  return handleResponse<{ status: string; message: string; masked_destination?: string; dev_otp?: string }>(res);
}

export async function verifyContactChangeOtp(new_contact: string, method: 'email' | 'sms', otp_code: string): Promise<User> {
  const res = await fetch(`${API_BASE}/users/me/change-contact/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ new_contact, method, otp_code }),
  });
  return handleResponse<User>(res);
}

// CHAT, SERVERS & CHANNELS
export async function getServers(): Promise<Server[]> {
  const res = await fetch(`${API_BASE}/chat/servers`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<Server[]>(res);
}

export async function createServer(name: string): Promise<Server> {
  const res = await fetch(`${API_BASE}/chat/servers`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ name }),
  });
  return handleResponse<Server>(res);
}

export async function getChannels(serverId: string): Promise<Channel[]> {
  const res = await fetch(`${API_BASE}/chat/servers/${serverId}/channels`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<Channel[]>(res);
}

export async function createChannel(serverId: string, name: string, type: 'text' | 'voice'): Promise<Channel> {
  const res = await fetch(`${API_BASE}/chat/servers/${serverId}/channels`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ name, type }),
  });
  return handleResponse<Channel>(res);
}

export async function startDM(targetUsername: string): Promise<Channel> {
  const res = await fetch(`${API_BASE}/chat/dm/start?target_username=${encodeURIComponent(targetUsername)}`, {
    method: 'POST',
    credentials: 'include',
  });
  return handleResponse<Channel>(res);
}

export async function getMessages(channelId: string): Promise<Message[]> {
  const res = await fetch(`${API_BASE}/chat/channels/${channelId}/messages`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<Message[]>(res);
}

export async function sendMessage(channelId: string, content: string, nonce: string): Promise<Message> {
  const res = await fetch(`${API_BASE}/chat/channels/${channelId}/messages`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ content, nonce }),
  });
  return handleResponse<Message>(res);
}

export async function getPublicChannels(): Promise<Channel[]> {
  const res = await fetch(`${API_BASE}/chat/channels`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<Channel[]>(res);
}

export async function getFriends(): Promise<Friendship[]> {
  const res = await fetch(`${API_BASE}/chat/friends`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<Friendship[]>(res);
}

export async function getReceivedFriendRequests(): Promise<FriendRequestItem[]> {
  const res = await fetch(`${API_BASE}/chat/friends/requests/received`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<FriendRequestItem[]>(res);
}

export async function sendFriendRequest(friendUsername: string): Promise<Friendship> {
  const res = await fetch(`${API_BASE}/chat/friends/request`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ friend_username: friendUsername }),
  });
  return handleResponse<Friendship>(res);
}

export async function acceptFriendRequest(requestId: string): Promise<Friendship> {
  const res = await fetch(`${API_BASE}/chat/friends/requests/${requestId}/accept`, {
    method: 'POST',
    credentials: 'include',
  });
  return handleResponse<Friendship>(res);
}

export async function declineFriendRequest(requestId: string): Promise<{ status: string; id: string }> {
  const res = await fetch(`${API_BASE}/chat/friends/requests/${requestId}/decline`, {
    method: 'POST',
    credentials: 'include',
  });
  return handleResponse<{ status: string; id: string }>(res);
}

// ACADEMY
export async function getTracks(): Promise<Track[]> {
  const res = await fetch(`${API_BASE}/academy/tracks`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<Track[]>(res);
}

export async function getLessonDetail(lessonId: string): Promise<LessonDetail> {
  const res = await fetch(`${API_BASE}/academy/lessons/${lessonId}`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<LessonDetail>(res);
}

export async function submitQuiz(lessonId: string, selectedAnswers: Record<string, number>): Promise<QuizResult> {
  const res = await fetch(`${API_BASE}/academy/lessons/${lessonId}/quiz`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ selected_answers: selectedAnswers }),
  });
  return handleResponse<QuizResult>(res);
}

export async function getAcademyProgress(): Promise<AcademyProfile> {
  const res = await fetch(`${API_BASE}/academy/me/progress`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<AcademyProfile>(res);
}

// CAREERS & CV SCREENING
export async function getJobOpenings(): Promise<JobOpening[]> {
  const res = await fetch(`${API_BASE}/careers/jobs`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<JobOpening[]>(res);
}

export async function applyForJob(jobId: string, fullName: string, email: string, cvFile: File): Promise<JobApplication> {
  const formData = new FormData();
  formData.append('full_name', fullName);
  formData.append('email', email);
  formData.append('file', cvFile);

  const res = await fetch(`${API_BASE}/careers/jobs/${jobId}/apply`, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  });
  return handleResponse<JobApplication>(res);
}

export async function getRecruiterApplications(): Promise<JobApplication[]> {
  const res = await fetch(`${API_BASE}/careers/recruiter/applications`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<JobApplication[]>(res);
}

// NOTIFICATIONS
export async function getNotifications(unreadOnly = false): Promise<AppNotification[]> {
  const res = await fetch(`${API_BASE}/notifications?unread_only=${unreadOnly}`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<AppNotification[]>(res);
}

export async function getUnreadNotificationCount(): Promise<{ unread_count: number }> {
  const res = await fetch(`${API_BASE}/notifications/count`, {
    method: 'GET',
    credentials: 'include',
  });
  return handleResponse<{ unread_count: number }>(res);
}

export async function markNotificationRead(id: string): Promise<{ success: boolean }> {
  const res = await fetch(`${API_BASE}/notifications/${id}/read`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({}),
  });
  return handleResponse<{ success: boolean }>(res);
}

export async function markAllNotificationsRead(): Promise<{ success: boolean }> {
  const res = await fetch(`${API_BASE}/notifications/read-all`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({}),
  });
  return handleResponse<{ success: boolean }>(res);
}

export async function clearNotifications(): Promise<{ success: boolean }> {
  const res = await fetch(`${API_BASE}/notifications`, {
    method: 'DELETE',
    credentials: 'include',
  });
  return handleResponse<{ success: boolean }>(res);
}
