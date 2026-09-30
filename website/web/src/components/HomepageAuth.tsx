import React, { useState } from 'react';
import { User } from '../types';
import { SignupForm } from './SignupForm';
import { LoginForm } from './LoginForm';
import { MessageSquare, Radio, Lock } from 'lucide-react';

interface HomepageAuthProps {
  onSuccess: (user: User) => void;
}

export const HomepageAuth: React.FC<HomepageAuthProps> = ({ onSuccess }) => {
  const [mode, setMode] = useState<'signup' | 'login'>('signup');

  return (
    <div className="w-full max-w-6xl mx-auto my-6 grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
      {/* Left Column: Homepage Details ONLY */}
      <div className="lg:col-span-6 space-y-6">
        <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-cyanglow/10 border border-cyanglow/20 text-cyanglow text-xs font-mono font-semibold">
          <Radio className="w-4 h-4 animate-pulse" />
          <span>Real-Time Community & Chat Platform</span>
        </div>

        <h1 className="text-4xl md:text-5xl font-black text-white tracking-tight leading-tight">
          Connect, Learn & Chat with <span className="bg-gradient-to-r from-electric via-cyanglow to-neonpink bg-clip-text text-transparent">Zero Friction.</span>
        </h1>

        <p className="text-slate-300 text-sm md:text-base leading-relaxed">
          The ultimate real-time platform for technical learning, WebRTC voice channels, and AI-powered community chat.
        </p>

        <div className="grid grid-cols-2 gap-4 pt-2">
          <div className="p-4 rounded-2xl glass-card border border-slate-800">
            <MessageSquare className="w-6 h-6 text-cyanglow mb-2" />
            <h3 className="font-bold text-white text-sm">Real-Time Chat</h3>
            <p className="text-xs text-slate-400 mt-1">WebSocket engine with zero message duplication.</p>
          </div>

          <div className="p-4 rounded-2xl glass-card border border-slate-800">
            <Lock className="w-6 h-6 text-neonpink mb-2" />
            <h3 className="font-bold text-white text-sm">Argon2id Encrypted</h3>
            <p className="text-xs text-slate-400 mt-1">Signed httpOnly cookie security.</p>
          </div>
        </div>
      </div>

      {/* Right Column: Sign Up & Login Form */}
      <div className="lg:col-span-6">
        {mode === 'signup' ? (
          <SignupForm
            onSuccess={onSuccess}
            onSwitchToLogin={() => setMode('login')}
          />
        ) : (
          <LoginForm
            onSuccess={onSuccess}
            onSwitchToSignup={() => setMode('signup')}
          />
        )}
      </div>
    </div>
  );
};
