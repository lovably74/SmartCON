export type UserRole =
    | 'HQ_ADMIN'
    | 'SITE_MANAGER'
    | 'TEAM_LEADER'
    | 'WORKER';

export interface User {
    id: number;
    name: string;
    role: UserRole;
    email?: string;
    phone: string;
    siteId?: number;
    teamId?: number;
    profileImage?: string;
}

export interface Site {
    id: number;
    name: string;
    address: string;
    managerId: number;
    totalWorkers: number;
    todayAttendance: number;
}

export interface Attendance {
    id: number;
    userId: number;
    siteId: number;
    date: string;
    checkIn: string;
    checkOut?: string;
    status: 'NORMAL' | 'LATE' | 'ABSENT';
    manDay: number; // 1.0 or 0.5
}

// 1. Users Data (10+ items)
export const USERS: User[] = [
    // HQ Admin
    { id: 1, name: "박본사", role: "HQ_ADMIN", email: "admin@smartcon.com", phone: "010-1111-1111" },

    // Site Managers
    { id: 2, name: "김현장", role: "SITE_MANAGER", siteId: 101, phone: "010-2222-2222" },
    { id: 3, name: "이소장", role: "SITE_MANAGER", siteId: 102, phone: "010-3333-3333" },

    // Team Leaders
    { id: 4, name: "최철근", role: "TEAM_LEADER", siteId: 101, teamId: 201, phone: "010-4444-4444" },
    { id: 5, name: "정형틀", role: "TEAM_LEADER", siteId: 101, teamId: 202, phone: "010-5555-5555" },
    { id: 6, name: "강전기", role: "TEAM_LEADER", siteId: 102, teamId: 203, phone: "010-6666-6666" },

    // Workers (Site 101)
    { id: 7, name: "김노무", role: "WORKER", siteId: 101, teamId: 201, phone: "010-7777-0001" },
    { id: 8, name: "이노무", role: "WORKER", siteId: 101, teamId: 201, phone: "010-7777-0002" },
    { id: 9, name: "박노무", role: "WORKER", siteId: 101, teamId: 202, phone: "010-7777-0003" },
    { id: 10, name: "최노무", role: "WORKER", siteId: 101, teamId: 202, phone: "010-7777-0004" },

    // Workers (Site 102)
    { id: 11, name: "정노무", role: "WORKER", siteId: 102, teamId: 203, phone: "010-7777-0005" },
    { id: 12, name: "한노무", role: "WORKER", siteId: 102, teamId: 203, phone: "010-7777-0006" },
];

// 2. Sites Data
export const SITES: Site[] = [
    { id: 101, name: "서울 강남 아파트 재건축 현장", address: "서울시 강남구 삼성동 123", managerId: 2, totalWorkers: 150, todayAttendance: 142 },
    { id: 102, name: "부산 신항만 공사 현장", address: "부산시 강서구 성북동", managerId: 3, totalWorkers: 80, todayAttendance: 75 },
];

// 3. Attendance Logs (Sample for Today)
export const ATTENDANCE_LOGS: Attendance[] = [
    { id: 1, userId: 7, siteId: 101, date: "2025-12-17", checkIn: "06:50", status: "NORMAL", manDay: 1.0 },
    { id: 2, userId: 8, siteId: 101, date: "2025-12-17", checkIn: "06:55", status: "NORMAL", manDay: 1.0 },
    { id: 3, userId: 9, siteId: 101, date: "2025-12-17", checkIn: "07:05", status: "LATE", manDay: 1.0 }, // 07:00 Late logic
    { id: 4, userId: 10, siteId: 101, date: "2025-12-17", checkIn: "06:45", status: "NORMAL", manDay: 1.0 },
    { id: 5, userId: 11, siteId: 102, date: "2025-12-17", checkIn: "07:00", status: "NORMAL", manDay: 1.0 },
    { id: 6, userId: 12, siteId: 102, date: "2025-12-17", checkIn: "07:10", status: "LATE", manDay: 0.5 },
];
