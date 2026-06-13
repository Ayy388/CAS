import type { CampaignStatus, OfferingStatus, EnrollmentStatus, CourseType } from '@/types'
import {
  CAMPAIGN_STATUS_MAP,
  OFFERING_STATUS_MAP,
  ENROLLMENT_STATUS_MAP,
  COURSE_TYPE_MAP,
} from '@/types'

export function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

export function campaignStatusText(status: CampaignStatus): string {
  return CAMPAIGN_STATUS_MAP[status] ?? status
}

export function offeringStatusText(status: OfferingStatus): string {
  return OFFERING_STATUS_MAP[status] ?? status
}

export function enrollmentStatusText(status: EnrollmentStatus): string {
  return ENROLLMENT_STATUS_MAP[status] ?? status
}

export function courseTypeText(type: CourseType): string {
  return COURSE_TYPE_MAP[type] ?? type
}

export function formatRemaining(seconds: number): string {
  if (seconds <= 0) return '已结束'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  const parts: string[] = []
  if (d > 0) parts.push(`${d}天`)
  if (h > 0 || d > 0) parts.push(`${h}小时`)
  if (m > 0 || h > 0 || d > 0) parts.push(`${m}分钟`)
  parts.push(`${s}秒`)
  return parts.join('')
}