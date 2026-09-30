import React, { useEffect, useState } from 'react';
import { User, JobOpening, JobApplication } from '../types';
import { getJobOpenings, applyForJob, getRecruiterApplications } from '../services/api';
import { Briefcase, CheckCircle, AlertCircle, Upload, Shield, Users, Download, Lock } from 'lucide-react';

interface CareersPortalProps {
  currentUser: User;
}

export const CareersPortal: React.FC<CareersPortalProps> = ({ currentUser }) => {
  const [jobs, setJobs] = useState<JobOpening[]>([]);
  const [selectedJob, setSelectedJob] = useState<JobOpening | null>(null);
  const [fullName, setFullName] = useState(currentUser.display_name);
  const [email, setEmail] = useState(currentUser.email);
  const [cvFile, setCvFile] = useState<File | null>(null);
  const [applying, setSubmitting] = useState(false);
  const [submittedApp, setSubmittedApp] = useState<JobApplication | null>(null);
  const [applyError, setApplyError] = useState<string | null>(null);

  const [recruiterApps, setRecruiterApps] = useState<JobApplication[]>([]);
  const [recruiterError, setRecruiterError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'openings' | 'recruiter'>('openings');

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    try {
      const list = await getJobOpenings();
      setJobs(list);
    } catch {
      // Ignore
    }
  };

  const handleFetchRecruiterDashboard = async () => {
    setRecruiterError(null);
    try {
      const list = await getRecruiterApplications();
      setRecruiterApps(list);
      setActiveTab('recruiter');
    } catch (err: any) {
      setRecruiterError(err.message || 'Access denied. Recruiter role required.');
    }
  };

  const handleApplySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedJob || !cvFile) return;

    setSubmitting(true);
    setApplyError(null);

    try {
      const result = await applyForJob(selectedJob.id, fullName, email, cvFile);
      setSubmittedApp(result);
      setSelectedJob(null);
    } catch (err: any) {
      setApplyError(err.message || 'Failed to submit application.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="w-full max-w-5xl mx-auto space-y-6">
      {/* Header Banner */}
      <div className="p-8 rounded-3xl glass-panel relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-electric via-cyanglow to-neonpink"></div>

        <div>
          <div className="flex items-center space-x-3 mb-2">
            <div className="w-10 h-10 rounded-2xl bg-electric/20 border border-electric/40 flex items-center justify-center text-cyanglow">
              <Briefcase className="w-6 h-6" />
            </div>
            <h1 className="text-3xl font-extrabold text-white tracking-tight">AI Careers & CV Portal</h1>
          </div>
          <p className="text-sm text-slate-400">
            Apply to cybersecurity openings with real OCR evidence-based skill evaluation
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={() => setActiveTab('openings')}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all border ${
              activeTab === 'openings'
                ? 'bg-electric text-white border-electric shadow-lg shadow-electric/25'
                : 'bg-darkcard text-slate-300 hover:bg-slate-800 border-slate-700'
            }`}
          >
            Job Openings
          </button>

          <button
            onClick={handleFetchRecruiterDashboard}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all border flex items-center space-x-1.5 ${
              activeTab === 'recruiter'
                ? 'bg-gradient-to-r from-electric to-cyanglow text-white border-cyanglow shadow-lg'
                : 'bg-darkcard text-slate-300 hover:bg-slate-800 border-slate-700'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>Recruiter Portal</span>
          </button>
        </div>
      </div>

      {/* Recruiter Error Banner */}
      {recruiterError && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 flex items-center space-x-3 text-red-400 text-sm">
          <Lock className="w-5 h-5 shrink-0" />
          <span>{recruiterError}</span>
        </div>
      )}

      {/* Application Success Receipt */}
      {submittedApp && (
        <div className="p-8 rounded-3xl glass-panel space-y-4 border border-emerald-500/40 bg-emerald-500/5">
          <div className="flex items-center space-x-3 text-emerald-400">
            <CheckCircle className="w-8 h-8" />
            <div>
              <h2 className="text-xl font-bold text-white">Application Submitted & Analyzed</h2>
              <p className="text-xs text-slate-300">Position: {submittedApp.job_title}</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
            <div className="p-4 rounded-2xl glass-card">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-1">
                AI Compatibility Score
              </span>
              <span className="text-3xl font-black text-cyanglow">{submittedApp.ai_score}% Match</span>
            </div>

            <div className="p-4 rounded-2xl glass-card">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-1">
                Detected Proficiencies
              </span>
              <div className="flex flex-wrap gap-1.5 pt-1">
                {submittedApp.detected_skills.map((skill) => (
                  <span
                    key={skill}
                    className="px-2.5 py-0.5 rounded-md bg-electric/30 border border-electric/40 text-xs font-mono font-semibold text-white"
                  >
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          </div>

          <p className="text-xs text-slate-300 font-mono p-4 rounded-xl bg-darkspace border border-slate-800">
            {submittedApp.summary}
          </p>

          <button
            onClick={() => setSubmittedApp(null)}
            className="text-xs text-cyanglow hover:underline font-semibold pt-2"
          >
            ← Submit Another Application
          </button>
        </div>
      )}

      {activeTab === 'recruiter' && !recruiterError ? (
        /* RECRUITER DASHBOARD VIEW */
        <div className="p-8 rounded-3xl glass-panel space-y-6">
          <div className="flex items-center justify-between border-b border-slate-800 pb-4">
            <div>
              <h2 className="text-2xl font-bold text-white flex items-center space-x-2">
                <Shield className="w-6 h-6 text-cyanglow" />
                <span>Recruiter Screening Portal</span>
              </h2>
              <p className="text-xs text-slate-400">Access-controlled candidate submission & OCR evidence queue</p>
            </div>
            <span className="text-xs font-mono text-emerald-400 bg-emerald-500/10 border border-emerald-500/30 px-3 py-1 rounded-full">
              {recruiterApps.length} Candidates Screened
            </span>
          </div>

          <div className="space-y-4">
            {recruiterApps.map((appItem) => (
              <div key={appItem.id} className="p-6 rounded-2xl glass-card space-y-3">
                <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-2">
                  <div>
                    <h3 className="text-lg font-bold text-white">{appItem.full_name}</h3>
                    <p className="text-xs text-cyanglow font-mono">{appItem.email} • Position: {appItem.job_title}</p>
                  </div>

                  <div className="flex items-center space-x-3">
                    <span className="px-3 py-1 rounded-xl bg-gradient-to-r from-electric to-cyanglow text-white text-sm font-extrabold shadow-md">
                      {appItem.ai_score}% AI Match
                    </span>
                    <a
                      href={appItem.cv_file_url}
                      target="_blank"
                      rel="noreferrer"
                      className="px-3 py-1.5 bg-darkcard hover:bg-slate-800 border border-slate-700 text-slate-300 rounded-xl text-xs font-semibold flex items-center space-x-1.5"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>Download CV</span>
                    </a>
                  </div>
                </div>

                <div className="p-3 rounded-xl bg-darkspace border border-slate-800/80 text-xs font-mono text-slate-300">
                  {appItem.summary}
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        /* JOB OPENINGS LIST VIEW */
        <div className="grid grid-cols-1 gap-6">
          {jobs.map((job) => (
            <div key={job.id} className="p-6 rounded-3xl glass-panel space-y-4 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
              <div className="space-y-2 flex-1">
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-bold text-cyanglow bg-cyanglow/10 border border-cyanglow/20 px-2.5 py-0.5 rounded-full">
                    {job.department}
                  </span>
                  <span className="text-xs text-slate-400">• {job.location}</span>
                </div>

                <h3 className="text-xl font-bold text-white">{job.title}</h3>
                <p className="text-xs text-slate-300 leading-relaxed">{job.description}</p>

                <div className="flex flex-wrap gap-1.5 pt-2">
                  <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider self-center mr-1">
                    Required Keywords:
                  </span>
                  {job.required_keywords.map((kw) => (
                    <span key={kw} className="px-2 py-0.5 rounded bg-darkcard text-xs font-mono text-slate-300 border border-slate-800">
                      {kw}
                    </span>
                  ))}
                </div>
              </div>

              <button
                onClick={() => {
                  setSelectedJob(job);
                  setApplyError(null);
                }}
                className="px-6 py-3 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 text-white font-bold rounded-xl transition-all shadow-lg shadow-electric/20 shrink-0"
              >
                Apply Now
              </button>
            </div>
          ))}
        </div>
      )}

      {/* APPLICATION SUBMISSION MODAL */}
      {selectedJob && (
        <div className="fixed inset-0 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-darkspace border border-slate-800 p-8 rounded-3xl max-w-lg w-full space-y-6 relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-electric via-cyanglow to-neonpink"></div>

            <div>
              <h3 className="text-2xl font-bold text-white">Apply for Position</h3>
              <p className="text-xs text-cyanglow font-semibold mt-1">{selectedJob.title}</p>
            </div>

            {applyError && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 flex items-center space-x-2 text-red-400 text-xs">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{applyError}</span>
              </div>
            )}

            <form onSubmit={handleApplySubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="w-full px-4 py-2.5 bg-darkcard border border-slate-700/60 rounded-xl text-white text-sm focus:outline-none focus:border-cyanglow"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1">
                  Email Address
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-4 py-2.5 bg-darkcard border border-slate-700/60 rounded-xl text-white text-sm focus:outline-none focus:border-cyanglow"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1">
                  Upload CV Document <span className="text-slate-500 font-normal">(PDF or TXT)</span>
                </label>
                <div className="p-4 rounded-xl border border-dashed border-slate-700 bg-darkcard/50 text-center hover:border-cyanglow transition-colors">
                  <input
                    type="file"
                    required
                    accept=".pdf,.txt,.md"
                    onChange={(e) => setCvFile(e.target.files?.[0] || null)}
                    className="hidden"
                    id="cv_upload_input"
                  />
                  <label htmlFor="cv_upload_input" className="cursor-pointer flex flex-col items-center space-y-2">
                    <Upload className="w-6 h-6 text-cyanglow" />
                    <span className="text-xs text-slate-300 font-semibold">
                      {cvFile ? cvFile.name : 'Click or drop PDF/TXT CV file here'}
                    </span>
                  </label>
                </div>
              </div>

              <div className="flex space-x-3 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedJob(null)}
                  className="flex-1 py-3 bg-slate-800 text-slate-300 rounded-xl text-sm font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={applying || !cvFile}
                  className="flex-1 py-3 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 text-white font-bold rounded-xl text-sm transition-all disabled:opacity-40"
                >
                  {applying ? 'Running OCR & AI Pipeline...' : 'Submit Application'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
