import React, { useState } from 'react';
import { User, UserLogin } from '../types';
import { login, verifyLogin2Fa } from '../services/api';
import { LogIn, User as UserIcon, Lock, AlertCircle, Eye, EyeOff, Sparkles, Shield, KeyRound, ArrowLeft } from 'lucide-react';

interface LoginFormProps {
  onSuccess: (user: User) => void;
  onSwitchToSignup: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({ onSuccess, onSwitchToSignup }) => {
  const [formData, setFormData] = useState<UserLogin>({
    login: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 2FA Challenge state
  const [step, setStep] = useState<'credentials' | '2fa'>('credentials');
  const [twoFaInfo, setTwoFaInfo] = useState<{
    maskedDestination?: string;
    method?: string;
    devOtp?: string;
  }>({});
  const [otpCode, setOtpCode] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const user = await login(formData);
      if (user.requires_2fa) {
        setTwoFaInfo({
          maskedDestination: user.masked_destination,
          method: user.two_factor_method || 'sms',
          devOtp: user.dev_otp,
        });
        setStep('2fa');
      } else {
        onSuccess(user);
      }
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handle2FaSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (otpCode.trim().length !== 6) {
      setError('Please enter the 6-digit verification code.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const user = await verifyLogin2Fa(formData.login, otpCode.trim());
      onSuccess(user);
    } catch (err: any) {
      setError(err.message || '2FA verification failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto p-8 rounded-3xl glass-panel shadow-2xl relative overflow-hidden transition-all border border-electric/30">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-electric via-cyanglow to-neonpink"></div>

      <div className="flex items-center space-x-3 mb-6">
        <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-electric/30 to-cyanglow/30 border border-cyanglow/40 flex items-center justify-center text-cyanglow shadow-lg">
          {step === '2fa' ? <KeyRound className="w-6 h-6" /> : <LogIn className="w-6 h-6" />}
        </div>
        <div>
          <h2 className="text-2xl font-black text-white tracking-tight">
            {step === '2fa' ? 'Two-Factor Auth' : 'Sign In'}
          </h2>
          <p className="text-xs text-cyanglow font-mono font-medium">
            {step === '2fa'
              ? `Verification via ${(twoFaInfo.method || 'sms').toUpperCase()}`
              : 'Access Connecto Workspace'}
          </p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 rounded-2xl bg-red-500/10 border border-red-500/30 flex items-start space-x-3 text-red-400 text-sm">
          <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {step === 'credentials' ? (
        /* LOGIN FORM - CONTAINS ONLY USERNAME/EMAIL & PASSWORD */
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
              Username or Email Address
            </label>
            <div className="relative">
              <UserIcon className="w-5 h-5 absolute left-3.5 top-3 text-slate-500" />
              <input
                type="text"
                required
                value={formData.login}
                onChange={(e) => setFormData({ ...formData, login: e.target.value })}
                placeholder="Username or email"
                className="w-full pl-11 pr-4 py-3 bg-darkcard/70 border border-slate-700/70 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow transition-all text-sm font-medium"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
              Password
            </label>
            <div className="relative">
              <Lock className="w-5 h-5 absolute left-3.5 top-3 text-slate-500" />
              <input
                type={showPassword ? 'text' : 'password'}
                required
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
            disabled={loading}
            className="w-full mt-3 py-3.5 px-4 bg-gradient-to-r from-electric via-cyanglow to-neonpink hover:opacity-95 text-white font-extrabold rounded-xl transition-all shadow-xl shadow-electric/25 active:scale-[0.98] disabled:opacity-50 flex items-center justify-center space-x-2 tracking-wider uppercase text-sm"
          >
            <Sparkles className="w-4 h-4" />
            <span>{loading ? 'Authenticating...' : 'Sign In & Start Chatting'}</span>
          </button>
        </form>
      ) : (
        /* 2FA VERIFICATION STEP */
        <form onSubmit={handle2FaSubmit} className="space-y-4">
          <p className="text-xs text-slate-300">
            A 6-digit verification code was sent to{' '}
            <span className="text-cyanglow font-semibold font-mono">
              {twoFaInfo.maskedDestination || `your ${(twoFaInfo.method || 'sms').toUpperCase()}`}
            </span>
            . Enter it below to complete sign in.
          </p>


          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
              6-Digit Security Code
            </label>
            <div className="relative">
              <KeyRound className="w-5 h-5 absolute left-3.5 top-3 text-slate-500" />
              <input
                type="text"
                maxLength={6}
                autoFocus
                required
                value={otpCode}
                onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ''))}
                placeholder="000000"
                className="w-full pl-11 pr-4 py-3 bg-darkcard/70 border border-slate-700/70 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-cyanglow focus:ring-1 focus:ring-cyanglow transition-all text-lg font-mono tracking-widest text-center"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || otpCode.length !== 6}
            className="w-full mt-3 py-3.5 px-4 bg-gradient-to-r from-electric via-cyanglow to-neonpink hover:opacity-95 text-white font-extrabold rounded-xl transition-all shadow-xl shadow-electric/25 active:scale-[0.98] disabled:opacity-50 flex items-center justify-center space-x-2 tracking-wider uppercase text-sm"
          >
            <Shield className="w-4 h-4" />
            <span>{loading ? 'Verifying...' : 'Verify & Log In'}</span>
          </button>

          <button
            type="button"
            onClick={() => {
              setStep('credentials');
              setOtpCode('');
              setError(null);
            }}
            className="w-full text-xs text-slate-400 hover:text-white flex items-center justify-center space-x-1.5 py-1 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Sign In</span>
          </button>
        </form>
      )}

      <div className="mt-6 pt-4 border-t border-slate-800/60 flex items-center justify-between text-xs text-slate-400">
        <span className="flex items-center space-x-1 text-cyanglow font-mono">
          <Shield className="w-3.5 h-3.5" />
          <span>httpOnly Cookie Auth</span>
        </span>
        <div>
          Need an account?{' '}
          <button
            onClick={onSwitchToSignup}
            className="text-cyanglow hover:underline font-bold"
          >
            Create One
          </button>
        </div>
      </div>
    </div>
  );
};
