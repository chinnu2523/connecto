import React, { useEffect, useState } from 'react';
import { User } from './types';
import { getProfile } from './services/api';
import { HomepageAuth } from './components/HomepageAuth';
import { ProfileDashboard } from './components/ProfileDashboard';
import { ChatWorkspace } from './components/ChatWorkspace';
import { AcademyDashboard } from './components/AcademyDashboard';
import { CareersPortal } from './components/CareersPortal';
import { Shield, Radio, BookOpen, MessageSquare, Briefcase, UserCheck } from 'lucide-react';

export const App: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<'chat' | 'academy' | 'careers' | 'profile'>('chat');

  useEffect(() => {
    getProfile()
      .then((u) => {
        setUser(u);
        setView('chat');
      })
      .catch(() => {
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-darkspace">
        <div className="flex items-center space-x-3 text-cyanglow">
          <Radio className="w-6 h-6 animate-pulse" />
          <span className="font-mono text-sm tracking-widest uppercase">Initializing Connecto Platform...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-darkspace flex flex-col justify-between p-4 md:p-6">
      {/* Top Navbar Header */}
      <header className="max-w-7xl w-full mx-auto flex items-center justify-between py-3 mb-2">
        <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setView('chat')}>
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-electric to-cyanglow flex items-center justify-center text-white shadow-lg shadow-electric/30">
            <Shield className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xl font-black tracking-wider text-white">CONNECTO</span>
            <span className="text-xs block text-cyanglow font-mono font-medium">CYBERSECURITY & COMMUNITY PLATFORM</span>
          </div>
        </div>

        {user && (
          <div className="flex items-center space-x-3">
            <button
              onClick={() => setView('chat')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all border ${
                view === 'chat'
                  ? 'bg-electric text-white border-electric shadow-lg shadow-electric/25'
                  : 'bg-darkcard hover:bg-slate-800 text-slate-300 border-slate-700'
              }`}
            >
              <MessageSquare className="w-4 h-4" />
              <span>Community Chat</span>
            </button>

            <button
              onClick={() => setView('academy')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all border ${
                view === 'academy'
                  ? 'bg-gradient-to-r from-electric to-cyanglow text-white border-cyanglow shadow-lg'
                  : 'bg-darkcard hover:bg-slate-800 text-slate-300 border-slate-700'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              <span>Academy</span>
            </button>

            <button
              onClick={() => setView('careers')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all border ${
                view === 'careers'
                  ? 'bg-gradient-to-r from-electric to-cyanglow text-white border-cyanglow shadow-lg'
                  : 'bg-darkcard hover:bg-slate-800 text-slate-300 border-slate-700'
              }`}
            >
              <Briefcase className="w-4 h-4" />
              <span>Careers & AI Portal</span>
            </button>

            <button
              onClick={() => setView('profile')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all border ${
                view === 'profile'
                  ? 'bg-electric text-white border-electric shadow-lg'
                  : 'bg-darkcard hover:bg-slate-800 text-slate-300 border-slate-700'
              }`}
            >
              <UserCheck className="w-4 h-4" />
              <span>Profile</span>
            </button>
          </div>
        )}
      </header>

      {/* Main Content Area */}
      <main className="w-full max-w-7xl mx-auto my-2">
        {user ? (
          view === 'profile' ? (
            <ProfileDashboard
              user={user}
              onUserUpdated={(u) => setUser(u)}
              onLogout={() => {
                setUser(null);
                setView('chat');
              }}
            />
          ) : view === 'academy' ? (
            <AcademyDashboard />
          ) : view === 'careers' ? (
            <CareersPortal currentUser={user} />
          ) : (
            <ChatWorkspace
              currentUser={user}
              onOpenProfile={() => setView('profile')}
            />
          )
        ) : (
          /* HOMEPAGE WITH SIGN UP & LOGIN ONLY */
          <HomepageAuth
            onSuccess={(u) => {
              setUser(u);
              setView('chat');
            }}
          />
        )}
      </main>

      {/* Footer */}
      <footer className="max-w-7xl w-full mx-auto text-center py-3 border-t border-slate-800/60 text-xs text-slate-500">
        Connecto Platform • Argon2id Encrypted • Real-Time Community Platform
      </footer>
    </div>
  );
};

export default App;
