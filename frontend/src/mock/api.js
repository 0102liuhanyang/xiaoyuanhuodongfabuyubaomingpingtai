const mockEvents = [
  {
    id: 1,
    title: '校园音乐节',
    category: '演出',
    tags: ['音乐', '夜场'],
    location: '礼堂',
    startTime: '2026-01-12 19:00',
    endTime: '2026-01-12 21:00',
    capacity: 100,
    status: 'published',
    description: '周五晚间音乐演出，欢迎报名',
  },
  {
    id: 2,
    title: '吉他体验营',
    category: '培训',
    tags: ['吉他', '体验'],
    location: '活动室203',
    startTime: '2026-01-13 14:00',
    endTime: '2026-01-13 16:00',
    capacity: 30,
    status: 'published',
    description: '零基础吉他体验课',
  },
]

let registrations = []

export function fetchEvents() {
  return Promise.resolve({ data: mockEvents })
}

export function fetchEvent(id) {
  const event = mockEvents.find((e) => e.id === Number(id))
  return Promise.resolve({ data: event })
}

export function registerEvent(eventId) {
  if (!registrations.includes(eventId)) {
    registrations.push(eventId)
  }
  return Promise.resolve({ data: { success: true } })
}

export function cancelRegistration(eventId) {
  registrations = registrations.filter((id) => id !== eventId)
  return Promise.resolve({ data: { success: true } })
}

export function fetchMyRegistrations() {
  const data = mockEvents.filter((e) => registrations.includes(e.id))
  return Promise.resolve({ data })
}
