import React, { useState } from 'react';
import { User } from '../types';
import { updateProfile, uploadAvatar, logout, requestContactChangeOtp, verifyContactChangeOtp } from '../services/api';
import {
  User as UserIcon,
  Shield,
  Camera,
  Lock,
  CheckCircle,
  AlertCircle,
  LogOut,
  Edit3,
  MapPin,
  Mail,
  Phone,
  KeyRound,
  ShieldCheck,
  X
} from 'lucide-react';

interface ProfileDashboardProps {
  user: User;
  onUserUpdated: (user: User) => void;
  onLogout: () => void;
}

export const ProfileDashboard: React.FC<ProfileDashboardProps> = ({ user, onUserUpdated, onLogout }) => {
  // Personal Details state (Genuinely editable)
  const [fullName, setFullName] = useState(user.full_name || '');
  const [displayName, setDisplayName] = useState(user.display_name || '');
  const [location, setLocation] = useState(user.location || '');
  const [bio, setBio] = useState(user.bio || '');

  const [updating, setUpdating] = useState(false);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const [msg, setMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Contact Change Flow (Email / Phone OTP verification)
  const [contactModalTarget, setContactModalTarget] = useState<'email' | 'sms' | null>(null);
  const [newContactInput, setNewContactInput] = useState('');
  const [otpCodeInput, setOtpCodeInput] = useState('');
  const [otpStep, setOtpStep] = useState<'request' | 'verify'>('request');
  const [contactLoading, setContactLoading] = useState(false);
  const [contactModalMsg, setContactModalMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setUpdating(true);
    setMsg(null);

    try {
      const updatedUser = await updateProfile({
        full_name: fullName.trim(),
        display_name: displayName.trim() || user.username,
        location: location.trim(),
        bio: bio.trim(),
      });
      onUserUpdated(updatedUser);
      setMsg({ type: 'success', text: 'Profile details updated and saved successfully!' });
    } catch (err: any) {
      setMsg({ type: 'error', text: err.message || 'Failed to update profile' });
    } finally {
      setUpdating(false);
    }
  };

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadingAvatar(true);
    setMsg(null);

    try {
      const updatedUser = await uploadAvatar(file);
      onUserUpdated(updatedUser);
      setMsg({ type: 'success', text: 'Avatar uploaded & validated successfully!' });
    } catch (err: any) {
      setMsg({ type: 'error', text: err.message || 'Avatar upload failed' });
    } finally {
      setUploadingAvatar(false);
    }
  };

  const handleLogoutClick = async () => {
    try {
      await logout();
    } catch {
      // Ignore
    }
    onLogout();
  };

  // Open OTP Change Modal
  const openContactModal = (method: 'email' | 'sms') => {
    setContactModalTarget(method);
    setNewContactInput('');
    setOtpCodeInput('');
    setOtpStep('request');
    setContactModalMsg(null);
  };

  const closeContactModal = () => {
    setContactModalTarget(null);
    setContactModalMsg(null);
    setNewContactInput('');
    setOtpCodeInput('');
  };

  const handleRequestContactOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!contactModalTarget || !newContactInput.trim()) return;

    setContactLoading(true);
    setContactModalMsg(null);
    try {
      const res = await requestContactChangeOtp(newContactInput.trim(), contactModalTarget);
      setOtpStep('verify');
      setContactModalMsg({
        type: 'success',
        text: res.message || ('Verification OTP sent to ' + newContactInput.trim()),
      });
    } catch (err: any) {
      setContactModalMsg({ type: 'error', text: err.message || 'Failed to send verification code.' });
    } finally {
      setContactLoading(false);
    }
  };

  const handleVerifyContactOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!contactModalTarget || !newContactInput.trim() || !otpCodeInput.trim()) return;

    setContactLoading(true);
    setContactModalMsg(null);
    try {
      const updatedUser = await verifyContactChangeOtp(
        newContactInput.trim(),
        contactModalTarget,
        otpCodeInput.trim()
      );
      onUserUpdated(updatedUser);
      closeContactModal();
      setMsg({
        type: 'success',
        text: (contactModalTarget === 'email' ? 'Email address' : 'Phone number') + ' verified and updated successfully!',
      });
    } catch (err: any) {
      setContactModalMsg({ type: 'error', text: err.message || 'Invalid or expired OTP code.' });
    } finally {
      setContactLoading(false);
    }
  };

  return (
    <div className="w-full max-w-2xl mx-auto space-y-6">
      {/* Header Banner */}
      <div className="p-8 rounded-3xl glass-panel relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-electric via-cyanglow to-neonpink"></div>

        <div className="flex items-center space-x-6">
          {/* Avatar Upload Dropzone */}
          <div className="relative group">
            <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-electric via-cyanglow to-neonpink p-1 shadow-xl">
              <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center overflow-hidden relative">
                {user.avatar_url ? (
                  <img src={user.avatar_url} alt={user.display_name} className="w-full h-full object-cover" />
                ) : (
                  <UserIcon className="w-10 h-10 text-slate-400" />
                )}
                {uploadingAvatar && (
                  <div className="absolute inset-0 bg-darkspace/80 flex items-center justify-center text-xs text-cyanglow font-semibold">
                    Validating...
                  </div>
                )}
              </div>
            </div>

            <label className="absolute bottom-0 right-0 w-8 h-8 rounded-full bg-electric hover:bg-cyanglow text-white flex items-center justify-center cursor-pointer transition-colors shadow-lg">
              <Camera className="w-4 h-4" />
              <input type="file" accept="image/png,image/jpeg,image/webp" onChange={handleAvatarChange} className="hidden" />
            </label>
          </div>

          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-2xl font-bold text-white">{user.display_name || user.username}</h1>
              <Shield className="w-5 h-5 text-cyanglow" />
            </div>
            
            <div className="flex items-center space-x-2 mt-1">
              <p className="text-sm font-mono text-cyanglow">@{user.username}</p>
              <span className="text-slate-600">•</span>
              {/* Live Presence Indicator */}
              {user.is_stealth ? (
                <span className="inline-flex items-center space-x-1.5 px-2.5 py-0.5 rounded-full bg-slate-800 border border-slate-700 text-slate-400 text-xs font-semibold">
                  <span>👤 Stealth Mode (Invisible)</span>
                </span>
              ) : (
                <span className="inline-flex items-center space-x-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-xs font-semibold">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                  <span>Online (Active Session)</span>
                </span>
              )}
            </div>

            <p className="text-xs text-slate-400 mt-1">{user.email}</p>
            {user.bio && (
              <p className="text-xs text-slate-300 italic mt-1 max-w-sm">"{user.bio}"</p>
            )}
          </div>
        </div>

        <button
          onClick={handleLogoutClick}
          className="flex items-center space-x-2 px-4 py-2.5 bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 rounded-xl transition-all text-sm font-semibold"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>

      {/* Alert Messages */}
      {msg && (
        <div
          className={
            msg.type === 'success'
              ? 'p-4 rounded-xl border flex items-start space-x-3 text-sm bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
              : 'p-4 rounded-xl border flex items-start space-x-3 text-sm bg-red-500/10 border-red-500/30 text-red-400'
          }
        >
          {msg.type === 'success' ? <CheckCircle className="w-5 h-5 shrink-0" /> : <AlertCircle className="w-5 h-5 shrink-0" />}
          <span>{msg.text}</span>
        </div>
      )}

      {/* Edit Profile Form (Genuinely Editable Fields) */}
      <div className="p-8 rounded-3xl glass-card space-y-6">
        <h3 className="text-lg font-bold text-white flex items-center space-x-2">
          <Edit3 className="w-5 h-5 text-electric" />
          <span>Profile Settings</span>
        </h3>

        <form onSubmit={handleUpdateProfile} className="space-y-5">
          {/* Full Name (Genuinely Editable) */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Full Name
              </label>
              <span className="inline-flex items-center space-x-1 text-xs text-cyanglow font-semibold px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20">
                <Edit3 className="w-3 h-3" />
                <span>EDITABLE</span>
              </span>
            </div>
            <div className="relative">
              <input
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Shadow Hayate"
                className="w-full px-4 py-2.5 bg-darkspace/80 border border-slate-700/60 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow text-sm font-medium"
              />
            </div>
            <span className="text-[11px] text-slate-500 mt-1 block">Your real name or full shinobi identity.</span>
          </div>

          {/* Display Name (Genuinely Editable) */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Display Name / Nickname
              </label>
              <span className="inline-flex items-center space-x-1 text-xs text-cyanglow font-semibold px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20">
                <Edit3 className="w-3 h-3" />
                <span>EDITABLE</span>
              </span>
            </div>
            <input
              type="text"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              placeholder="e.g. Shadow"
              className="w-full px-4 py-2.5 bg-darkspace/80 border border-slate-700/60 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow text-sm font-medium"
            />
            <span className="text-[11px] text-slate-500 mt-1 block">Name shown on the leaderboard and community channels.</span>
          </div>

          {/* City & Country / Region (Genuinely Editable) */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider flex items-center space-x-1.5">
                <MapPin className="w-3.5 h-3.5 text-slate-400" />
                <span>City & Country / Region</span>
              </label>
              <span className="inline-flex items-center space-x-1 text-xs text-cyanglow font-semibold px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20">
                <Edit3 className="w-3 h-3" />
                <span>EDITABLE</span>
              </span>
            </div>
            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g. Tokyo, Japan / Highland District"
              className="w-full px-4 py-2.5 bg-darkspace/80 border border-slate-700/60 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow text-sm font-medium"
            />
            <span className="text-[11px] text-slate-500 mt-1 block">Used for clan region identification and local time alignment.</span>
          </div>

          {/* Username (Locked Policy: 1-Time Change) */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Username
              </label>
              <span className="inline-flex items-center space-x-1 text-xs text-amber-400 font-semibold px-2 py-0.5 rounded bg-amber-400/10 border border-amber-400/20">
                <Lock className="w-3 h-3" />
                <span>{user.username_changed ? 'LOCKED (PERMANENT)' : '1-TIME CHANGE'}</span>
              </span>
            </div>
            <input
              type="text"
              disabled
              value={'@' + user.username}
              className="w-full px-4 py-2.5 bg-slate-900/40 border border-slate-800 rounded-xl text-slate-400 cursor-not-allowed font-mono text-sm"
            />
          </div>

          {/* Personal Bio (Genuinely Editable) */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Personal Bio
              </label>
              <span className="inline-flex items-center space-x-1 text-xs text-cyanglow font-semibold px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20">
                <Edit3 className="w-3 h-3" />
                <span>EDITABLE</span>
              </span>
            </div>
            <textarea
              rows={3}
              maxLength={255}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              placeholder="Tell everyone about yourself, interests, or tactical role..."
              className="w-full px-4 py-2.5 bg-darkspace/80 border border-slate-700/60 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow resize-none text-sm"
            />
            <div className="flex justify-between text-[11px] text-slate-500 mt-1">
              <span>Full Name, Display Name, City/Country, and Bio are freely editable anytime.</span>
              <span>{bio.length}/255</span>
            </div>
          </div>

          <button
            type="submit"
            disabled={updating}
            className="w-full py-3 px-4 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 text-white font-bold rounded-xl transition-all shadow-lg shadow-electric/25 disabled:opacity-40 flex items-center justify-center space-x-2"
          >
            <CheckCircle className="w-4 h-4" />
            <span>{updating ? 'Saving Changes...' : 'Save Profile Changes'}</span>
          </button>
        </form>
      </div>

      {/* Account Security & Verified Contacts Card */}
      <div className="p-8 rounded-3xl glass-card space-y-6">
        <h3 className="text-lg font-bold text-white flex items-center space-x-2">
          <ShieldCheck className="w-5 h-5 text-emerald-400" />
          <span>Verified Contacts & OTP Security</span>
        </h3>

        <div className="space-y-4">
          {/* Email Address */}
          <div className="p-4 rounded-2xl bg-slate-900/50 border border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-start space-x-3">
              <div className="p-2.5 rounded-xl bg-electric/10 text-electric border border-electric/20 shrink-0">
                <Mail className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">Email Address</span>
                  <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/30 px-2 py-0.5 rounded-md">VERIFIED</span>
                </div>
                <div className="text-sm font-mono text-white mt-0.5">{user.email}</div>
                <div className="text-xs text-slate-400 mt-0.5">Used for login, recovery, and security alerts.</div>
              </div>
            </div>
            <button
              type="button"
              onClick={() => openContactModal('email')}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-cyanglow border border-cyanglow/30 hover:border-cyanglow rounded-xl text-xs font-bold transition-all shrink-0 self-start sm:self-center"
            >
              Change Email ➔
            </button>
          </div>

          {/* Mobile / Phone Number */}
          <div className="p-4 rounded-2xl bg-slate-900/50 border border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-start space-x-3">
              <div className="p-2.5 rounded-xl bg-electric/10 text-electric border border-electric/20 shrink-0">
                <Phone className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">Mobile Number (2FA)</span>
                  {user.phone_number ? (
                    <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/30 px-2 py-0.5 rounded-md">VERIFIED</span>
                  ) : (
                    <span className="text-[10px] font-bold text-amber-400 bg-amber-500/10 border border-amber-500/30 px-2 py-0.5 rounded-md">NOT CONFIGURED</span>
                  )}
                </div>
                <div className="text-sm font-mono text-white mt-0.5">{user.phone_number || 'No phone registered'}</div>
                <div className="text-xs text-slate-400 mt-0.5">Used for httpSMS 2FA challenges and SMS login codes.</div>
              </div>
            </div>
            <button
              type="button"
              onClick={() => openContactModal('sms')}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-cyanglow border border-cyanglow/30 hover:border-cyanglow rounded-xl text-xs font-bold transition-all shrink-0 self-start sm:self-center"
            >
              {user.phone_number ? 'Change Phone ➔' : 'Add Phone ➔'}
            </button>
          </div>
        </div>
      </div>

      {/* Interactive OTP Contact Change Modal */}
      {contactModalTarget && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-darkspace border border-slate-700/80 rounded-3xl p-6 shadow-2xl relative space-y-5 animate-in fade-in zoom-in duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center space-x-2.5">
                <div className="p-2 rounded-xl bg-electric/20 text-electric">
                  <KeyRound className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-base font-bold text-white">
                    {contactModalTarget === 'email' ? 'Change Registered Email' : 'Change Mobile Number'}
                  </h4>
                  <p className="text-xs text-slate-400">Requires OTP verification to the new contact</p>
                </div>
              </div>
              <button
                onClick={closeContactModal}
                className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Message */}
            {contactModalMsg && (
              <div
                className={
                  contactModalMsg.type === 'success'
                    ? 'p-3.5 rounded-xl border flex items-start space-x-2.5 text-xs bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                    : 'p-3.5 rounded-xl border flex items-start space-x-2.5 text-xs bg-red-500/10 border-red-500/30 text-red-400'
                }
              >
                {contactModalMsg.type === 'success' ? (
                  <CheckCircle className="w-4 h-4 shrink-0" />
                ) : (
                  <AlertCircle className="w-4 h-4 shrink-0" />
                )}
                <span>{contactModalMsg.text}</span>
              </div>
            )}

            {/* STEP 1: Enter New Contact & Request OTP */}
            {otpStep === 'request' ? (
              <form onSubmit={handleRequestContactOtp} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                    {contactModalTarget === 'email' ? 'New Email Address' : 'New Mobile Number (with country code)'}
                  </label>
                  <input
                    type={contactModalTarget === 'email' ? 'email' : 'tel'}
                    required
                    value={newContactInput}
                    onChange={(e) => setNewContactInput(e.target.value)}
                    placeholder={contactModalTarget === 'email' ? 'new_address@example.com' : '+91 9963759333'}
                    className="w-full px-4 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow text-sm"
                  />
                  <span className="text-[11px] text-slate-400 mt-1 block">
                    We will send a 6-digit verification code to this new {contactModalTarget === 'email' ? 'email' : 'number'}.
                  </span>
                </div>

                <div className="flex space-x-3 pt-2">
                  <button
                    type="button"
                    onClick={closeContactModal}
                    className="flex-1 py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold rounded-xl text-sm transition-all"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={contactLoading || !newContactInput.trim()}
                    className="flex-1 py-2.5 px-4 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 text-white font-bold rounded-xl text-sm transition-all shadow-lg disabled:opacity-40"
                  >
                    {contactLoading ? 'Sending Code...' : 'Send OTP ➔'}
                  </button>
                </div>
              </form>
            ) : (
              /* STEP 2: Enter OTP & Verify */
              <form onSubmit={handleVerifyContactOtp} className="space-y-4">
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                      Enter 6-Digit OTP Code
                    </label>
                    <button
                      type="button"
                      onClick={() => setOtpStep('request')}
                      className="text-xs text-cyanglow hover:underline"
                    >
                      Change Contact
                    </button>
                  </div>
                  <input
                    type="text"
                    required
                    maxLength={6}
                    value={otpCodeInput}
                    onChange={(e) => setOtpCodeInput(e.target.value.replace(/\D/g, ''))}
                    placeholder="123456"
                    className="w-full px-4 py-3 bg-slate-900 border border-slate-700 rounded-xl text-white placeholder-slate-500 text-center tracking-[0.5em] font-mono text-lg font-bold focus:outline-none focus:border-cyanglow"
                  />
                  <span className="text-[11px] text-slate-400 mt-1 block text-center">
                    Enter the code received at <strong className="text-white">{newContactInput}</strong>
                  </span>
                </div>

                <div className="flex space-x-3 pt-2">
                  <button
                    type="button"
                    onClick={closeContactModal}
                    className="flex-1 py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold rounded-xl text-sm transition-all"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={contactLoading || otpCodeInput.length < 4}
                    className="flex-1 py-2.5 px-4 bg-gradient-to-r from-emerald-500 to-teal-400 hover:from-emerald-400 hover:to-teal-300 text-darkspace font-bold rounded-xl text-sm transition-all shadow-lg disabled:opacity-40"
                  >
                    {contactLoading ? 'Verifying...' : 'Verify & Update ✓'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
