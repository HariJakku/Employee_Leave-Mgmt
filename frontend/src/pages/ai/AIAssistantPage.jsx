import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Bot, Send, Sparkles, MessageSquare, FileSpreadsheet,
  BrainCircuit, Users, User, ArrowRight, CheckCircle2
} from 'lucide-react'
import toast from 'react-hot-toast'
import aiService from '../../services/aiService'
import employeeService from '../../services/employeeService'
import leaveService from '../../services/leaveService'
import { useAuth } from '../../context/AuthContext'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import { formatDate } from '../../utils/dateUtils'
import { ROLES } from '../../utils/constants'

function AIAssistantPage() {
  const navigate = useNavigate()
  const { role, user } = useAuth()
  const isHR = [ROLES.ADMIN, ROLES.HR].includes(role)

  // Active feature tab: 'chat' | 'parse' | 'hr' | 'summary'
  const [activeTab, setActiveTab] = useState('chat')

  // Chat state
  const [chatMessages, setChatMessages] = useState([
    {
      sender: 'ai',
      text: `Hello ${user?.firstName || 'there'}! I am your AI Leave & HR Assistant. You can ask me about your available leave balances, upcoming holidays, or company policy limits. How can I assist you today?`,
    }
  ])
  const [inputPrompt, setInputPrompt] = useState('')
  const [chatLoading, setChatLoading] = useState(false)
  const chatBottomRef = useRef(null)

  // Natural Language Leave Parser state
  const [nlInput, setNlInput] = useState('')
  const [parsing, setParsing] = useState(false)
  const [parsedDraft, setParsedDraft] = useState(null)
  const [submittingLeave, setSubmittingLeave] = useState(false)

  // HR Intelligence Query state
  const [hrQuery, setHrQuery] = useState('')
  const [hrReply, setHrReply] = useState(null)
  const [hrLoading, setHrLoading] = useState(false)

  // Employee Summary state
  const [employees, setEmployees] = useState([])
  const [selectedEmpId, setSelectedEmpId] = useState('')
  const [empSummary, setEmpSummary] = useState(null)
  const [summaryLoading, setSummaryLoading] = useState(false)

  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [chatMessages])

  useEffect(() => {
    if (isHR) {
      employeeService.getEmployees({ size: 100, status: 'ACTIVE' })
        .then(res => setEmployees(res.content || []))
        .catch(console.error)
    }
  }, [isHR])

  // Chat submit
  const handleSendChat = async (promptToSend) => {
    const prompt = promptToSend || inputPrompt
    if (!prompt.trim()) return

    setChatMessages(prev => [...prev, { sender: 'user', text: prompt }])
    setInputPrompt('')
    setChatLoading(true)

    try {
      const res = await aiService.chatWithLeaveAssistant(prompt)
      setChatMessages(prev => [...prev, { sender: 'ai', text: res.reply }])
    } catch {
      setChatMessages(prev => [...prev, { sender: 'ai', text: 'Sorry, I encountered an error. Please try again.' }])
    } finally {
      setChatLoading(false)
    }
  }

  // Parse natural language to leave request
  const handleParseLeave = async () => {
    if (!nlInput.trim()) {
      toast.error('Please describe your leave requirement')
      return
    }

    setParsing(true)
    setParsedDraft(null)
    try {
      const draft = await aiService.parseLeaveRequest(nlInput)
      setParsedDraft(draft)
    } catch (err) {
      toast.error('Failed to parse leave draft')
    } finally {
      setParsing(false)
    }
  }

  // Confirm and submit parsed leave
  const handleConfirmSubmitLeave = async () => {
    if (!parsedDraft) return
    setSubmittingLeave(true)
    try {
      await leaveService.applyLeave({
        leaveTypeId: parsedDraft.leaveTypeId,
        startDate: parsedDraft.startDate,
        endDate: parsedDraft.endDate,
        reason: parsedDraft.reason,
      })
      toast.success('Leave request successfully filed via AI Assistant!')
      navigate('/leave/history')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Submission failed')
    } finally {
      setSubmittingLeave(false)
    }
  }

  // HR Intelligence Query
  const handleHRQuery = async (queryText) => {
    const q = queryText || hrQuery
    if (!q.trim()) return
    setHrLoading(true)
    setHrReply(null)
    try {
      const res = await aiService.queryHRAssistant(q)
      setHrReply(res.reply)
    } catch {
      toast.error('Query failed')
    } finally {
      setHrLoading(false)
    }
  }

  // Employee Summary generator
  const handleGenerateSummary = async () => {
    if (!selectedEmpId) {
      toast.error('Select an employee first')
      return
    }
    setSummaryLoading(true)
    setEmpSummary(null)
    try {
      const res = await aiService.generateEmployeeSummary(selectedEmpId)
      setEmpSummary(res.reply)
    } catch {
      toast.error('Failed to generate summary')
    } finally {
      setSummaryLoading(false)
    }
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="page-header">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-1.5 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-lg text-white">
              <Bot size={20} />
            </span>
            <h1 className="page-title">Enterprise AI Assistant</h1>
          </div>
          <p className="page-subtitle">
            Ground-truth LLM intelligence for leave policies, natural language request drafting, and workforce metrics
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-gray-200 overflow-x-auto">
        <button
          onClick={() => setActiveTab('chat')}
          className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center gap-2 whitespace-nowrap transition-colors ${
            activeTab === 'chat'
              ? 'border-primary-600 text-primary-600'
              : 'border-transparent text-gray-500 hover:text-gray-800'
          }`}
        >
          <MessageSquare size={16} /> Leave Policy Advisor
        </button>

        <button
          onClick={() => setActiveTab('parse')}
          className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center gap-2 whitespace-nowrap transition-colors ${
            activeTab === 'parse'
              ? 'border-primary-600 text-primary-600'
              : 'border-transparent text-gray-500 hover:text-gray-800'
          }`}
        >
          <Sparkles size={16} /> Natural Language Leave Request
        </button>

        {isHR && (
          <>
            <button
              onClick={() => setActiveTab('hr')}
              className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center gap-2 whitespace-nowrap transition-colors ${
                activeTab === 'hr'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-800'
              }`}
            >
              <BrainCircuit size={16} /> HR Analytics Copilot
            </button>

            <button
              onClick={() => setActiveTab('summary')}
              className={`px-5 py-3 text-sm font-semibold border-b-2 flex items-center gap-2 whitespace-nowrap transition-colors ${
                activeTab === 'summary'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-800'
              }`}
            >
              <User size={16} /> Employee Executive Profiler
            </button>
          </>
        )}
      </div>

      {/* TAB 1: Chat Advisor */}
      {activeTab === 'chat' && (
        <div className="space-y-4">
          {/* Quick Prompts */}
          <div className="flex flex-wrap gap-2">
            <span className="text-xs text-gray-400 font-semibold py-1">Quick Prompts:</span>
            {[
              'What is my remaining sick leave balance?',
              'How many casual leave days do I have available?',
              'Explain the company earned leave policy',
              'Can I take leave next Monday?',
            ].map((qp, idx) => (
              <button
                key={idx}
                onClick={() => handleSendChat(qp)}
                className="text-xs px-3 py-1 bg-white border border-gray-200 text-gray-700 rounded-full hover:bg-gray-50 transition-colors shadow-sm"
              >
                {qp}
              </button>
            ))}
          </div>

          {/* Chat Box */}
          <div className="card p-0 flex flex-col h-[520px] overflow-hidden">
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {chatMessages.map((m, idx) => (
                <div
                  key={idx}
                  className={`flex items-start gap-3 ${m.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  {m.sender === 'ai' && (
                    <div className="w-8 h-8 rounded-full bg-indigo-600 text-white flex items-center justify-center flex-shrink-0 text-xs">
                      <Bot size={16} />
                    </div>
                  )}

                  <div
                    className={`max-w-[78%] rounded-2xl px-4 py-3 text-sm leading-relaxed ${
                      m.sender === 'user'
                        ? 'bg-primary-600 text-white rounded-tr-none'
                        : 'bg-gray-100 text-gray-900 rounded-tl-none whitespace-pre-line'
                    }`}
                  >
                    {m.text}
                  </div>

                  {m.sender === 'user' && (
                    <div className="w-8 h-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center flex-shrink-0 text-xs font-bold">
                      Me
                    </div>
                  )}
                </div>
              ))}

              {chatLoading && (
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-indigo-600 text-white flex items-center justify-center flex-shrink-0 text-xs">
                    <Bot size={16} />
                  </div>
                  <div className="bg-gray-100 text-gray-500 rounded-2xl px-4 py-2.5 text-xs flex items-center gap-2">
                    <LoadingSpinner size="sm" />
                    <span>Analyzing database records...</span>
                  </div>
                </div>
              )}
              <div ref={chatBottomRef} />
            </div>

            {/* Input Bar */}
            <form
              onSubmit={(e) => { e.preventDefault(); handleSendChat(); }}
              className="p-3 bg-white border-t border-gray-100 flex items-center gap-2"
            >
              <input
                type="text"
                value={inputPrompt}
                onChange={(e) => setInputPrompt(e.target.value)}
                placeholder="Ask about leave entitlements, policies, or quota rules..."
                className="input flex-1"
                disabled={chatLoading}
              />
              <button
                type="submit"
                disabled={chatLoading || !inputPrompt.trim()}
                className="btn-primary"
              >
                <Send size={16} />
              </button>
            </form>
          </div>
        </div>
      )}

      {/* TAB 2: Natural Language Leave Parser */}
      {activeTab === 'parse' && (
        <div className="card space-y-5">
          <div>
            <h3 className="font-bold text-gray-900 text-base">Smart Leave Request Generator</h3>
            <p className="text-xs text-gray-500 mt-1">
              Type your leave plan in natural English. Our AI will automatically parse the leave type, start/end dates,
              calculate the total days, and prepare a structured request for you to inspect before submitting.
            </p>
          </div>

          <div className="space-y-2">
            <textarea
              rows={3}
              value={nlInput}
              onChange={(e) => setNlInput(e.target.value)}
              placeholder="e.g.: I need to take sick leave next Monday and Tuesday because I have a doctor appointment scheduled."
              className="input text-sm"
            />

            <div className="flex items-center justify-between">
              <span className="text-xs text-gray-400">
                Example: "Taking casual leave from 2026-10-12 to 2026-10-15 for a family trip"
              </span>
              <button
                onClick={handleParseLeave}
                disabled={parsing || !nlInput.trim()}
                className="btn-primary btn-sm"
              >
                {parsing ? <LoadingSpinner size="sm" /> : <Sparkles size={14} />}
                {parsing ? 'Parsing Description...' : 'Generate Structured Draft'}
              </button>
            </div>
          </div>

          {/* Parsed Result Preview */}
          {parsedDraft && (
            <div className="mt-6 p-5 rounded-xl border border-indigo-100 bg-indigo-50/50 space-y-4 animate-fade-in">
              <div className="flex items-center justify-between border-b border-indigo-100 pb-3">
                <div className="flex items-center gap-2">
                  <CheckCircle2 size={18} className="text-emerald-600" />
                  <span className="font-bold text-gray-900 text-sm">Parsed Leave Application Draft</span>
                </div>
                <span className="text-xs text-indigo-700 font-semibold bg-indigo-100 px-2.5 py-0.5 rounded-full">
                  {parsedDraft.leaveTypeName}
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                <div>
                  <span className="text-gray-400 block">Start Date</span>
                  <span className="font-bold text-gray-800 text-sm">{formatDate(parsedDraft.startDate)}</span>
                </div>
                <div>
                  <span className="text-gray-400 block">End Date</span>
                  <span className="font-bold text-gray-800 text-sm">{formatDate(parsedDraft.endDate)}</span>
                </div>
                <div>
                  <span className="text-gray-400 block">Calculated Days</span>
                  <span className="font-extrabold text-indigo-700 text-sm">{parsedDraft.totalDays} days</span>
                </div>
              </div>

              <div className="text-xs">
                <span className="text-gray-400 block">Extracted Reason</span>
                <p className="text-gray-800 font-medium mt-0.5">{parsedDraft.reason}</p>
              </div>

              <div className="text-[11px] text-gray-500 italic bg-white/70 p-2.5 rounded-lg border border-indigo-100">
                {parsedDraft.explanation}
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  onClick={() => setParsedDraft(null)}
                  className="btn btn-secondary btn-sm"
                >
                  Discard
                </button>
                <button
                  onClick={handleConfirmSubmitLeave}
                  disabled={submittingLeave}
                  className="btn-primary btn-sm"
                >
                  {submittingLeave ? <LoadingSpinner size="sm" /> : <Send size={14} />}
                  {submittingLeave ? 'Submitting...' : 'Confirm & Submit to Manager'}
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 3: HR Copilot */}
      {isHR && activeTab === 'hr' && (
        <div className="card space-y-5">
          <div>
            <h3 className="font-bold text-gray-900 text-base">HR Intelligence & Workforce Analytics</h3>
            <p className="text-xs text-gray-500 mt-1">
              Ask questions regarding company-wide presence, departmental leave load, or capacity management.
            </p>
          </div>

          <div className="flex flex-wrap gap-2">
            {[
              'How many employees are currently on leave today?',
              'Which department has the highest leave activity?',
              'Summarize organization attendance health',
            ].map((sample, idx) => (
              <button
                key={idx}
                onClick={() => handleHRQuery(sample)}
                className="text-xs px-3 py-1 bg-gray-50 border border-gray-200 text-gray-700 rounded-full hover:bg-gray-100 transition-colors"
              >
                {sample}
              </button>
            ))}
          </div>

          <div className="flex gap-2">
            <input
              type="text"
              value={hrQuery}
              onChange={(e) => setHrQuery(e.target.value)}
              placeholder="Ask an HR analytics inquiry..."
              className="input flex-1"
            />
            <button
              onClick={() => handleHRQuery()}
              disabled={hrLoading || !hrQuery.trim()}
              className="btn-primary"
            >
              {hrLoading ? <LoadingSpinner size="sm" /> : <Sparkles size={16} />}
              {hrLoading ? 'Analyzing...' : 'Ask HR Copilot'}
            </button>
          </div>

          {hrReply && (
            <div className="p-5 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl border border-blue-100 text-sm text-gray-800 leading-relaxed space-y-2 animate-fade-in">
              <div className="flex items-center gap-2 text-indigo-700 font-bold text-xs uppercase tracking-wider">
                <Sparkles size={14} /> Executive HR Summary
              </div>
              <p className="whitespace-pre-line">{hrReply}</p>
            </div>
          )}
        </div>
      )}

      {/* TAB 4: Employee Executive Profiler */}
      {isHR && activeTab === 'summary' && (
        <div className="card space-y-5">
          <div>
            <h3 className="font-bold text-gray-900 text-base">Employee AI Profiler</h3>
            <p className="text-xs text-gray-500 mt-1">
              Generate an executive summary of an employee's tenure, department contributions, and leave records.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-3">
            <select
              value={selectedEmpId}
              onChange={(e) => setSelectedEmpId(e.target.value)}
              className="input sm:max-w-md"
            >
              <option value="">Select Employee to Profile</option>
              {employees.map(emp => (
                <option key={emp.id} value={emp.id}>
                  {emp.fullName} ({emp.employeeCode} - {emp.designation})
                </option>
              ))}
            </select>

            <button
              onClick={handleGenerateSummary}
              disabled={summaryLoading || !selectedEmpId}
              className="btn-primary"
            >
              {summaryLoading ? <LoadingSpinner size="sm" /> : <Bot size={16} />}
              {summaryLoading ? 'Generating Profile...' : 'Generate Executive Summary'}
            </button>
          </div>

          {empSummary && (
            <div className="p-5 bg-gray-50 rounded-xl border border-gray-200 text-sm text-gray-800 leading-relaxed space-y-3 animate-fade-in">
              <div className="flex items-center justify-between border-b border-gray-200 pb-2">
                <span className="font-bold text-gray-900 text-xs uppercase tracking-wider">
                  AI-Assisted Personnel Summary
                </span>
                <span className="text-[11px] text-gray-400">Strictly Confidential</span>
              </div>
              <p className="whitespace-pre-line text-gray-700">{empSummary}</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default AIAssistantPage
