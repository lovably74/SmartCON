import PublicLayout from "@/components/layout/PublicLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { HardHat, UserCircle2, Users, Building2, Shield } from "lucide-react";
import { useState } from "react";
import { Link, useLocation } from "wouter";
import { toast } from "sonner";

/**
 * 5단계 역할 선택 컴포넌트
 * Requirements: 1.10, 1.11
 * 
 * 역할 계층:
 * 1. ROLE_SUPER (슈퍼관리자) - 시스템 전체 관리
 * 2. ROLE_HQ (본사관리자) - 회사 전체 관리
 * 3. ROLE_SITE (현장관리자) - 현장별 관리
 * 4. ROLE_TEAM (노무팀장) - 팀 단위 관리
 * 5. ROLE_WORKER (일반노무자) - 개인 정보 관리
 */

interface UserRole {
  role: "SUPER" | "HQ" | "SITE" | "TEAM" | "WORKER";
  displayName: string;
  description: string;
  icon: React.ReactNode;
  color: string;
  siteCount?: number;
  teamMemberCount?: number;
  lastAccess?: string;
}

export default function RoleSelect() {
  const [, setLocation] = useLocation();
  const [selectedRole, setSelectedRole] = useState<string | null>(null);

  // 사용자가 보유한 역할 목록 (API에서 가져올 데이터)
  const userRoles: UserRole[] = [
    {
      role: "SUPER",
      displayName: "슈퍼관리자",
      description: "Super Admin",
      icon: <Shield className="h-8 w-8" />,
      color: "bg-purple-100 text-purple-600 group-hover:bg-purple-600 group-hover:text-white",
      lastAccess: "오늘"
    },
    {
      role: "HQ",
      displayName: "본사관리자",
      description: "HQ Admin",
      icon: <Building2 className="h-8 w-8" />,
      color: "bg-indigo-100 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white",
      siteCount: 5,
      lastAccess: "어제"
    },
    {
      role: "SITE",
      displayName: "현장관리자",
      description: "Site Manager",
      icon: <HardHat className="h-8 w-8" />,
      color: "bg-blue-100 text-blue-600 group-hover:bg-blue-600 group-hover:text-white",
      siteCount: 2,
      lastAccess: "2일 전"
    },
    {
      role: "TEAM",
      displayName: "노무팀장",
      description: "Team Leader",
      icon: <Users className="h-8 w-8" />,
      color: "bg-orange-100 text-orange-600 group-hover:bg-orange-600 group-hover:text-white",
      teamMemberCount: 12,
      lastAccess: "오늘"
    },
    {
      role: "WORKER",
      displayName: "일반노무자",
      description: "Worker",
      icon: <UserCircle2 className="h-8 w-8" />,
      color: "bg-green-100 text-green-600 group-hover:bg-green-600 group-hover:text-white",
      lastAccess: "어제"
    }
  ];

  const handleRoleSelect = (role: UserRole) => {
    setSelectedRole(role.role);
    
    // 현장관리자나 노무팀장인 경우 현장 선택이 필요할 수 있음
    if ((role.role === "SITE" || role.role === "TEAM") && (role.siteCount || 0) > 1) {
      toast.info("현장을 선택해주세요.");
      setLocation(`/site-select?role=${role.role}`);
      return;
    }

    // 역할에 따라 적절한 대시보드로 이동
    const dashboardRoutes = {
      SUPER: "/super/dashboard",
      HQ: "/hq/dashboard",
      SITE: "/site/dashboard",
      TEAM: "/worker/dashboard",
      WORKER: "/worker/dashboard"
    };

    toast.success(`${role.displayName}로 로그인되었습니다.`);
    setLocation(dashboardRoutes[role.role]);
  };

  return (
    <PublicLayout>
      <div className="container flex items-center justify-center min-h-[calc(100vh-4rem-20rem)] py-20">
        <Card className="w-full max-w-4xl shadow-lg">
          <CardHeader className="text-center space-y-2">
            <CardTitle className="text-2xl font-bold">접속할 역할을 선택해주세요</CardTitle>
            <CardDescription>
              회원님은 현재 {userRoles.length}개의 역할 권한을 보유하고 있습니다.
            </CardDescription>
          </CardHeader>
          <CardContent className="grid md:grid-cols-3 lg:grid-cols-5 gap-4 p-6">
            {userRoles.map((role) => (
              <Button
                key={role.role}
                variant="outline"
                className="w-full h-auto flex-col p-6 hover:border-primary hover:bg-primary/5 transition-all group space-y-4"
                onClick={() => handleRoleSelect(role)}
              >
                <div className={`h-16 w-16 rounded-full flex items-center justify-center ${role.color} transition-all`}>
                  {role.icon}
                </div>
                <div className="text-center space-y-1">
                  <span className="font-bold text-lg block">{role.displayName}</span>
                  <span className="text-xs text-muted-foreground block">{role.description}</span>
                </div>
                <div className="flex flex-col gap-1 w-full">
                  {role.siteCount && (
                    <Badge variant="secondary" className="text-xs justify-center">
                      현장: {role.siteCount}개
                    </Badge>
                  )}
                  {role.teamMemberCount && (
                    <Badge variant="secondary" className="text-xs justify-center">
                      팀원: {role.teamMemberCount}명
                    </Badge>
                  )}
                  {role.lastAccess && (
                    <span className="text-xs text-muted-foreground">
                      최근 접속: {role.lastAccess}
                    </span>
                  )}
                </div>
              </Button>
            ))}
          </CardContent>
          
          <div className="px-6 pb-6">
            <div className="text-xs text-muted-foreground bg-muted/50 p-4 rounded-lg">
              <p className="font-medium mb-2">역할별 권한 안내</p>
              <ul className="space-y-1 list-disc list-inside">
                <li>슈퍼관리자: 시스템 전체 관리 및 구독 승인</li>
                <li>본사관리자: 회사 전체 현장 및 인력 관리</li>
                <li>현장관리자: 담당 현장의 출역 및 계약 관리</li>
                <li>노무팀장: 소속 팀원 관리 및 출역 확인</li>
                <li>일반노무자: 개인 출역 기록 및 계약 조회</li>
              </ul>
            </div>
          </div>
        </Card>
      </div>
    </PublicLayout>
  );
}

