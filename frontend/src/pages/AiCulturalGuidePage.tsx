import React, { useState, useEffect, useRef } from 'react';
import { Send, Bot, User as UserIcon, Trash2, BookOpen, ExternalLink, Sparkles, AlertCircle, RefreshCw, MessageSquarePlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { aiService } from '../services/aiService';
import { AiConversation, AiMessage } from '../types/ai';

export const AiCulturalGuidePage: React.FC = () => {
  const { user } = useAuth();
  const [conversations, setConversations] = useState<AiConversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<AiMessage[]>([]);
  const [inputQuery, setInputQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (user) {
      loadConversations();
    }
  }, [user]);

  const loadConversations = async () => {
    try {
      const data = await aiService.getConversations(0, 20);
      setConversations(data.content);
    } catch (err: any) {
      console.error('Failed to load conversations', err);
    }
  };

  const handleSelectConversation = async (id: string) => {
    setActiveConversationId(id);
    setError(null);
    try {
      const conv = await aiService.getConversationById(id);
      setMessages(conv.messages || []);
    } catch (err: any) {
      setError('Failed to load conversation details');
    }
  };

  const handleNewChat = () => {
    setActiveConversationId(null);
    setMessages([]);
    setError(null);
    setInputQuery('');
  };

  const handleDeleteConversation = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await aiService.deleteConversation(id);
      setConversations(conversations.filter(c => c.id !== id));
      if (activeConversationId === id) {
        handleNewChat();
      }
    } catch (err: any) {
      console.error('Failed to delete conversation', err);
    }
  };

  const handleSend = async (queryToSend?: string) => {
    const query = queryToSend || inputQuery;
    if (!query.trim() || loading) return;

    if (!user) {
      setError('Please log in to chat with the AI Cultural Guide.');
      return;
    }

    const userMessage: AiMessage = {
      id: 'temp-' + Date.now(),
      sender: 'USER',
      content: query,
      createdAt: new Date().toISOString(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInputQuery('');
    setLoading(true);
    setError(null);

    try {
      const res = await aiService.askCulturalGuide(query, activeConversationId || undefined);
      
      if (!activeConversationId) {
        setActiveConversationId(res.conversationId);
        loadConversations();
      }

      const assistantMessage: AiMessage = {
        id: res.messageId,
        sender: 'ASSISTANT',
        content: res.answer,
        sources: res.sources,
        relevanceScore: res.relevanceScore,
        createdAt: res.createdAt,
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to get answer from AI Cultural Guide. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (messagesEndRef.current && typeof messagesEndRef.current.scrollIntoView === 'function') {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, loading]);

  const quickPrompts = [
    { title: 'What is Silambam?', query: 'What is Silambam and what is its primary weapon?' },
    { title: 'Footwork Stances', query: 'What are the primary footwork stances in Silambam?' },
    { title: 'சிலம்பம் வரலாறு', query: 'சிலம்பம் தற்காப்புக் கலையின் வரலாறு மற்றும் சிறப்புகள் யாவை?' },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 min-h-[calc(100vh-80px)]">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-amber-900 via-amber-800 to-amber-950 text-white rounded-2xl p-6 sm:p-8 mb-8 shadow-xl border border-amber-700/40 relative overflow-hidden">
        <div className="relative z-10 max-w-3xl">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/20 text-amber-300 text-xs font-semibold uppercase tracking-wider mb-4 border border-amber-500/30">
            <Sparkles className="w-4 h-4 text-amber-400" /> Grounded Heritage Intelligence
          </div>
          <h1 className="text-3xl sm:text-4xl font-bold font-serif text-amber-50 mb-2">
            Marabagam AI Cultural Guide
          </h1>
          <p className="text-amber-200/90 text-sm sm:text-base leading-relaxed">
            Ask questions about Tamil traditional arts, history, stances, and heritage. Every response is strictly grounded in verified digital archives with full source attribution.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Sidebar - Conversation History */}
        <div className="lg:col-span-1 bg-white rounded-2xl p-4 shadow-sm border border-stone-200 flex flex-col h-[650px]">
          <button
            onClick={handleNewChat}
            className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-amber-600 hover:bg-amber-700 text-white rounded-xl font-medium transition-colors mb-4 shadow-sm"
          >
            <MessageSquarePlus className="w-5 h-5" /> New Conversation
          </button>

          <h3 className="text-xs font-semibold text-stone-500 uppercase tracking-wider mb-3 px-2">
            Conversation History
          </h3>

          <div className="flex-1 overflow-y-auto space-y-2 pr-1">
            {!user ? (
              <p className="text-xs text-stone-400 p-2 italic text-center">Log in to view saved chat history.</p>
            ) : conversations.length === 0 ? (
              <p className="text-xs text-stone-400 p-2 italic text-center">No previous conversations.</p>
            ) : (
              conversations.map(conv => (
                <div
                  key={conv.id}
                  onClick={() => handleSelectConversation(conv.id)}
                  className={`group flex items-center justify-between p-3 rounded-xl cursor-pointer text-sm transition-all ${
                    activeConversationId === conv.id
                      ? 'bg-amber-50 text-amber-900 font-medium border border-amber-200'
                      : 'text-stone-700 hover:bg-stone-50'
                  }`}
                >
                  <span className="truncate flex-1 pr-2">{conv.title}</span>
                  <button
                    onClick={(e) => handleDeleteConversation(conv.id, e)}
                    className="opacity-0 group-hover:opacity-100 text-stone-400 hover:text-red-600 transition-opacity p-1 rounded hover:bg-stone-100"
                    title="Delete Conversation"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Main Chat Panel */}
        <div className="lg:col-span-3 bg-white rounded-2xl shadow-sm border border-stone-200 flex flex-col h-[650px] overflow-hidden">
          {/* Chat Messages */}
          <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-6">
            {messages.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 max-w-lg mx-auto">
                <div className="w-16 h-16 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center mb-4">
                  <Bot className="w-8 h-8" />
                </div>
                <h3 className="text-lg font-bold text-stone-900 mb-2 font-serif">Welcome to AI Cultural Guide</h3>
                <p className="text-sm text-stone-600 mb-6">
                  Select a topic or type a question below to start exploring Tamil heritage.
                </p>

                {/* Quick Prompts */}
                <div className="w-full space-y-2">
                  {quickPrompts.map((prompt, idx) => (
                    <button
                      key={idx}
                      onClick={() => handleSend(prompt.query)}
                      className="w-full text-left p-3 rounded-xl border border-stone-200 hover:border-amber-400 hover:bg-amber-50/50 transition-all text-xs sm:text-sm text-stone-700 flex items-center justify-between group"
                    >
                      <span>{prompt.title}</span>
                      <Sparkles className="w-4 h-4 text-amber-500 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              messages.map(msg => (
                <div
                  key={msg.id}
                  className={`flex gap-3 sm:gap-4 ${msg.sender === 'USER' ? 'flex-row-reverse' : 'flex-row'}`}
                >
                  <div
                    className={`w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 text-sm font-semibold ${
                      msg.sender === 'USER' ? 'bg-stone-800 text-white' : 'bg-amber-600 text-white'
                    }`}
                  >
                    {msg.sender === 'USER' ? <UserIcon className="w-5 h-5" /> : <Bot className="w-5 h-5" />}
                  </div>

                  <div className={`max-w-[80%] rounded-2xl p-4 text-sm leading-relaxed ${
                    msg.sender === 'USER'
                      ? 'bg-stone-800 text-white rounded-tr-none'
                      : 'bg-stone-50 border border-stone-200 text-stone-800 rounded-tl-none'
                  }`}>
                    <p className="whitespace-pre-wrap">{msg.content}</p>

                    {/* Sources Attribution */}
                    {msg.sources && msg.sources.length > 0 && (
                      <div className="mt-4 pt-3 border-t border-stone-200/80">
                        <div className="flex items-center gap-1.5 text-xs font-semibold text-amber-900 mb-2">
                          <BookOpen className="w-3.5 h-3.5" /> Cited Heritage Sources:
                        </div>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                          {msg.sources.map((src, idx) => (
                            <div key={idx} className="bg-white p-2.5 rounded-lg border border-stone-200 text-xs">
                              <div className="font-semibold text-stone-900 truncate">{src.title}</div>
                              {src.author && <div className="text-stone-500 text-[11px]">Author: {src.author}</div>}
                              <div className="text-stone-500 text-[11px] truncate">{src.sourceName}</div>
                              {src.sourceUrl && (
                                <a
                                  href={src.sourceUrl}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="inline-flex items-center gap-1 text-amber-700 hover:text-amber-900 text-[11px] font-medium mt-1"
                                >
                                  View Source <ExternalLink className="w-3 h-3" />
                                </a>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}

            {/* Shimmer Loading */}
            {loading && (
              <div className="flex gap-4 items-start">
                <div className="w-9 h-9 rounded-full bg-amber-600 text-white flex items-center justify-center">
                  <Bot className="w-5 h-5 animate-pulse" />
                </div>
                <div className="bg-stone-50 border border-stone-200 p-4 rounded-2xl rounded-tl-none text-xs text-stone-500 flex items-center gap-2">
                  <RefreshCw className="w-4 h-4 animate-spin text-amber-600" /> Retrieving verified heritage information...
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Error Banner */}
          {error && (
            <div className="bg-red-50 border-t border-red-200 p-3 px-4 flex items-center justify-between text-xs text-red-700">
              <span className="flex items-center gap-2"><AlertCircle className="w-4 h-4" /> {error}</span>
              <button onClick={() => setError(null)} className="font-semibold underline hover:text-red-900">Dismiss</button>
            </div>
          )}

          {/* Input Bar */}
          <div className="p-4 border-t border-stone-200 bg-white">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              className="flex gap-2"
            >
              <input
                type="text"
                value={inputQuery}
                onChange={(e) => setInputQuery(e.target.value)}
                placeholder="Ask about Tamil art forms, history, stances, or traditional masters..."
                disabled={loading}
                className="flex-1 px-4 py-3 border border-stone-300 rounded-xl focus:ring-2 focus:ring-amber-500 focus:border-amber-500 text-sm outline-none disabled:bg-stone-100"
              />
              <button
                type="submit"
                disabled={loading || !inputQuery.trim()}
                className="px-5 py-3 bg-amber-600 hover:bg-amber-700 disabled:opacity-50 text-white font-medium rounded-xl transition-colors flex items-center gap-2 text-sm shadow-sm"
              >
                <span>Send</span> <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
