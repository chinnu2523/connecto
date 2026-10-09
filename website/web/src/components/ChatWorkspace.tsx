import React, { useEffect, useState, useRef } from 'react';
import { User, Server, Channel, Message, Friendship, FriendRequestItem, AppNotification } from '../types';
import {
  getServers, createServer, getChannels, createChannel, getPublicChannels,
  getMessages, sendMessage, startDM, getFriends, sendFriendRequest,
  getReceivedFriendRequests, acceptFriendRequest, declineFriendRequest,
  getNotifications, getUnreadNotificationCount, markNotificationRead,
  markAllNotificationsRead, clearNotifications
} from '../services/api';
import { wsClient } from '../services/websocket';
import { voiceManager } from '../services/webrtc';
import {
  Hash, Volume2, Plus, Users, Send, ShieldAlert,
  MessageSquare, Clock, UserPlus, Mic, MicOff, PhoneOff, Phone, Radio,
  Check, X, Sparkles, Bell, Trash2, CheckCheck
} from 'lucide-react';

interface ChatWorkspaceProps {
  currentUser: User;
  onOpenProfile: () => void;
}

const OFFICIAL_HUB_SERVER: Server = {
  id: 'connecto_community_hub',
  name: 'Connecto Hub',
  icon_url: null,
  owner_id: 'system',
  created_at: '',
};

export const ChatWorkspace: React.FC<ChatWorkspaceProps> = ({ currentUser, onOpenProfile }) => {
  const [servers, setServers] = useState<Server[]>([]);
  const [activeServer, setActiveServer] = useState<Server | null>(OFFICIAL_HUB_SERVER);
  const [channels, setChannels] = useState<Channel[]>([]);
  const [publicChannelsList, setPublicChannelsList] = useState<Channel[]>([]);
  const [activeChannel, setActiveChannel] = useState<Channel | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageInput, setMessageInput] = useState('');

  const [friends, setFriends] = useState<Friendship[]>([]);
  const [receivedRequests, setReceivedRequests] = useState<FriendRequestItem[]>([]);
  const [activeTab, setActiveTab] = useState<'chat' | 'friends'>('chat');
  const [addFriendInput, setAddFriendInput] = useState('');
  const [friendMsg, setFriendMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const [showCreateServer, setShowCreateServer] = useState(false);
  const [newServerName, setNewServerName] = useState('');
  const [showCreateChannel, setShowCreateChannel] = useState(false);
  const [newChannelName, setNewServerChannelName] = useState('');

  // Voice State (Group Channels & 1:1 Calls)
  const [inVoiceRoom, setInVoiceRoom] = useState(false);
  const [voicePeers, setVoicePeers] = useState<string[]>([]);
  const [isMuted, setIsMuted] = useState(false);

  // 1:1 Voice Call State
  const [callState, setCallState] = useState<'IDLE' | 'OUTGOING_RINGING' | 'INCOMING_RINGING' | 'CONNECTED'>('IDLE');
  const [activeCallPartner, setActiveCallPartner] = useState<{ username: string; displayName: string; roomId: string; avatar?: string } | null>(null);
  const [incomingCallData, setIncomingCallData] = useState<{ roomId: string; callerId: string; callerUsername: string; callerName: string; callerAvatar?: string } | null>(null);
  const [callDurationSeconds, setCallDurationSeconds] = useState(0);
  const [isCallInitiator, setIsCallInitiator] = useState(false);

  // Notification States
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [unreadNotifCount, setUnreadNotifCount] = useState<number>(0);
  const [showNotificationTray, setShowNotificationTray] = useState<boolean>(false);
  const [notifFilter, setNotifFilter] = useState<'all' | 'calls' | 'requests' | 'messages'>('all');

  const loadNotifications = async () => {
    try {
      const list = await getNotifications();
      setNotifications(list);
      const cnt = await getUnreadNotificationCount();
      setUnreadNotifCount(cnt.unread_count);
    } catch {
      // ignore
    }
  };

  const handleMarkNotifRead = async (id: string) => {
    try {
      await markNotificationRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, is_read: true } : n)));
      setUnreadNotifCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllNotifsRead = async () => {
    try {
      await markAllNotificationsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, is_read: true })));
      setUnreadNotifCount(0);
    } catch (err) {
      console.error(err);
    }
  };

  const handleClearAllNotifs = async () => {
    try {
      await clearNotifications();
      setNotifications([]);
      setUnreadNotifCount(0);
    } catch (err) {
      console.error(err);
    }
  };

  const [errorBanner, setErrorBanner] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Quick Action Chips
  const quickChips = [
    '👋 Hey everyone!',
    '🎮 Anyone for CS Rank?',
    '🔥 Let\'s team up!',
    '🚀 Ready to play'
  ];

  // Initialize WebSocket connection & WebRTC Voice Handlers
  useEffect(() => {
    wsClient.connect();

    const removeWsListener = wsClient.addListener(async (event) => {
      if (event.type === 'message_created' || event.type === 'new_message' || event.type === 'dm_message') {
        const rawMsg = event.data || event.message || event.payload;
        if (!rawMsg) return;
        const newMsg: Message = {
          ...rawMsg,
          sender_username: rawMsg.sender_username || rawMsg.author_name || rawMsg.user || rawMsg.sender || '',
          sender_display_name: rawMsg.sender_display_name || rawMsg.author_display_name || rawMsg.nickname || rawMsg.sender_username || rawMsg.user || 'Shinobi',
          sender_id: rawMsg.sender_id || rawMsg.author_id || '',
          sender_avatar_url: rawMsg.sender_avatar_url || rawMsg.author_avatar || rawMsg.avatar_url || rawMsg.avatar || null,
          created_at: rawMsg.created_at || rawMsg.timestamp || rawMsg.createdAt || new Date().toISOString()
        };
        const currentCh = activeChannel;
        const eventChId = event.channel_id || event.channel || newMsg.channel_id;
        const eventChName = event.channel_name || '';

        const isMatch = currentCh && (
          newMsg.channel_id === currentCh.id ||
          newMsg.channel_id === currentCh.name ||
          eventChId === currentCh.id ||
          eventChId === currentCh.name ||
          (currentCh.name && eventChName && eventChName.toLowerCase() === currentCh.name.toLowerCase()) ||
          (currentCh.name && newMsg.channel_id && newMsg.channel_id.toLowerCase() === currentCh.name.toLowerCase()) ||
          (currentCh.name && (
            (newMsg.sender_username && newMsg.sender_username.toLowerCase() === currentCh.name.toLowerCase()) ||
            (currentCh.name.startsWith('dm-') && newMsg.sender_username && currentCh.name.toLowerCase().includes(newMsg.sender_username.toLowerCase()))
          ))
        );

        if (isMatch) {
          setMessages((prev) => {
            const existsById = prev.some((m) => m.id === newMsg.id);
            if (existsById) return prev;

            if (newMsg.nonce) {
              const pendingIdx = prev.findIndex((m) => m.nonce === newMsg.nonce);
              if (pendingIdx !== -1) {
                const copy = [...prev];
                copy[pendingIdx] = newMsg;
                return copy;
              }
            }
            return [...prev, newMsg];
          });
        }

        // Real-time friend list preview update
        if (newMsg.sender_username && currentUser?.username && newMsg.sender_username.toLowerCase() !== currentUser.username.toLowerCase()) {
          setFriends((prev) =>
            prev.map((f) => {
              if (f.friend_username && f.friend_username.toLowerCase() === newMsg.sender_username.toLowerCase()) {
                return {
                  ...f,
                  last_message: newMsg.content,
                  last_message_at: newMsg.created_at,
                };
              }
              return f;
            })
          );
        }
      } else if (
        event.type === 'friend_request_received' ||
        event.type === 'friend_accepted' ||
        event.type === 'friend_updated'
      ) {
        fetchFriendsAndRequests();
      }

      // --- WEBRTC & FALLBACK VOICE EVENTS ---
      else if (event.type === 'voice_data') {
        if (event.audio && callState === 'CONNECTED') {
          voiceManager.playVoiceChunk(event.audio);
        }
      } else if (event.type === 'voice_room_joined') {
        setInVoiceRoom(true);
        const existingPeers: string[] = event.existing_peer_ids || [];
        setVoicePeers(existingPeers);
        // Only call initiator starts offers in 1:1 calls to prevent glare collisions
        if (isCallInitiator || (!activeCallPartner && existingPeers.length > 0)) {
          for (const peerId of existingPeers) {
            await voiceManager.initiateOffer(peerId);
          }
        }
      } else if (event.type === 'voice_peer_joined') {
        setVoicePeers((prev) => Array.from(new Set([...prev, event.peer_id])));
      } else if (event.type === 'voice_peer_left') {
        setVoicePeers((prev) => prev.filter((id) => id !== event.peer_id));
      } else if (event.type === 'voice_offer') {
        await voiceManager.handleOffer(event.sender_user_id, event.sdp);
      } else if (event.type === 'voice_answer') {
        await voiceManager.handleAnswer(event.sender_user_id, event.sdp);
      } else if (event.type === 'voice_ice_candidate') {
        await voiceManager.handleIceCandidate(event.sender_user_id, event.candidate);
      }
      
      // --- 1:1 DIRECT CALL EVENTS ---
      else if (event.type === 'incoming_call' || event.type === 'call:incoming') {
        const rId = event.room_id || event.call_id;
        const cUser = event.caller_username || event.caller || 'Shinobi';
        const cName = event.caller_name || event.caller || cUser;
        const cAvatar = event.caller_avatar || event.avatar || '';
        setIncomingCallData({
          roomId: rId,
          callerId: event.caller_id || cUser,
          callerUsername: cUser,
          callerName: cName,
          callerAvatar: cAvatar,
        });
        setCallState('INCOMING_RINGING');
        setIsCallInitiator(false);
        playRingtone();
      } else if (event.type === 'call_ringing' || event.type === 'call:ringing') {
        setCallState('OUTGOING_RINGING');
      } else if (event.type === 'call_connected' || event.type === 'call:connected' || event.type === 'call:accepted') {
        const rId = event.room_id || event.call_id || activeCallPartner?.roomId;
        setCallState('CONNECTED');
        setIncomingCallData(null);
        voiceManager.unlockAudio();
        if (rId) {
          voiceManager.joinVoiceChannel(rId);
        }
      } else if (
        event.type === 'call_rejected' ||
        event.type === 'call:rejected' ||
        event.type === 'call:declined' ||
        event.type === 'call_ended' ||
        event.type === 'call:end' ||
        event.type === 'call:ended'
      ) {
        voiceManager.leaveVoiceChannel();
        setCallState('IDLE');
        setActiveCallPartner(null);
        setIncomingCallData(null);
        setCallDurationSeconds(0);
        setIsCallInitiator(false);
      } else if (event.type === 'notification_received') {
        loadNotifications();
        if ('Notification' in window && Notification.permission === 'granted') {
          try {
            const notif = event.notification;
            new Notification(notif?.title || 'Connecto Notification', {
              body: notif?.content || 'New activity on Connecto',
              icon: '/favicon.ico'
            });
          } catch {
            // ignore
          }
        }
      }
    });

    return () => {
      removeWsListener();
    };
  }, [activeChannel?.id, activeChannel?.name, activeCallPartner?.roomId, isCallInitiator, callState]);

  const playRingtone = () => {
    try {
      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, audioCtx.currentTime);
      osc.frequency.setValueAtTime(520, audioCtx.currentTime + 0.15);
      gain.gain.setValueAtTime(0.12, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.8);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.8);
    } catch {
      // ignore
    }
  };

  // Call duration counter
  useEffect(() => {
    let interval: any = null;
    if (callState === 'CONNECTED') {
      interval = setInterval(() => {
        setCallDurationSeconds((prev) => prev + 1);
      }, 1000);
    } else {
      setCallDurationSeconds(0);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [callState]);

  // Repeated ringtone when incoming ringing
  useEffect(() => {
    let interval: any = null;
    if (callState === 'INCOMING_RINGING') {
      interval = setInterval(() => {
        playRingtone();
      }, 3500);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [callState]);

  // Periodic notification polling
  useEffect(() => {
    loadNotifications();
    const interval = setInterval(loadNotifications, 20000);
    return () => clearInterval(interval);
  }, []);

  // Live polling fallback to ensure instant cross-platform sync across web & android
  useEffect(() => {
    if (!activeChannel || activeChannel.type !== 'text') return;
    const channelId = activeChannel.id;
    let isCancelled = false;

    const pollInterval = setInterval(async () => {
      try {
        const history = await getMessages(channelId);
        if (!isCancelled && history) {
          setMessages((prev) => {
            if (
              prev.length !== history.length ||
              (history.length > 0 && prev.length > 0 && history[history.length - 1].id !== prev[prev.length - 1].id)
            ) {
              return history;
            }
            return prev;
          });
        }
      } catch {
        // Ignore background polling errors
      }
    }, 2000);

    return () => {
      isCancelled = true;
      clearInterval(pollInterval);
    };
  }, [activeChannel?.id]);

  // Periodic friend requests & friends sync
  useEffect(() => {
    fetchInitialData();
    fetchFriendsAndRequests();
    const interval = setInterval(fetchFriendsAndRequests, 3500);
    return () => clearInterval(interval);
  }, []);

  const fetchInitialData = async () => {
    try {
      // 1. Fetch official public community channels
      const publicChs = await getPublicChannels();
      setPublicChannelsList(publicChs);

      // 2. Fetch custom user servers
      let serverList: Server[] = [];
      try {
        serverList = await getServers();
      } catch {
        serverList = [];
      }
      setServers(serverList);

      // 3. Default to Official Community Hub if no server is selected
      setActiveServer(OFFICIAL_HUB_SERVER);
      setChannels(publicChs);

      if (publicChs.length > 0) {
        const defaultCh = publicChs.find((c) => c.name === 'general') || publicChs[0];
        selectChannel(defaultCh);
      }
    } catch {
      // Ignore
    }
  };

  const fetchFriendsAndRequests = async () => {
    try {
      const [friendsList, reqsList] = await Promise.all([
        getFriends().catch(() => []),
        getReceivedFriendRequests().catch(() => [])
      ]);
      setFriends(friendsList);
      setReceivedRequests(reqsList);
    } catch {
      // Ignore
    }
  };

  const selectServer = async (server: Server) => {
    if (inVoiceRoom) handleLeaveVoice();
    setActiveServer(server);
    setActiveTab('chat');

    if (server.id === OFFICIAL_HUB_SERVER.id) {
      // Load canonical community channels
      let chs = publicChannelsList;
      if (chs.length === 0) {
        try {
          chs = await getPublicChannels();
          setPublicChannelsList(chs);
        } catch {
          chs = [];
        }
      }
      setChannels(chs);
      if (chs.length > 0) {
        const defaultCh = chs.find((c) => c.name === 'general') || chs[0];
        selectChannel(defaultCh);
      }
    } else {
      try {
        const chList = await getChannels(server.id);
        setChannels(chList);
        if (chList.length > 0) {
          selectChannel(chList[0]);
        }
      } catch {
        setChannels([]);
      }
    }
  };

  const selectChannel = async (channel: Channel) => {
    if (inVoiceRoom) handleLeaveVoice();
    setActiveChannel(channel);
    wsClient.subscribeChannel(channel.id, channel.name);
    if (channel.type === 'text') {
      try {
        const history = await getMessages(channel.id);
        setMessages(history);
      } catch {
        setMessages([]);
      }
    }
  };

  const handleJoinVoice = async () => {
    if (!activeChannel || activeChannel.type !== 'voice') return;
    await voiceManager.joinVoiceChannel(activeChannel.name || activeChannel.id);
  };

  const handleLeaveVoice = () => {
    voiceManager.leaveVoiceChannel();
    setInVoiceRoom(false);
    setVoicePeers([]);
  };

  const start1on1Call = async (targetUsername: string, targetDisplayName: string, targetAvatar?: string) => {
    const cleanTarget = targetUsername.replace('@', '').trim();
    const roomId = `call_${currentUser.username}_${Date.now()}`;
    setIsCallInitiator(true);
    voiceManager.unlockAudio();
    setActiveCallPartner({
      username: cleanTarget,
      displayName: targetDisplayName,
      roomId,
      avatar: targetAvatar,
    });
    setCallState('OUTGOING_RINGING');
    setCallDurationSeconds(0);

    wsClient.send({
      action: 'call:initiate',
      type: 'call:initiate',
      target: cleanTarget,
      target_user: cleanTarget,
      call_id: roomId,
      room_id: roomId,
      avatar: currentUser.avatar_url || '👤',
    });

    await voiceManager.joinVoiceChannel(roomId);
  };

  const acceptIncomingCall = async () => {
    if (!incomingCallData) return;
    const { roomId, callerUsername, callerName, callerAvatar } = incomingCallData;
    setIsCallInitiator(false);
    voiceManager.unlockAudio();
    setActiveCallPartner({
      username: callerUsername,
      displayName: callerName,
      roomId,
      avatar: callerAvatar,
    });
    setIncomingCallData(null);
    setCallState('CONNECTED');
    setCallDurationSeconds(0);

    wsClient.send({
      action: 'call:accept',
      type: 'call:accept',
      target: callerUsername,
      call_id: roomId,
      room_id: roomId,
    });

    await voiceManager.joinVoiceChannel(roomId);
  };

  const declineIncomingCall = () => {
    if (!incomingCallData) return;
    wsClient.send({
      action: 'call:decline',
      type: 'call:decline',
      target: incomingCallData.callerUsername,
      call_id: incomingCallData.roomId,
      room_id: incomingCallData.roomId,
      reason: 'declined',
    });
    setIncomingCallData(null);
    setCallState('IDLE');
    setIsCallInitiator(false);
  };

  const end1on1Call = () => {
    if (activeCallPartner) {
      wsClient.send({
        action: 'call:end',
        type: 'call:end',
        target: activeCallPartner.username,
        call_id: activeCallPartner.roomId,
        room_id: activeCallPartner.roomId,
      });
    }
    voiceManager.leaveVoiceChannel();
    setCallState('IDLE');
    setActiveCallPartner(null);
    setIncomingCallData(null);
    setCallDurationSeconds(0);
    setIsCallInitiator(false);
  };

  const toggleMuteCall = () => {
    const nextMuted = !isMuted;
    setIsMuted(nextMuted);
    voiceManager.setMuted(nextMuted);
  };

  const formatDuration = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!messageInput.trim() || !activeChannel) return;

    const content = messageInput.trim();
    const nonce = `temp-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
    setMessageInput('');
    setErrorBanner(null);

    const tempMsg: Message = {
      id: nonce,
      channel_id: activeChannel.id,
      sender_id: currentUser.id,
      sender_username: currentUser.username,
      sender_display_name: currentUser.display_name,
      sender_avatar_url: currentUser.avatar_url,
      content,
      nonce,
      created_at: new Date().toISOString(),
      pending: true,
    };

    setMessages((prev) => [...prev, tempMsg]);

    try {
      await sendMessage(activeChannel.id, content, nonce);
    } catch (err: any) {
      setMessages((prev) => prev.filter((m) => m.nonce !== nonce));
      setErrorBanner(err.message || 'Failed to send message');
    }
  };

  const handleCreateServerSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newServerName.trim()) return;
    try {
      const created = await createServer(newServerName.trim());
      setServers((prev) => [...prev, created]);
      setNewServerName('');
      setShowCreateServer(false);
      selectServer(created);
    } catch (err: any) {
      alert(err.message);
    }
  };

  const handleCreateChannelSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChannelName.trim() || !activeServer || activeServer.id === OFFICIAL_HUB_SERVER.id) return;
    try {
      const created = await createChannel(activeServer.id, newChannelName.trim(), 'text');
      setChannels((prev) => [...prev, created]);
      setNewServerChannelName('');
      setShowCreateChannel(false);
      selectChannel(created);
    } catch (err: any) {
      alert(err.message);
    }
  };

  const handleSendFriendReq = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!addFriendInput.trim()) return;
    setFriendMsg(null);
    try {
      await sendFriendRequest(addFriendInput.trim());
      setAddFriendInput('');
      setFriendMsg({ type: 'success', text: 'Friend request sent successfully!' });
      fetchFriendsAndRequests();
    } catch (err: any) {
      setFriendMsg({ type: 'error', text: err.message || 'Failed to send friend request' });
    }
  };

  const handleAcceptRequest = async (requestId: string) => {
    try {
      await acceptFriendRequest(requestId);
      setFriendMsg({ type: 'success', text: 'Friend request accepted!' });
      fetchFriendsAndRequests();
    } catch (err: any) {
      alert(err.message || 'Failed to accept friend request');
    }
  };

  const handleDeclineRequest = async (requestId: string) => {
    try {
      await declineFriendRequest(requestId);
      fetchFriendsAndRequests();
    } catch (err: any) {
      alert(err.message || 'Failed to decline friend request');
    }
  };

  const handleStartDMWithFriend = async (targetUsername: string) => {
    try {
      const dmCh = await startDM(targetUsername);
      setActiveTab('chat');
      selectChannel(dmCh);
    } catch (err: any) {
      alert(err.message || 'Failed to open DM');
    }
  };

  // Filter accepted friends
  const acceptedFriends = friends.filter((f) => f.status === 'accepted');
  // Filter outgoing pending requests
  const outgoingRequests = friends.filter((f) => f.status === 'pending' && f.user_id === currentUser.id);

  return (
    <div className="flex h-[86vh] w-full max-w-7xl mx-auto rounded-3xl glass-panel overflow-hidden shadow-2xl">
      {/* Persistent Audio Element for WebRTC & Audio Streaming Fallback */}
      <audio id="remote_call_audio" autoPlay playsInline style={{ display: 'none' }} />

      {/* 1. SERVER SIDEBAR */}
      <div className="w-18 bg-darkspace/95 border-r border-slate-800/80 p-3 flex flex-col items-center justify-between shrink-0">
        <div className="space-y-3 w-full flex flex-col items-center">
          {/* Friends Tab Button */}
          <button
            onClick={() => setActiveTab('friends')}
            className={`w-12 h-12 rounded-2xl flex items-center justify-center transition-all relative shadow-md ${
              activeTab === 'friends'
                ? 'bg-gradient-to-tr from-electric to-cyanglow text-white rounded-xl shadow-lg shadow-electric/40'
                : 'bg-darkcard text-slate-400 hover:text-white hover:bg-electric/20'
            }`}
            title="Friends & Direct Messages"
          >
            <Users className="w-6 h-6" />
            {receivedRequests.length > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-[10px] font-black rounded-full flex items-center justify-center border-2 border-darkspace">
                {receivedRequests.length}
              </span>
            )}
          </button>

          <div className="w-8 h-0.5 bg-slate-800 my-2 rounded-full"></div>

          {/* Canonical Connecto Hub (Official Community Channels) */}
          <button
            onClick={() => selectServer(OFFICIAL_HUB_SERVER)}
            className={`w-12 h-12 rounded-2xl flex items-center justify-center font-bold text-xl transition-all relative group ${
              activeServer?.id === OFFICIAL_HUB_SERVER.id && activeTab === 'chat'
                ? 'bg-gradient-to-tr from-electric via-cyanglow to-neonpink text-white rounded-xl shadow-lg shadow-electric/40 ring-2 ring-cyanglow/50'
                : 'bg-darkcard text-cyanglow hover:bg-slate-700/60 hover:text-white'
            }`}
            title="⛩️ Connecto Official Hub"
          >
            <span>⛩️</span>
            {activeServer?.id === OFFICIAL_HUB_SERVER.id && activeTab === 'chat' && (
              <span className="absolute -left-3 top-2 bottom-2 w-1 bg-cyanglow rounded-r-full"></span>
            )}
          </button>

          {/* Custom Servers */}
          {servers.map((server) => (
            <button
              key={server.id}
              onClick={() => selectServer(server)}
              className={`w-12 h-12 rounded-2xl flex items-center justify-center font-bold text-lg transition-all relative ${
                activeServer?.id === server.id && activeTab === 'chat'
                  ? 'bg-electric text-white rounded-xl shadow-lg shadow-electric/40'
                  : 'bg-darkcard text-slate-300 hover:bg-slate-700/60 hover:text-white'
              }`}
              title={server.name}
            >
              {server.name.substring(0, 2).toUpperCase()}
            </button>
          ))}

          {/* Create Server Button */}
          <button
            onClick={() => setShowCreateServer(true)}
            className="w-12 h-12 rounded-2xl bg-darkcard/50 hover:bg-cyanglow/20 border border-dashed border-slate-700 hover:border-cyanglow text-cyanglow flex items-center justify-center transition-all"
            title="Create Custom Server"
          >
            <Plus className="w-6 h-6" />
          </button>
        </div>

        <div className="flex flex-col items-center space-y-3 w-full">
          {/* Notification Bell Button */}
          <button
            onClick={() => {
              setShowNotificationTray(!showNotificationTray);
              if ('Notification' in window && Notification.permission === 'default') {
                Notification.requestPermission();
              }
            }}
            className={`w-12 h-12 rounded-2xl flex items-center justify-center transition-all relative ${
              showNotificationTray
                ? 'bg-electric text-white shadow-lg shadow-electric/40'
                : 'bg-darkcard text-slate-400 hover:text-white hover:bg-slate-700/60'
            }`}
            title="Notification Center"
          >
            <Bell className="w-5 h-5" />
            {unreadNotifCount > 0 && (
              <span className="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 bg-gradient-to-r from-red-500 to-rose-600 text-white text-[10px] font-black rounded-full flex items-center justify-center border-2 border-darkspace animate-pulse">
                {unreadNotifCount > 9 ? '9+' : unreadNotifCount}
              </span>
            )}
          </button>

          {/* User Profile Button */}
          <button
            onClick={onOpenProfile}
          className="w-12 h-12 rounded-full bg-gradient-to-tr from-electric to-cyanglow p-0.5 relative group"
          title="Open Profile Settings"
        >
          <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center overflow-hidden">
            {currentUser.avatar_url ? (
              <img src={currentUser.avatar_url} alt="Profile" className="w-full h-full object-cover" />
            ) : (
              <span className="font-bold text-sm text-cyanglow">{currentUser.display_name[0]}</span>
            )}
          </div>
          <span className="absolute bottom-0 right-0 w-3.5 h-3.5 bg-emerald-500 rounded-full border-2 border-darkspace"></span>
        </button>
        </div>
      </div>

      {/* 2. CHANNELS SIDEBAR */}
      <div className="w-64 bg-darkglass/85 border-r border-slate-800/80 p-4 flex flex-col justify-between shrink-0 overflow-y-auto">
        <div>
          {/* Header */}
          <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-4">
            <div className="flex items-center space-x-2 truncate">
              <span className="text-lg">
                {activeServer?.id === OFFICIAL_HUB_SERVER.id ? '⛩️' : '🛡️'}
              </span>
              <h2 className="font-bold text-white tracking-wide truncate">
                {activeServer ? activeServer.name : 'Connecto Hub'}
              </h2>
            </div>
            {activeServer && activeServer.owner_id === currentUser.id && activeServer.id !== OFFICIAL_HUB_SERVER.id && (
              <button
                onClick={() => setShowCreateChannel(true)}
                className="p-1 rounded-lg text-slate-400 hover:text-cyanglow hover:bg-slate-800"
                title="Create Channel"
              >
                <Plus className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Text Channels */}
          <div className="space-y-1">
            <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider px-2 mb-2 flex items-center justify-between">
              <span>{activeServer?.id === OFFICIAL_HUB_SERVER.id ? 'Official Channels' : 'Text Channels'}</span>
              <span className="text-[10px] bg-slate-800 px-1.5 py-0.5 rounded text-slate-400">
                {channels.filter((c) => c.type === 'text').length}
              </span>
            </div>
            {channels
              .filter((c) => c.type === 'text')
              .map((ch) => (
                <button
                  key={ch.id}
                  onClick={() => {
                    setActiveTab('chat');
                    selectChannel(ch);
                  }}
                  className={`w-full flex items-center space-x-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                    activeChannel?.id === ch.id && activeTab === 'chat'
                      ? 'bg-electric/20 text-cyanglow border border-electric/30 font-semibold shadow-sm shadow-electric/10'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                  }`}
                >
                  <Hash className="w-4 h-4 shrink-0 text-slate-400" />
                  <span className="truncate">{ch.name}</span>
                </button>
              ))}

            {/* Voice Rooms */}
            {channels.some((c) => c.type === 'voice') && (
              <>
                <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider px-2 mt-4 mb-2 flex items-center justify-between">
                  <span>Voice Lounge (WebRTC)</span>
                  <Radio className="w-3 h-3 text-emerald-400 animate-pulse" />
                </div>
                {channels
                  .filter((c) => c.type === 'voice')
                  .map((ch) => (
                    <button
                      key={ch.id}
                      onClick={() => {
                        setActiveTab('chat');
                        selectChannel(ch);
                      }}
                      className={`w-full flex items-center space-x-2 px-3 py-2 rounded-xl text-sm font-medium transition-all ${
                        activeChannel?.id === ch.id && activeTab === 'chat'
                          ? 'bg-electric/20 text-cyanglow border border-electric/30 font-semibold'
                          : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                      }`}
                    >
                      <Volume2 className="w-4 h-4 shrink-0 text-cyanglow" />
                      <span className="truncate">{ch.name}</span>
                    </button>
                  ))}
              </>
            )}

            {/* Direct Messages Quick List */}
            {acceptedFriends.length > 0 && (
              <>
                <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider px-2 mt-5 mb-2 flex items-center justify-between">
                  <span>Direct Messages</span>
                  <span className="text-[10px] bg-slate-800 px-1.5 py-0.5 rounded text-slate-400">
                    {acceptedFriends.length}
                  </span>
                </div>
                {acceptedFriends.map((f) => (
                  <button
                    key={f.id}
                    onClick={() => handleStartDMWithFriend(f.friend_username)}
                    className={`w-full flex items-center space-x-2.5 px-3 py-2 rounded-xl text-xs font-medium transition-all ${
                      activeChannel?.name.includes(f.friend_username) && activeTab === 'chat'
                        ? 'bg-electric/25 text-white border border-electric/40 font-semibold'
                        : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                    }`}
                  >
                    <div className="w-5 h-5 rounded-full bg-electric/30 flex items-center justify-center font-bold text-[10px] text-cyanglow shrink-0">
                      {f.friend_display_name[0]}
                    </div>
                    <span className="truncate flex-1 text-left">{f.friend_display_name}</span>
                    <span className="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>
                  </button>
                ))}
              </>
            )}
          </div>
        </div>

        {/* Current User Micro Status */}
        <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between">
          <div className="flex items-center space-x-2 truncate">
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-electric to-cyanglow p-0.5 shrink-0">
              <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center font-bold text-xs text-white">
                {currentUser.display_name[0]}
              </div>
            </div>
            <div className="truncate">
              <p className="font-bold text-white text-xs truncate">{currentUser.display_name}</p>
              <p className="text-[10px] font-mono text-emerald-400 flex items-center space-x-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 inline-block"></span>
                <span>Online & Synced</span>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* 3. MAIN CHAT / VOICE / FRIENDS AREA */}
      <div className="flex-1 bg-darkcard/40 flex flex-col justify-between overflow-hidden">
        {activeTab === 'friends' ? (
          /* ================= FRIENDS & CONNECTIONS DASHBOARD ================= */
          <div className="p-8 space-y-6 overflow-y-auto">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold text-white flex items-center space-x-2">
                  <Users className="w-7 h-7 text-cyanglow" />
                  <span>Friends & Connections</span>
                </h2>
                <p className="text-xs text-slate-400 mt-1">
                  Private direct messaging and cross-platform connection with mobile & web gamers.
                </p>
              </div>

              <div className="flex items-center space-x-2">
                <span className="text-xs font-mono text-cyanglow bg-cyanglow/10 border border-cyanglow/30 px-3 py-1 rounded-full flex items-center space-x-1.5">
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>{acceptedFriends.length} Accepted Friends</span>
                </span>
              </div>
            </div>

            {/* Add Friend Form */}
            <form onSubmit={handleSendFriendReq} className="p-6 rounded-2xl glass-card space-y-3">
              <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider">
                Add Friend by Username
              </label>
              <div className="flex space-x-3">
                <input
                  type="text"
                  required
                  value={addFriendInput}
                  onChange={(e) => setAddFriendInput(e.target.value)}
                  placeholder="Enter exact username (e.g. shinobi_99, shadow_legend)"
                  className="flex-1 px-4 py-2.5 bg-darkspace border border-slate-700/60 rounded-xl text-white text-sm focus:outline-none focus:border-cyanglow"
                />
                <button
                  type="submit"
                  className="px-5 py-2.5 bg-electric hover:bg-electric/90 text-white font-bold rounded-xl text-sm transition-all flex items-center space-x-1.5 shadow-lg shadow-electric/25"
                >
                  <UserPlus className="w-4 h-4" />
                  <span>Send Request</span>
                </button>
              </div>

              {friendMsg && (
                <p className={`text-xs mt-2 ${friendMsg.type === 'success' ? 'text-emerald-400' : 'text-red-400'}`}>
                  {friendMsg.text}
                </p>
              )}
            </form>

            {/* SECTION 1: INCOMING FRIEND REQUESTS */}
            {receivedRequests.length > 0 && (
              <div className="space-y-3">
                <div className="flex items-center space-x-2">
                  <h3 className="text-xs font-bold text-amber-400 uppercase tracking-wider">
                    Incoming Friend Requests ({receivedRequests.length})
                  </h3>
                  <span className="w-2 h-2 rounded-full bg-amber-400 animate-ping"></span>
                </div>

                <div className="space-y-2">
                  {receivedRequests.map((req) => (
                    <div
                      key={req.id}
                      className="p-4 rounded-xl glass-card border border-amber-500/30 bg-amber-500/5 flex items-center justify-between"
                    >
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-full bg-amber-500/20 border border-amber-500/40 flex items-center justify-center font-bold text-amber-300">
                          {req.sender_display_name[0]}
                        </div>
                        <div>
                          <p className="font-bold text-white text-sm">{req.sender_display_name}</p>
                          <p className="text-xs font-mono text-slate-400">@{req.sender_username} sent you a request</p>
                        </div>
                      </div>

                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => handleAcceptRequest(req.id)}
                          className="px-4 py-2 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-bold text-xs rounded-xl transition-all shadow-md flex items-center space-x-1.5"
                        >
                          <Check className="w-4 h-4" />
                          <span>Accept</span>
                        </button>
                        <button
                          onClick={() => handleDeclineRequest(req.id)}
                          className="px-3.5 py-2 bg-slate-800 hover:bg-red-500/20 text-slate-300 hover:text-red-400 border border-slate-700 hover:border-red-500/40 font-semibold text-xs rounded-xl transition-all flex items-center space-x-1"
                        >
                          <X className="w-4 h-4" />
                          <span>Decline</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* SECTION 2: ACTIVE ACCEPTED FRIENDS */}
            <div className="space-y-3">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                Active Friends ({acceptedFriends.length})
              </h3>
              {acceptedFriends.length === 0 ? (
                <div className="p-8 rounded-2xl glass-card text-center space-y-2">
                  <Users className="w-10 h-10 text-slate-600 mx-auto" />
                  <p className="text-sm font-bold text-white">No Accepted Friends Yet</p>
                  <p className="text-xs text-slate-400">
                    Send a friend request above or accept incoming requests to start private direct messaging.
                  </p>
                </div>
              ) : (
                acceptedFriends.map((f) => (
                  <div key={f.id} className="p-4 rounded-xl glass-card flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-electric to-cyanglow p-0.5">
                        <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center font-bold text-white text-xs">
                          {f.friend_display_name[0]}
                        </div>
                      </div>
                      <div>
                        <div className="flex items-center space-x-2">
                          <p className="font-bold text-white text-sm">{f.friend_display_name}</p>
                          <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                        </div>
                        <p className="text-xs font-mono text-slate-400">@{f.friend_username}</p>
                      </div>
                    </div>

                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => start1on1Call(f.friend_username, f.friend_display_name)}
                        className="px-3 py-2 bg-emerald-500/15 text-emerald-400 hover:bg-emerald-500/25 border border-emerald-500/30 rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all shadow-sm"
                        title={`Voice Call @${f.friend_username}`}
                      >
                        <Phone className="w-3.5 h-3.5" />
                        <span>Voice Call</span>
                      </button>

                      <button
                        onClick={() => handleStartDMWithFriend(f.friend_username)}
                        className="px-3.5 py-2 bg-cyanglow/10 text-cyanglow hover:bg-cyanglow/20 border border-cyanglow/30 rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all shadow-sm"
                      >
                        <MessageSquare className="w-3.5 h-3.5" />
                        <span>Direct Message</span>
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* SECTION 3: OUTGOING PENDING REQUESTS */}
            {outgoingRequests.length > 0 && (
              <div className="space-y-3 pt-2">
                <h3 className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                  Sent Requests Pending ({outgoingRequests.length})
                </h3>
                <div className="space-y-2">
                  {outgoingRequests.map((f) => (
                    <div key={f.id} className="p-3.5 rounded-xl glass-card border border-slate-800 flex items-center justify-between opacity-80">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center font-bold text-slate-400 text-xs">
                          {f.friend_display_name[0]}
                        </div>
                        <div>
                          <p className="font-bold text-slate-300 text-xs">{f.friend_display_name}</p>
                          <p className="text-[11px] font-mono text-slate-500">@{f.friend_username}</p>
                        </div>
                      </div>
                      <span className="text-[11px] text-amber-400 bg-amber-400/10 border border-amber-400/20 px-2.5 py-1 rounded-lg flex items-center space-x-1">
                        <Clock className="w-3 h-3" />
                        <span>Pending</span>
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : activeChannel?.type === 'voice' ? (
          /* ================= WEBRTC VOICE ROOM VIEW ================= */
          <div className="flex-1 flex flex-col justify-between p-8 items-center text-center">
            <div className="w-full flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center space-x-2">
                <Volume2 className="w-6 h-6 text-cyanglow" />
                <span className="font-bold text-white text-xl">{activeChannel.name}</span>
              </div>
              <span className="text-xs font-mono text-emerald-400 bg-emerald-500/10 border border-emerald-500/30 px-3 py-1 rounded-full flex items-center space-x-1">
                <Radio className="w-3 h-3 animate-pulse" />
                <span>WebRTC Voice Active</span>
              </span>
            </div>

            <div className="my-auto space-y-6 max-w-md w-full">
              <div className="w-28 h-28 mx-auto rounded-full bg-gradient-to-tr from-electric to-cyanglow p-1 shadow-2xl animate-pulse">
                <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center text-cyanglow">
                  <Volume2 className="w-12 h-12" />
                </div>
              </div>

              <div>
                <h3 className="text-2xl font-bold text-white">
                  {inVoiceRoom ? 'Connected to Voice' : 'Voice Channel Disconnected'}
                </h3>
                <p className="text-sm text-slate-400 mt-1">
                  {inVoiceRoom
                    ? `${voicePeers.length + 1} participant(s) in call • STUN Signaled`
                    : 'Click join below to transmit real-time audio stream'}
                </p>
              </div>

              {inVoiceRoom && (
                <div className="p-4 rounded-2xl glass-card text-left space-y-2">
                  <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Active Peers in Call</span>
                  <div className="flex flex-wrap gap-2 pt-1">
                    <span className="px-3 py-1 rounded-lg bg-electric/30 border border-electric/40 text-xs font-bold text-white">
                      You ({currentUser.display_name})
                    </span>
                    {voicePeers.map((peerId) => (
                      <span key={peerId} className="px-3 py-1 rounded-lg bg-cyanglow/20 border border-cyanglow/30 text-xs font-bold text-cyanglow">
                        Peer #{peerId.substring(0, 6)}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex items-center justify-center space-x-4 pt-4">
                {inVoiceRoom ? (
                  <>
                    <button
                      onClick={() => setIsMuted(!isMuted)}
                      className={`p-4 rounded-2xl transition-all shadow-lg ${
                        isMuted
                          ? 'bg-red-500/20 border border-red-500/40 text-red-400'
                          : 'bg-darkcard border border-slate-700 text-slate-200 hover:text-white'
                      }`}
                      title={isMuted ? 'Unmute Microphone' : 'Mute Microphone'}
                    >
                      {isMuted ? <MicOff className="w-6 h-6" /> : <Mic className="w-6 h-6" />}
                    </button>

                    <button
                      onClick={handleLeaveVoice}
                      className="px-6 py-4 bg-red-500 hover:bg-red-600 text-white font-bold rounded-2xl transition-all shadow-lg flex items-center space-x-2"
                    >
                      <PhoneOff className="w-5 h-5" />
                      <span>Leave Room</span>
                    </button>
                  </>
                ) : (
                  <button
                    onClick={handleJoinVoice}
                    className="px-8 py-4 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 text-white font-bold rounded-2xl transition-all shadow-xl shadow-electric/30 flex items-center space-x-2"
                  >
                    <Volume2 className="w-5 h-5" />
                    <span>Join Voice Channel</span>
                  </button>
                )}
              </div>
            </div>

            <p className="text-[11px] text-slate-500 font-mono">
              STUN: stun.l.google.com:19302 • WebRTC Audio Stream
            </p>
          </div>
        ) : (
          /* ================= TEXT CHAT VIEW ================= */
          <div className="flex-1 flex flex-col justify-between overflow-hidden">
            {/* Chat Header */}
            <div className="px-6 py-4 border-b border-slate-800/80 bg-darkglass/40 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div className="w-9 h-9 rounded-xl bg-electric/20 border border-electric/30 flex items-center justify-center text-cyanglow">
                  {activeChannel?.name.startsWith('dm-') ? <MessageSquare className="w-5 h-5" /> : <Hash className="w-5 h-5" />}
                </div>
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="font-bold text-white text-lg">
                      {activeChannel?.name.startsWith('dm-')
                        ? activeChannel.name.replace('dm-', '@').replace('-', ' & @')
                        : `#${activeChannel?.name || 'general'}`}
                    </span>
                    <span className="text-[11px] font-mono text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded">
                      Live
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">
                    {activeChannel?.name === 'general'
                      ? 'Global public community chat & discussion'
                      : activeChannel?.name === 'announcements'
                      ? 'Official updates & platform release notes'
                      : activeChannel?.name === 'dev-chat'
                      ? 'FastAPI, WebSockets, WebRTC & Security discussion'
                      : activeChannel?.name === 'gaming'
                      ? 'Gaming, memes & casual talk'
                      : activeChannel?.name === 'war-room'
                      ? '⚡ Hokage Tactical Briefings & Clan Intelligence'
                      : activeChannel?.name === 'tournaments'
                      ? 'Free Fire Esports tournaments & competitions'
                      : activeChannel?.name === 'clips'
                      ? 'Community gaming highlights & clutches'
                      : activeChannel?.name.startsWith('dm-')
                      ? 'Encrypted 1:1 Direct Chat'
                      : 'Community channel'}
                  </p>
                </div>
              </div>

              {/* Right Header Actions */}
              <div className="flex items-center space-x-2.5">
                {activeChannel?.name.startsWith('dm-') && (
                  <button
                    onClick={() => {
                      const dmPartner = activeChannel.name
                        .replace('dm-', '')
                        .split('-')
                        .find((u) => u.toLowerCase() !== currentUser.username.toLowerCase()) || '';
                      if (dmPartner) {
                        start1on1Call(dmPartner, dmPartner);
                      }
                    }}
                    className="px-3.5 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs transition-all shadow-md shadow-emerald-500/25 flex items-center space-x-1.5"
                    title="Start 1:1 Voice Call"
                  >
                    <Phone className="w-3.5 h-3.5" />
                    <span>Voice Call</span>
                  </button>
                )}

                {/* Notification Button */}
                <button
                  onClick={() => {
                    setShowNotificationTray(!showNotificationTray);
                    if ('Notification' in window && Notification.permission === 'default') {
                      Notification.requestPermission();
                    }
                  }}
                  className="relative p-2.5 rounded-xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700/60 hover:border-cyanglow/50 text-slate-300 hover:text-cyanglow transition-all flex items-center space-x-2"
                  title="Notification Center"
                >
                  <Bell className="w-4 h-4" />
                  <span className="text-xs font-semibold hidden md:inline">Alerts</span>
                  {unreadNotifCount > 0 && (
                    <span className="min-w-[18px] h-[18px] px-1 bg-gradient-to-r from-red-500 to-rose-600 text-white text-[10px] font-extrabold rounded-full flex items-center justify-center animate-pulse">
                      {unreadNotifCount > 9 ? '9+' : unreadNotifCount}
                    </span>
                  )}
                </button>
              </div>
            </div>

            {errorBanner && (
              <div className="mx-6 mt-4 p-3 bg-red-500/10 border border-red-500/30 rounded-xl flex items-center space-x-2 text-red-400 text-xs font-semibold">
                <ShieldAlert className="w-4 h-4 shrink-0" />
                <span>{errorBanner}</span>
              </div>
            )}

            {/* Messages Feed */}
            <div className="flex-1 p-6 overflow-y-auto space-y-4">
              {messages.length === 0 ? (
                <div className="h-full flex flex-col items-center justify-center text-center space-y-3 opacity-60">
                  <MessageSquare className="w-12 h-12 text-slate-500" />
                  <p className="text-white font-bold text-sm">No messages yet in #{activeChannel?.name}</p>
                  <p className="text-slate-400 text-xs max-w-sm">Be the first shinobi to break the silence! Send a message below.</p>
                </div>
              ) : (
                messages.map((msg) => {
                  const senderUser = msg.sender_username || '';
                  const currentUserName = currentUser?.username || '';
                  const isMine =
                    (senderUser && currentUserName && senderUser.toLowerCase() === currentUserName.toLowerCase()) ||
                    (msg.sender_id && currentUser?.id && msg.sender_id === currentUser.id);

                  if (isMine) {
                    return (
                      <div
                        key={msg.id}
                        className={`flex justify-end my-2 ${msg.pending ? 'opacity-50' : ''}`}
                      >
                        <div className="max-w-[75%] md:max-w-md flex flex-col items-end">
                          <div className="flex items-center space-x-2 mb-1 text-[11px] text-slate-400">
                            <span>
                              {msg.created_at
                                ? new Date(msg.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                                : 'Just now'}
                            </span>
                            <span className="font-bold text-electric">You</span>
                          </div>
                          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-2xl rounded-tr-none px-4 py-2.5 shadow-lg border border-blue-500/30">
                            <p className="text-sm leading-relaxed break-words">{msg.content}</p>
                          </div>
                        </div>
                      </div>
                    );
                  }

                  const displayName = msg.sender_display_name || msg.sender_username || 'Friend';
                  const avatarUrl = msg.sender_avatar_url;

                  return (
                    <div
                      key={msg.id}
                      className={`flex items-start space-x-3 my-2 ${msg.pending ? 'opacity-50' : ''}`}
                    >
                      <div className="w-9 h-9 rounded-full flex items-center justify-center font-bold text-xs text-white shrink-0 bg-gradient-to-tr from-cyan-600 to-blue-600 overflow-hidden shadow-sm">
                        {avatarUrl && (avatarUrl.startsWith('http') || avatarUrl.startsWith('/')) ? (
                          <img src={avatarUrl} alt={displayName} className="w-full h-full object-cover" />
                        ) : avatarUrl ? (
                          <span className="text-base">{avatarUrl}</span>
                        ) : (
                          <span>{displayName ? displayName[0].toUpperCase() : 'G'}</span>
                        )}
                      </div>

                      <div className="max-w-[75%] md:max-w-md flex flex-col items-start">
                        <div className="flex items-baseline space-x-2 mb-1">
                          <span className="font-bold text-sm text-white">{displayName}</span>
                          {msg.sender_username && <span className="text-[11px] font-mono text-cyanglow">@{msg.sender_username}</span>}
                          <span className="text-[10px] text-slate-500">
                            {msg.created_at
                              ? new Date(msg.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                              : 'Just now'}
                          </span>
                        </div>

                        <div className="bg-slate-800/80 border border-slate-700/60 text-slate-200 rounded-2xl rounded-tl-none px-4 py-2.5 shadow">
                          <p className="text-sm leading-relaxed break-words">{msg.content}</p>
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Announcement Permission Check */}
            {activeChannel?.name.toLowerCase() === 'announcements' && !(
              currentUser.is_admin ||
              currentUser.username === 'connecto_admin' ||
              currentUser.username === 'admin' ||
              currentUser.is_recruiter ||
              currentUser.username.endsWith('_admin')
            ) ? (
              <div className="p-4 bg-darkglass/80 border-t border-slate-800/80 flex items-center justify-center space-x-2 text-slate-400 text-xs font-semibold">
                <span className="p-1 rounded-md bg-amber-500/10 text-amber-400 border border-amber-500/20">🔒</span>
                <span>#announcements is read-only. Only administrators can post official updates.</span>
              </div>
            ) : (
              <>
                {/* Quick Action Gamer Chips */}
                <div className="px-6 py-2 bg-darkglass/40 border-t border-slate-800/60 flex items-center space-x-2 overflow-x-auto">
                  {quickChips.map((chip, idx) => (
                    <button
                      key={idx}
                      onClick={() => setMessageInput(chip)}
                      className="px-3 py-1 rounded-lg bg-slate-800/70 hover:bg-electric/20 border border-slate-700/60 hover:border-cyanglow/50 text-slate-300 hover:text-white text-xs font-medium whitespace-nowrap transition-all"
                    >
                      {chip}
                    </button>
                  ))}
                </div>

                {/* Message Input Form */}
                <form onSubmit={handleSendMessage} className="p-4 bg-darkglass/60 border-t border-slate-800/80 flex items-center space-x-3">
                  <input
                    type="text"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    placeholder={`Message #${activeChannel?.name || 'general'}...`}
                    className="flex-1 px-4 py-3 bg-darkspace/90 border border-slate-700/70 rounded-2xl text-white text-sm focus:outline-none focus:border-cyanglow transition-all"
                  />

                  <button
                    type="submit"
                    disabled={!messageInput.trim()}
                    className="p-3 bg-gradient-to-r from-electric to-cyanglow hover:from-electric/90 hover:to-cyanglow/90 disabled:opacity-40 disabled:cursor-not-allowed text-white rounded-2xl transition-all shadow-lg shadow-electric/20"
                    title="Send Message"
                  >
                    <Send className="w-5 h-5" />
                  </button>
                </form>
              </>
            )}
          </div>
        )}
      </div>


      {/* CREATE SERVER MODAL */}
      {showCreateServer && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-darkcard border border-slate-700 rounded-3xl p-6 max-w-md w-full space-y-4 shadow-2xl">
            <h3 className="text-xl font-bold text-white">Create Custom Server</h3>
            <p className="text-xs text-slate-400">
              Set up a private server for your squad or project team with custom channels.
            </p>
            <form onSubmit={handleCreateServerSubmit} className="space-y-4">
              <input
                type="text"
                required
                value={newServerName}
                onChange={(e) => setNewServerName(e.target.value)}
                placeholder="Server Name (e.g. Akatsuki Clan)"
                className="w-full px-4 py-2.5 bg-darkspace border border-slate-700 rounded-xl text-white text-sm focus:outline-none focus:border-cyanglow"
              />
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setShowCreateServer(false)}
                  className="px-4 py-2 text-sm text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-electric hover:bg-electric/90 text-white font-bold rounded-xl text-sm"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* CREATE CHANNEL MODAL */}
      {showCreateChannel && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-darkcard border border-slate-700 rounded-3xl p-6 max-w-md w-full space-y-4 shadow-2xl">
            <h3 className="text-xl font-bold text-white">Create Text Channel</h3>
            <form onSubmit={handleCreateChannelSubmit} className="space-y-4">
              <input
                type="text"
                required
                value={newChannelName}
                onChange={(e) => setNewServerChannelName(e.target.value)}
                placeholder="Channel Name (e.g. tactics)"
                className="w-full px-4 py-2.5 bg-darkspace border border-slate-700 rounded-xl text-white text-sm focus:outline-none focus:border-cyanglow"
              />
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setShowCreateChannel(false)}
                  className="px-4 py-2 text-sm text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-electric hover:bg-electric/90 text-white font-bold rounded-xl text-sm"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* CYBERPUNK NOTIFICATION CENTER TRAY MODAL */}
      {showNotificationTray && (
        <div className="fixed inset-0 z-50 flex items-start justify-end p-4 sm:p-6 pointer-events-none">
          <div
            className="fixed inset-0 bg-black/50 backdrop-blur-sm pointer-events-auto transition-opacity"
            onClick={() => setShowNotificationTray(false)}
          />
          <div className="relative w-full max-w-md bg-darkspace/95 border border-slate-700/80 shadow-2xl rounded-3xl p-5 pointer-events-auto flex flex-col max-h-[85vh] z-10 backdrop-blur-xl animate-in fade-in zoom-in-95 duration-200">
            {/* Header */}
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center space-x-2.5">
                <div className="w-8 h-8 rounded-lg bg-electric/20 border border-electric/40 flex items-center justify-center text-cyanglow">
                  <Bell className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white tracking-wide">Notifications</h3>
                  <p className="text-[11px] text-slate-400">
                    {unreadNotifCount} unread alert{unreadNotifCount !== 1 ? 's' : ''}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-2">
                {unreadNotifCount > 0 && (
                  <button
                    onClick={handleMarkAllNotifsRead}
                    className="p-1.5 rounded-lg text-xs text-slate-400 hover:text-cyanglow hover:bg-slate-800/80 transition-all flex items-center space-x-1"
                    title="Mark all read"
                  >
                    <CheckCheck className="w-4 h-4" />
                  </button>
                )}
                {notifications.length > 0 && (
                  <button
                    onClick={handleClearAllNotifs}
                    className="p-1.5 rounded-lg text-xs text-slate-400 hover:text-red-400 hover:bg-slate-800/80 transition-all flex items-center space-x-1"
                    title="Clear all"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
                <button
                  onClick={() => setShowNotificationTray(false)}
                  className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Filter Tabs */}
            <div className="flex items-center space-x-1.5 pt-3 pb-2 overflow-x-auto">
              {(['all', 'calls', 'requests', 'messages'] as const).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setNotifFilter(tab)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-semibold capitalize transition-all shrink-0 ${
                    notifFilter === tab
                      ? 'bg-electric text-white shadow-md shadow-electric/30'
                      : 'bg-slate-800/60 text-slate-400 hover:text-white hover:bg-slate-800'
                  }`}
                >
                  {tab}
                </button>
              ))}
            </div>

            {/* Notifications List */}
            <div className="flex-1 overflow-y-auto space-y-2.5 py-3 pr-1">
              {notifications
                .filter((n) => {
                  if (notifFilter === 'calls') return n.type.toLowerCase().includes('call') || n.type.toLowerCase().includes('voice');
                  if (notifFilter === 'requests') return n.type.toLowerCase().includes('request') || n.type.toLowerCase().includes('friend');
                  if (notifFilter === 'messages') return n.type.toLowerCase().includes('message') || n.type.toLowerCase().includes('chat');
                  return true;
                })
                .length === 0 ? (
                <div className="py-12 text-center text-slate-500 text-xs">
                  <Bell className="w-8 h-8 mx-auto mb-2 opacity-40" />
                  <p>No notifications in this category</p>
                </div>
              ) : (
                notifications
                  .filter((n) => {
                    if (notifFilter === 'calls') return n.type.toLowerCase().includes('call') || n.type.toLowerCase().includes('voice');
                    if (notifFilter === 'requests') return n.type.toLowerCase().includes('request') || n.type.toLowerCase().includes('friend');
                    if (notifFilter === 'messages') return n.type.toLowerCase().includes('message') || n.type.toLowerCase().includes('chat');
                    return true;
                  })
                  .map((notif) => (
                    <div
                      key={notif.id}
                      className={`p-3.5 rounded-2xl border transition-all ${
                        notif.is_read
                          ? 'bg-darkcard/40 border-slate-800/80 opacity-75'
                          : 'bg-slate-800/70 border-cyanglow/30 shadow-md'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-start space-x-2.5">
                          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${
                            notif.type.toLowerCase().includes('call')
                              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                              : notif.type.toLowerCase().includes('request')
                              ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                              : 'bg-electric/20 text-cyanglow border border-electric/30'
                          }`}>
                            {notif.sender_username ? notif.sender_username[0].toUpperCase() : '⚡'}
                          </div>
                          <div>
                            <div className="flex items-center space-x-2">
                              <h4 className="text-xs font-bold text-white">{notif.title}</h4>
                              {!notif.is_read && (
                                <span className="w-2 h-2 rounded-full bg-cyan-400 shadow-sm shadow-cyan-400"></span>
                              )}
                            </div>
                            <p className="text-xs text-slate-300 mt-0.5 leading-relaxed">{notif.content}</p>
                            <span className="text-[10px] text-slate-500 mt-1 block">
                              {new Date(notif.created_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                        </div>

                        {!notif.is_read && (
                          <button
                            onClick={() => handleMarkNotifRead(notif.id)}
                            className="p-1 rounded-lg text-slate-400 hover:text-cyanglow hover:bg-slate-700/50 shrink-0"
                            title="Mark as read"
                          >
                            <Check className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* ================= INCOMING VOICE CALL DIALOG ================= */}
      {callState === 'INCOMING_RINGING' && incomingCallData && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4 animate-in fade-in duration-200">
          <div className="w-full max-w-sm rounded-3xl p-6 glass-card border border-emerald-500/50 bg-slate-900/95 shadow-2xl text-center space-y-6">
            <div className="space-y-1.5">
              <span className="text-[11px] font-bold tracking-widest text-emerald-400 uppercase bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20 inline-block">
                Incoming Voice Call
              </span>
            </div>

            {/* Pulsing Avatar */}
            <div className="relative mx-auto w-24 h-24 flex items-center justify-center">
              <div className="absolute inset-0 rounded-full bg-emerald-500/20 animate-ping" />
              <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-400 p-1 relative z-10 shadow-lg shadow-emerald-500/30">
                <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center font-extrabold text-white text-2xl">
                  {incomingCallData.callerAvatar && incomingCallData.callerAvatar.length <= 2
                    ? incomingCallData.callerAvatar
                    : incomingCallData.callerName[0]?.toUpperCase() || 'S'}
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-xl font-bold text-white">{incomingCallData.callerName}</h3>
              <p className="text-xs font-mono text-slate-400">@{incomingCallData.callerUsername}</p>
              <p className="text-xs text-emerald-400 font-semibold mt-2 animate-pulse">Ringing...</p>
            </div>

            <div className="flex items-center justify-center space-x-6 pt-2">
              <button
                onClick={declineIncomingCall}
                className="w-14 h-14 rounded-full bg-red-500/20 border border-red-500/40 hover:bg-red-500 text-red-400 hover:text-white flex items-center justify-center transition-all shadow-lg hover:scale-105"
                title="Decline Call"
              >
                <PhoneOff className="w-6 h-6" />
              </button>

              <button
                onClick={acceptIncomingCall}
                className="w-14 h-14 rounded-full bg-emerald-500 hover:bg-emerald-400 text-white flex items-center justify-center transition-all shadow-xl shadow-emerald-500/40 hover:scale-105 animate-bounce"
                title="Accept Call"
              >
                <Phone className="w-6 h-6" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ================= OUTGOING CALL RINGING DIALOG ================= */}
      {callState === 'OUTGOING_RINGING' && activeCallPartner && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4 animate-in fade-in duration-200">
          <div className="w-full max-w-sm rounded-3xl p-6 glass-card border border-electric/50 bg-slate-900/95 shadow-2xl text-center space-y-6">
            <div className="space-y-1.5">
              <span className="text-[11px] font-bold tracking-widest text-cyanglow uppercase bg-electric/10 px-3 py-1 rounded-full border border-electric/20 inline-block">
                Calling Shinobi...
              </span>
            </div>

            {/* Pulsing Avatar */}
            <div className="relative mx-auto w-24 h-24 flex items-center justify-center">
              <div className="absolute inset-0 rounded-full bg-electric/20 animate-ping" />
              <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-electric to-cyanglow p-1 relative z-10 shadow-lg shadow-electric/30">
                <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center font-extrabold text-white text-2xl">
                  {activeCallPartner.avatar && activeCallPartner.avatar.length <= 2
                    ? activeCallPartner.avatar
                    : activeCallPartner.displayName[0]?.toUpperCase() || 'S'}
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-xl font-bold text-white">{activeCallPartner.displayName}</h3>
              <p className="text-xs font-mono text-slate-400">@{activeCallPartner.username}</p>
              <p className="text-xs text-cyanglow font-semibold mt-2 animate-pulse">Waiting for answer...</p>
            </div>

            <div className="flex items-center justify-center pt-2">
              <button
                onClick={end1on1Call}
                className="w-14 h-14 rounded-full bg-red-500 hover:bg-red-600 text-white flex items-center justify-center transition-all shadow-lg hover:scale-105"
                title="Cancel Call"
              >
                <PhoneOff className="w-6 h-6" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ================= FLOATING ACTIVE CALL BAR ================= */}
      {callState === 'CONNECTED' && activeCallPartner && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 max-w-lg w-[92%] glass-panel bg-slate-900/95 border border-emerald-500/50 rounded-2xl p-3.5 shadow-2xl shadow-emerald-500/10 flex items-center justify-between animate-in slide-in-from-top duration-300">
          <div className="flex items-center space-x-3">
            <div className="relative">
              <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-400 p-0.5">
                <div className="w-full h-full rounded-full bg-darkspace flex items-center justify-center font-bold text-white text-xs">
                  {activeCallPartner.avatar && activeCallPartner.avatar.length <= 2
                    ? activeCallPartner.avatar
                    : activeCallPartner.displayName[0]?.toUpperCase() || 'S'}
                </div>
              </div>
              <span className="absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full bg-emerald-500 border-2 border-slate-900" />
            </div>

            <div>
              <div className="flex items-center space-x-2">
                <p className="font-bold text-white text-sm leading-tight">{activeCallPartner.displayName}</p>
                <span className="text-[10px] text-emerald-400 font-mono bg-emerald-500/10 px-1.5 py-0.5 rounded border border-emerald-500/20">
                  {formatDuration(callDurationSeconds)}
                </span>
              </div>
              <p className="text-[11px] text-slate-400 font-mono">1:1 Voice Call • High-Fidelity Opus</p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={toggleMuteCall}
              className={`p-2.5 rounded-xl border transition-all ${
                isMuted
                  ? 'bg-red-500/20 border-red-500/40 text-red-400'
                  : 'bg-slate-800 border-slate-700 text-slate-200 hover:text-white'
              }`}
              title={isMuted ? 'Unmute Mic' : 'Mute Mic'}
            >
              {isMuted ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
            </button>

            <button
              onClick={end1on1Call}
              className="p-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white transition-all shadow-md"
              title="End Call"
            >
              <PhoneOff className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
