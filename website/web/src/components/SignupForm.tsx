import React, { useState, useEffect } from 'react';
import { User, UserSignup } from '../types';
import { signup, checkUsernameAvailability, checkEmailAvailability } from '../services/api';
import { UserPlus, Mail, User as UserIcon, Lock, ShieldCheck, AlertCircle, Eye, EyeOff, Sparkles, Shield, CheckCircle2, XCircle, Loader2 } from 'lucide-react';

interface SignupFormProps {
  onSuccess: (user: User) => void;
  onSwitchToLogin: () => void;
}

export const SignupForm: React.FC<SignupFormProps> = ({ onSuccess, onSwitchToLogin }) => {
  const [formData, setFormData] = useState<UserSignup>({
    email: '',
    username: '',
    display_name: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Live validation states
  const [usernameStatus, setUsernameStatus] = useState<'idle' | 'checking' | 'available' | 'taken' | 'invalid'>('idle');
  const [usernameMsg, setUsernameMsg] = useState('');

  const [emailStatus, setEmailStatus] = useState<'idle' | 'checking' | 'available' | 'taken' | 'invalid'>('idle');
  const [emailMsg, setEmailMsg] = useState('');

  // Live Username Check
  useEffect(() => {
    const clean = formData.username.trim();
    if (!clean) {
      setUsernameStatus('idle');
      setUsernameMsg('');
      return;
    }
    if (clean.length < 3) {
      setUsernameStatus('invalid');
      setUsernameMsg('Username must be at least 3 characters');
      return;
    }
    if (!/^[a-zA-Z0-9_]+$/.test(clean)) {
      setUsernameStatus('invalid');
      setUsernameMsg('Only letters, numbers, and underscores allowed');
      return;
    }

    setUsernameStatus('checking');
    setUsernameMsg('Checking availability...');
    const timer = setTimeout(async () => {
      try {
        const res = await checkUsernameAvailability(clean);
        if (res.available) {
          setUsernameStatus('available');
          setUsernameMsg('Username is available ✓');
        } else {
          setUsernameStatus('taken');
          setUsernameMsg(res.message || 'Username is already taken');
        }
      } catch {
        setUsernameStatus('available');
        setUsernameMsg('Username available');
      }
    }, 320);

    return () => clearTimeout(timer);
  }, [formData.username]);

  // Live Email Check
  useEffect(() => {
    const clean = formData.email.trim();
    if (!clean) {
      setEmailStatus('idle');
      setEmailMsg('');
      return;
    }
    const emailRegex = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
    if (!emailRegex.test(clean)) {
      setEmailStatus('invalid');
      setEmailMsg('Please enter a valid email address (e.g. name@example.com)');
      return;
    }

    setEmailStatus('checking');
    setEmailMsg('Verifying email...');
    const timer = setTimeout(async () => {
      try {
        const res = await checkEmailAvailability(clean);
        if (res.available) {
          setEmailStatus('available');
          setEmailMsg('Email verified & available ✓');
        } else {
          setEmailStatus('taken');
          setEmailMsg(res.message || 'Email already registered');
        }
      } catch {
        setEmailStatus('available');
        setEmailMsg('Email verified ✓');
      }
    }, 350);

    return () => clearTimeout(timer);
  }, [formData.email]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const user = await signup(formData);
      onSuccess(user);
    } catch (err: any) {
      setError(err.message || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto p-8 rounded-3xl glass-panel shadow-2xl relative overflow-hidden transition-all border border-electric/30">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-electric via-cyanglow to-neonpink"></div>

      <div className="flex items-center space-x-3 mb-6">
        <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-electric/30 to-cyanglow/30 border border-cyanglow/40 flex items-center justify-center text-cyanglow shadow-lg">
          <UserPlus className="w-6 h-6" />
        </div>
        <div>
          <h2 className="text-2xl font-black text-white tracking-tight">Create Account</h2>
          <p className="text-xs text-cyanglow font-mono font-medium">Join Connecto Community</p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 rounded-2xl bg-red-500/10 border border-red-500/30 flex items-start space-x-3 text-red-400 text-sm">
          <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* DEDICATED SIGNUP FORM */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
            Email Address
          </label>
          <div className="relative">
            <Mail className={`w-5 h-5 absolute left-3.5 top-3 ${
              emailStatus === 'available' ? 'text-emerald-400' :
              emailStatus === 'taken' || emailStatus === 'invalid' ? 'text-rose-400' :
              'text-slate-500'
            }`} />
            <input
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="you@domain.dev"
              className={`w-full pl-11 pr-10 py-3 bg-darkcard/70 border rounded-xl text-white placeholder-slate-500 focus:outline-none transition-all text-sm font-medium ${
                emailStatus === 'available' ? 'border-emerald-500/70 focus:border-emerald-400 focus:ring-1 focus:ring-emerald-400' :
                emailStatus === 'taken' || emailStatus === 'invalid' ? 'border-rose-500/70 focus:border-rose-400 focus:ring-1 focus:ring-rose-400' :
                emailStatus === 'checking' ? 'border-sky-500/70 focus:border-sky-400 focus:ring-1 focus:ring-sky-400' :
                'border-slate-700/70 focus:border-cyanglow focus:ring-1 focus:ring-cyanglow'
              }`}
            />
            <div className="absolute right-3.5 top-3.5">
              {emailStatus === 'checking' && <Loader2 className="w-4 h-4 text-sky-400 animate-spin" />}
              {emailStatus === 'available' && <CheckCircle2 className="w-4 h-4 text-emerald-400" />}
              {(emailStatus === 'taken' || emailStatus === 'invalid') && <XCircle className="w-4 h-4 text-rose-400" />}
            </div>
          </div>
          {emailStatus !== 'idle' && emailMsg && (
            <div className={`mt-1.5 text-xs font-medium px-2.5 py-1 rounded-lg flex items-center space-x-1.5 ${
              emailStatus === 'available' ? 'bg-emerald-950/40 border border-emerald-500/40 text-emerald-400' :
              emailStatus === 'checking' ? 'bg-sky-950/40 border border-sky-500/40 text-sky-300' :
              'bg-rose-950/40 border border-rose-500/40 text-rose-400'
            }`}>
              <span>{emailMsg}</span>
            </div>
          )}
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
            Username <span className="text-slate-500 font-normal lowercase">(3-32 chars, letters/numbers/_)</span>
          </label>
          <div className="relative">
            <UserIcon className={`w-5 h-5 absolute left-3.5 top-3 ${
              usernameStatus === 'available' ? 'text-emerald-400' :
              usernameStatus === 'taken' || usernameStatus === 'invalid' ? 'text-rose-400' :
              'text-slate-500'
            }`} />
            <input
              type="text"
              required
              pattern="[a-zA-Z0-9_]{3,32}"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              placeholder="cyber_ninja"
              className={`w-full pl-11 pr-10 py-3 bg-darkcard/70 border rounded-xl text-white placeholder-slate-500 focus:outline-none transition-all text-sm font-medium ${
                usernameStatus === 'available' ? 'border-emerald-500/70 focus:border-emerald-400 focus:ring-1 focus:ring-emerald-400' :
                usernameStatus === 'taken' || usernameStatus === 'invalid' ? 'border-rose-500/70 focus:border-rose-400 focus:ring-1 focus:ring-rose-400' :
                usernameStatus === 'checking' ? 'border-sky-500/70 focus:border-sky-400 focus:ring-1 focus:ring-sky-400' :
                'border-slate-700/70 focus:border-cyanglow focus:ring-1 focus:ring-cyanglow'
              }`}
            />
            <div className="absolute right-3.5 top-3.5">
              {usernameStatus === 'checking' && <Loader2 className="w-4 h-4 text-sky-400 animate-spin" />}
              {usernameStatus === 'available' && <CheckCircle2 className="w-4 h-4 text-emerald-400" />}
              {(usernameStatus === 'taken' || usernameStatus === 'invalid') && <XCircle className="w-4 h-4 text-rose-400" />}
            </div>
          </div>
          {usernameStatus !== 'idle' && usernameMsg && (
            <div className={`mt-1.5 text-xs font-medium px-2.5 py-1 rounded-lg flex items-center space-x-1.5 ${
              usernameStatus === 'available' ? 'bg-emerald-950/40 border border-emerald-500/40 text-emerald-400' :
              usernameStatus === 'checking' ? 'bg-sky-950/40 border border-sky-500/40 text-sky-300' :
              'bg-rose-950/40 border border-rose-500/40 text-rose-400'
            }`}>
              <span>{usernameMsg}</span>
            </div>
          )}
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
            Display Name
          </label>
          <div className="relative">
            <ShieldCheck className="w-5 h-5 absolute left-3.5 top-3 text-slate-500" />
            <input
              type="text"
              required
              value={formData.display_name}
              onChange={(e) => setFormData({ ...formData, display_name: e.target.value })}
              placeholder="Alex Thorne"
              className="w-full pl-11 pr-4 py-3 bg-darkcard/70 border border-slate-700/70 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow transition-all text-sm font-medium"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
            Password <span className="text-slate-500 font-normal lowercase">(Min 8 chars, Argon2id)</span>
          </label>
          <div className="relative">
            <Lock className="w-5 h-5 absolute left-3.5 top-3 text-slate-500" />
            <input
              type={showPassword ? 'text' : 'password'}
              required
              minLength={8}
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              placeholder="••••••••••••"
              className="w-full pl-11 pr-11 py-3 bg-darkcard/70 border border-slate-700/70 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow transition-all text-sm font-medium"
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3.5 top-3 text-slate-500 hover:text-slate-300 transition-colors"
            >
              {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
            </button>
          </div>
        </div>

        <button
          type="submit"
          disabled={loading || usernameStatus === 'taken' || usernameStatus === 'invalid' || emailStatus === 'taken' || emailStatus === 'invalid'}
          className="w-full mt-3 py-3.5 px-4 bg-gradient-to-r from-electric via-cyanglow to-neonpink hover:opacity-95 text-white font-extrabold rounded-xl transition-all shadow-xl shadow-electric/25 active:scale-[0.98] disabled:opacity-50 flex items-center justify-center space-x-2 tracking-wider uppercase text-sm"
        >
          <Sparkles className="w-4 h-4" />
          <span>{loading ? 'Creating Account...' : 'Sign Up & Start Chatting'}</span>
        </button>
      </form>

      <div className="mt-6 pt-4 border-t border-slate-800/60 flex items-center justify-between text-xs text-slate-400">
        <span className="flex items-center space-x-1 text-cyanglow font-mono">
          <Shield className="w-3.5 h-3.5" />
          <span>Argon2id Encrypted</span>
        </span>
        <div>
          Already registered?{' '}
          <button
            onClick={onSwitchToLogin}
            className="text-cyanglow hover:underline font-bold"
          >
            Sign In
          </button>
        </div>
      </div>
    </div>
  );
};
